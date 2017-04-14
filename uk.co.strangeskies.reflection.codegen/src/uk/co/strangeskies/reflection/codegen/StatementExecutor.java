/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,|__   __||  _ `.        / \     |  \   | |  ,-`__`¬  ,-`__`¬
 *   ( (_`-'   | |   | | ) |       / . \    | . \  | | / .`  `' / .`  `'
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `.`.   / /   \ \  | | \ \| || |   | || +--'
 *  \ \__.' /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,.
 *   `.__.-`   |_|   |_|    |_|/_/       \_\|_|   \__|  `-.__.J  `-.__.J
 *                   __    _         _      __      __
 *                 ,`_ `, | |  _    | |  ,-`__`¬  ,`_ `,
 *                ( (_`-' | | ) |   | | / .`  `' ( (_`-'
 *                 `._ `. | L-' L   | || '--.     `._ `.
 *                _   `. \| ,.-^.`. | || +--'    _   `. \
 *               \ \__.' /| |    \ \| | \ `.__,.\ \__.' /
 *                `.__.-` |_|    |_||_|  `-.__.J `.__.-`
 *
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;

import java.util.HashMap;
import java.util.Map;

public class StatementExecutor {
	private class StatementVisitorImpl implements StatementVisitor {
		private boolean complete = false;
		private Object result;

		@Override
		public void visitReturn() {
			complete = true;
		}

		@Override
		public <T> void visitReturn(ValueExpression<T> expression) {
			visitReturn();
			result = expressionEvaluator.evaluate(expression).get();
		}

		@Override
		public void visitExpression(Expression expression) {
			expressionEvaluator.evaluate(expression);
		}

		@Override
		public <T> void visitDeclaration(LocalVariableExpression<T> variable) {
			declareLocal(variable.getSignature());
		}

		@Override
		public <T> void visitDeclaration(
				LocalValueExpression<T> variable,
				ValueExpression<? extends T> initializer) {
			declareLocal(variable.getSignature());
			setEnclosedLocal(variable.getSignature(), expressionEvaluator.evaluate(initializer).get());
		}

		private boolean isComplete() {
			return complete;
		}

		private Object getResult() {
			return result;
		}
	}

	private final ReflectiveInstance<?, ?> receiver;
	private final StatementExecutor enclosingState;

	private final Map<ParameterSignature<?>, Object> locals;

	private final ExpressionEvaluator expressionEvaluator;

	private StatementExecutor(ReflectiveInstance<?, ?> receiver, StatementExecutor enclosingState) {
		this.receiver = receiver;
		this.enclosingState = enclosingState;

		locals = new HashMap<>();

		expressionEvaluator = new ExpressionEvaluator(this);
	}

	public StatementExecutor(ReflectiveInstance<?, ?> receiver) {
		this(receiver, null);
	}

	public StatementExecutor() {
		this(null);
	}

	public StatementExecutor enclose(ReflectiveInstance<?, ?> receiver) {
		return new StatementExecutor(receiver, this);
	}

	public StatementExecutor enclose() {
		return enclose(null);
	}

	public <T> void declareLocal(ParameterSignature<T> variable) {
		if (locals.containsKey(variable)) {
			throw new CodeGenerationException(CODEGEN_PROPERTIES.cannotRedeclareVariable(variable));
		}
		locals.put(variable, null);
	}

	@SuppressWarnings("unchecked")
	public <T> T getEnclosedLocal(ParameterSignature<T> variable) {
		if (locals.containsKey(variable)) {
			return (T) locals.get(variable);
		} else if (enclosingState != null) {
			return enclosingState.getEnclosedLocal(variable);
		} else {
			throw new CodeGenerationException(CODEGEN_PROPERTIES.undefinedVariable(variable));
		}
	}

	public <T> void setEnclosedLocal(ParameterSignature<T> variable, T value) {
		if (locals.containsKey(variable)) {
			locals.put(variable, value);
		} else if (enclosingState != null) {
			enclosingState.setEnclosedLocal(variable, value);
		} else {
			throw new CodeGenerationException(CODEGEN_PROPERTIES.undefinedVariable(variable));
		}
	}

	@SuppressWarnings("unchecked")
	public <J> J getEnclosingInstance(ClassDeclaration<?, J> receiverClass) {
		if (receiver.getClassDefinition().getDeclaration() == receiverClass) {
			return (J) receiver;
		} else if (enclosingState != null) {
			return enclosingState.getEnclosingInstance(receiverClass);
		} else {
			throw new CodeGenerationException(
					CODEGEN_PROPERTIES.cannotResolveEnclosingInstance(receiverClass));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T executeBlock(Block<T> block) {
		StatementVisitorImpl statementVisitor = enclose().new StatementVisitorImpl();

		block.getStatements().map(s -> {
			s.accept(statementVisitor);
			return s;
		}).filter(s -> statementVisitor.isComplete()).findFirst();

		return (T) statementVisitor.getResult();
	}
}
