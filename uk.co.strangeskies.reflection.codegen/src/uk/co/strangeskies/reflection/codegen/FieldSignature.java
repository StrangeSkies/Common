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
import static uk.co.strangeskies.reflection.codegen.Modifiers.modifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.token.TypeToken;

public class FieldSignature<T> extends MemberSignature<FieldSignature<T>> {
	private final AnnotatedType type;
	private final Set<Annotation> annotations;

	protected FieldSignature(String variableName, AnnotatedType type) {
		super(variableName);

		this.type = type;
		this.annotations = emptySet();
	}

	protected FieldSignature(
			String variableName,
			Set<Annotation> annotations,
			Modifiers modifiers,
			AnnotatedType type) {
		super(variableName, annotations, modifiers);

		this.type = type;
		this.annotations = annotations;
	}

	@Override
	protected FieldSignature<T> withMemberSignatureData(
			String variableName,
			Set<Annotation> annotations,
			Modifiers modifiers) {
		return new FieldSignature<>(variableName, annotations, modifiers, type);
	}

	public static
			FieldSignature<?>
			fieldSignature(String variableName, AnnotatedType type) {
		return new FieldSignature<>(variableName, type);
	}

	public static
			FieldSignature<?>
			fieldSignature(String variableName, Type type) {
		return new FieldSignature<>(variableName, AnnotatedTypes.annotated(type));
	}

	public static <
			U> FieldSignature<U> fieldSignature(String variableName, Class<U> type) {
		return new FieldSignature<>(variableName, AnnotatedTypes.annotated(type));
	}

	public static <U> FieldSignature<U> fieldSignature(
			String variableName,
			TypeToken<U> type) {
		return new FieldSignature<>(variableName, type.getAnnotatedDeclaration());
	}

	public static <U> FieldSignature<U> fieldSignature(Field field) {
		return new FieldSignature<U>(
				field.getName(),
				AnnotatedTypes.annotated(field.getType()))
						.annotated(field.getAnnotations())
						.withModifiers(modifiers(field.getModifiers()));
	}

	public FieldSignature<T> asStatic(boolean isStatic) {
		return withModifiers(modifiers.withStatic(isStatic));
	}

	public FieldSignature<T> asFinal(boolean isFinal) {
		return withModifiers(modifiers.withFinal(isFinal));
	}

	public FieldSignature<T> asTransient(boolean isTransient) {
		return withModifiers(modifiers.withTransient(isTransient));
	}

	public FieldSignature<T> asVolatile(boolean isVolatile) {
		return withModifiers(modifiers.withVolatile(isVolatile));
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public FieldSignature<T> annotated(
			Collection<? extends Annotation> annotations) {
		return new FieldSignature<>(
				name,
				new HashSet<>(annotations),
				modifiers,
				type);
	}

	public AnnotatedType getType() {
		return type;
	}

	@Override
	public String toString() {
		return getType() + " " + getName();
	}
}
