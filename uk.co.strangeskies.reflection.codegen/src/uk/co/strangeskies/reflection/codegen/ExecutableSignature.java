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
package uk.co.strangeskies.reflection.codegen;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedMethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ExecutableSignature<S extends ExecutableSignature<S>>
		extends MemberSignature<S> implements ParameterizedSignature<S> {
	final List<ParameterSignature<?>> parameters;
	final List<TypeVariableSignature> typeVariables;

	ErasedMethodSignature erasedSignature;

	protected ExecutableSignature(String name) {
		super(name);

		this.parameters = emptyList();
		this.typeVariables = emptyList();
	}

	protected ExecutableSignature(String name, Set<Annotation> annotations, Modifiers modifiers,
			List<ParameterSignature<?>> parameters, List<TypeVariableSignature> typeVariables) {
		super(name, annotations, modifiers);

		this.parameters = parameters;
		this.typeVariables = typeVariables;
	}

	public ErasedMethodSignature erased() {
		if (erasedSignature == null) {
			this.erasedSignature = erasedMethodSignature(getName(),
					parameters.stream().map(v -> getErasedType(v.getType().getType())).toArray(
							Class<?>[]::new));
		}
		return erasedSignature;
	}

	@Override
	protected S withMemberSignatureData(String name, Set<Annotation> annotations,
			Modifiers modifiers) {
		return withExecutableSignatureData(name, annotations, modifiers, parameters, typeVariables);
	}

	protected abstract S withExecutableSignatureData(String name, Set<Annotation> annotations,
			Modifiers modifiers, List<ParameterSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables);

	@Override
	public Stream<? extends TypeVariableSignature> getTypeVariables() {
		return typeVariables.stream();
	}

	@Override
	public S typeVariables(Collection<? extends TypeVariableSignature> typeVariables) {
		return withExecutableSignatureData(name, annotations, modifiers, parameters,
				new ArrayList<>(typeVariables));
	}

	public abstract AnnotatedType getReturnType();

	public Stream<? extends ParameterSignature<?>> getParameters() {
		return parameters.stream();
	}

	public S withParameters(ParameterSignature<?>... parameters) {
		return withParameters(asList(parameters));
	}

	public S withParameters(Collection<? extends ParameterSignature<?>> parameters) {
		return withExecutableSignatureData(name, annotations, modifiers, new ArrayList<>(parameters),
				typeVariables);
	}

	protected void appendParameters(StringBuilder builder) {
		builder
				.append("(")
				.append(getParameters().map(Objects::toString).collect(joining(", ")))
				.append(")");
	}
}
