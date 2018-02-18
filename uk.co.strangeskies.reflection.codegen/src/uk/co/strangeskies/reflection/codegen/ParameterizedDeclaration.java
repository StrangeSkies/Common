/*
 * Copyright (C) 2018 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
/*
s * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.Types.getErasedType;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.TypeVariable;
import java.util.stream.Collectors;

import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class ParameterizedDeclaration<S extends ParameterizedSignature<?>>
		extends AnnotatedDeclaration<S> {
	public ParameterizedDeclaration(S signature, SignatureWriter writer) {
		super(signature);

		signature.getTypeVariables().forEach(typeVariable -> {
			validateBounds(typeVariable);
			writeTypeVariable(typeVariable, writer);
		});
	}

	private void writeTypeVariable(TypeVariableSignature typeVariable, SignatureWriter writer) {
		writer.visitFormalTypeParameter(typeVariable.getName());

		if (typeVariable.getBounds().count() == 0) {
			SignatureVisitor classBoundWriter = writer.visitClassBound();
			ClassWritingContext.visitTypeSignature(classBoundWriter, Object.class);
		} else {
			typeVariable
					.getBounds()
					.filter(
							b -> b.getType() instanceof TypeVariable<?>
									|| !getErasedType(b.getType()).isInterface())
					.reduce(throwingReduce())
					.ifPresent(bound -> {
						SignatureVisitor classBoundWriter = writer.visitClassBound();
						ClassWritingContext.visitTypeSignature(classBoundWriter, bound.getType());
					});

			typeVariable
					.getBounds()
					.filter(
							b -> !(b.getType() instanceof TypeVariable<?>)
									&& getErasedType(b.getType()).isInterface())
					.forEach(bound -> {
						SignatureVisitor interfaceBoundWriter = writer.visitInterfaceBound();
						ClassWritingContext.visitTypeSignature(interfaceBoundWriter, bound.getType());
					});
		}
	}

	private void validateBounds(TypeVariableSignature typeVariable) {
		intersectionOf(
				typeVariable.getBounds().map(AnnotatedType::getType).collect(Collectors.toList()));
	}

	/**
	 * @return true if the declaration has type parameters, false otherwise
	 */
	public boolean isParameterized() {
		return getSignature().getTypeVariables().count() > 0;
	}
}
