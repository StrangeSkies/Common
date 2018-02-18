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

import static java.util.Collections.emptySet;
import static uk.co.strangeskies.reflection.codegen.Modifiers.emptyModifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ParameterSignature<T> implements AnnotatedSignature<ParameterSignature<T>> {
	private final String variableName;
	private final AnnotatedType type;
	private final Set<Annotation> annotations;
	private final Modifiers modifiers;

	protected ParameterSignature(String variableName, AnnotatedType type) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = emptySet();
		this.modifiers = emptyModifiers();
	}

	protected ParameterSignature(
			String variableName,
			AnnotatedType type,
			Set<Annotation> annotations,
			Modifiers modifiers) {
		this.variableName = variableName;
		this.type = type;
		this.annotations = annotations;
		this.modifiers = modifiers;
	}

	public static ParameterSignature<?> parameterSignature(String variableName, AnnotatedType type) {
		return new ParameterSignature<>(variableName, type);
	}

	public static ParameterSignature<?> parameterSignature(String variableName, Type type) {
		return new ParameterSignature<>(variableName, AnnotatedTypes.annotated(type));
	}

	public static <U> ParameterSignature<U> parameterSignature(String variableName, Class<U> type) {
		return new ParameterSignature<>(variableName, AnnotatedTypes.annotated(type));
	}

	public static <U> ParameterSignature<U> parameterSignature(
			String variableName,
			TypeToken<U> type) {
		return new ParameterSignature<>(variableName, type.getAnnotatedDeclaration());
	}

	public static <U> ParameterSignature<U> parameterSignature(Parameter parameter) {
		return new ParameterSignature<U>(
				parameter.getName(),
				AnnotatedTypes.annotated(parameter.getParameterizedType()))
						.annotated(parameter.getDeclaredAnnotations())
						.withModifiers(Modifiers.modifiers(parameter.getModifiers()));
	}

	public static <U> ParameterSignature<U> overrideParameterSignature(
			ExecutableParameter parameter) {
		return new ParameterSignature<>(
				parameter.getName(),
				AnnotatedTypes.annotated(parameter.getType()));
	}

	public Modifiers getModifiers() {
		return modifiers;
	}

	private ParameterSignature<T> withModifiers(Modifiers modifiers) {
		return new ParameterSignature<>(variableName, type, annotations, modifiers);
	}

	public ParameterSignature<T> asFinal(boolean isFinal) {
		return withModifiers(modifiers);
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public ParameterSignature<T> annotated(Collection<? extends Annotation> annotations) {
		return new ParameterSignature<>(variableName, type, new HashSet<>(annotations), modifiers);
	}

	public String getVariableName() {
		return variableName;
	}

	public AnnotatedType getType() {
		return type;
	}

	public Class<?> getErasedType() {
		return Types.getErasedType(type.getType());
	}

	@Override
	public String toString() {
		return getType() + " " + getVariableName();
	}
}
