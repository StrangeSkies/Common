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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.codegen.Modifiers.modifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

public class MethodSignature<T> extends ExecutableSignature<MethodSignature<T>> {
	public static MethodSignature<Void> methodSignature(String methodName) {
		return new MethodSignature<>(methodName);
	}

	public static MethodSignature<?> methodSignature(Method method) {
		return new MethodSignature<>(method.getName())
				.annotated(method.getAnnotations())
				.withModifiers(modifiers(method.getModifiers()))
				.withReturnType(method.getReturnType())
				.asDefault(method.isDefault())
				.typeVariables(
						stream(method.getTypeParameters())
								.map(TypeVariableSignature::typeVariableSignature)
								.collect(toList()))
				.withParameters(
						stream(method.getParameters()).map(ParameterSignature::parameterSignature).collect(
								toList()));
	}

	public static MethodSignature<?> overrideMethodSignature(ExecutableToken<?, ?> method) {
		return new MethodSignature<>(method.getName())
				.annotated(method.getMember().getAnnotations())
				.withModifiers(modifiers(method.getMember().getModifiers()).withAbstract(false))
				.withReturnType(method.getReturnType().getType())
				.asDefault(((Method) method.getMember()).isDefault())
				.typeVariables(
						method
								.getTypeParameters()
								.map(TypeParameter::getType)
								.map(TypeVariableSignature::typeVariableSignature)
								.collect(toList()))
				.withParameters(
						method.getParameters().map(ParameterSignature::overrideParameterSignature).collect(
								toList()));
	}

	private final AnnotatedType returnType;

	protected MethodSignature(String methodName) {
		super(methodName);

		this.returnType = AnnotatedTypes.annotated(void.class);
	}

	protected MethodSignature(
			String name,
			Set<Annotation> annotations,
			Modifiers modifiers,
			List<ParameterSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables,
			AnnotatedType returnType) {
		super(name, annotations, modifiers, parameters, typeVariables);

		this.returnType = returnType;
	}

	@Override
	protected MethodSignature<T> withExecutableSignatureData(
			String name,
			Set<Annotation> annotations,
			Modifiers modifiers,
			List<ParameterSignature<?>> parameters,
			List<TypeVariableSignature> typeVariables) {
		return new MethodSignature<>(
				name,
				annotations,
				modifiers,
				parameters,
				typeVariables,
				returnType);
	}

	public MethodSignature<T> asStatic(boolean isStatic) {
		return withModifiers(modifiers.withStatic(isStatic));
	}

	public MethodSignature<T> asFinal(boolean isFinal) {
		return withModifiers(modifiers.withFinal(isFinal));
	}

	public MethodSignature<T> asAbstract(boolean isAbstract) {
		return withModifiers(modifiers.withAbstract(isAbstract));
	}

	public MethodSignature<T> asSynchronized(boolean isSynchronized) {
		return withModifiers(modifiers.withSynchronized(isSynchronized));
	}

	public MethodSignature<T> asStrict(boolean isStrict) {
		return withModifiers(modifiers.withStrict(isStrict));
	}

	public MethodSignature<T> asNative(boolean isNative) {
		return withModifiers(modifiers.withNative(isNative));
	}

	public MethodSignature<T> asDefault(boolean isDefault) {
		return withModifiers(modifiers.withDefault(isDefault));
	}

	@Override
	public AnnotatedType getReturnType() {
		return returnType;
	}

	public MethodSignature<?> withReturnType(AnnotatedType returnType) {
		return new MethodSignature<>(
				name,
				annotations,
				modifiers,
				parameters,
				typeVariables,
				returnType);
	}

	public MethodSignature<?> withReturnType(Type returnType) {
		return withReturnType(AnnotatedTypes.annotated(returnType));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(Class<U> returnType) {
		return (MethodSignature<U>) withReturnType(AnnotatedTypes.annotated(returnType));
	}

	@SuppressWarnings("unchecked")
	public <U> MethodSignature<U> withReturnType(TypeToken<U> returnType) {
		return (MethodSignature<U>) withReturnType(returnType.getAnnotatedDeclaration());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		ParameterizedSignature.appendTypeParametersTo(this, builder);
		builder.append(' ').append(getReturnType()).append(' ').append(getName());
		appendParameters(builder);

		return builder.toString();
	}
}
