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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A collection of utility methods relating to type variables.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariables {
	private static final Type[] DEFAULT_UPPER_BOUND = new Type[] { Object.class };

	private TypeVariables() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends GenericDeclaration> TypeVariable<T> unboundedTypeVariable(T declaration, String name) {
		return new TypeVariable<T>() {
			private final Map<Class<? extends Annotation>, Annotation> annotations = new LinkedHashMap<>();

			@Override
			public Type[] getBounds() {
				return DEFAULT_UPPER_BOUND;
			}

			@Override
			public String toString() {
				return getName();
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof TypeVariable<?>))
					return false;
				if (that == this)
					return true;

				TypeVariable<?> typeVariable = (TypeVariable<?>) that;

				return Objects.equals(getName(), typeVariable.getName())
						&& Objects.equals(getGenericDeclaration(), typeVariable.getGenericDeclaration());
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getBounds()) ^ Objects.hashCode(getName()) ^ Objects.hashCode(getGenericDeclaration());
			}

			@Override
			public final <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				return (U) annotations.get(annotationClass);
			}

			@Override
			public final Annotation[] getAnnotations() {
				return annotations.values().toArray(new Annotation[annotations.size()]);
			}

			@Override
			public final Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}

			@Override
			public T getGenericDeclaration() {
				return declaration;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				return new AnnotatedType[0];
			}
		};
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> typeVariableExtending(
			T declaration,
			String name,
			AnnotatedType... bounds) {
		return typeVariableExtending(declaration, name, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> typeVariableExtending(
			T declaration,
			String name,
			Collection<? extends AnnotatedType> bounds) {
		return typeVariableExtending(declaration, name, Collections.emptySet(), bounds);
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param annotations
	 *          the annotations to be declared on the new type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> typeVariableExtending(
			T declaration,
			String name,
			Collection<Annotation> annotations,
			AnnotatedType... bounds) {
		return typeVariableExtending(declaration, name, annotations, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param declaration
	 *          the containing generic declaration
	 * @param name
	 *          the name of the type variable
	 * @param annotations
	 *          the annotations to be declared on the new type variable
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @param <T>
	 *          the type of the generic declaration
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static <T extends GenericDeclaration> TypeVariable<T> typeVariableExtending(
			T declaration,
			String name,
			Collection<Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		AnnotatedType[] annotatedBounds = bounds.toArray(new AnnotatedType[bounds.size()]);

		return new TypeVariable<T>() {
			private final Map<Class<? extends Annotation>, Annotation> annotations = new LinkedHashMap<>();

			@Override
			public Type[] getBounds() {
				return Arrays.stream(annotatedBounds).map(AnnotatedType::getType).toArray(Type[]::new);
			}

			@Override
			public String toString() {
				return getName();
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof TypeVariable<?>))
					return false;
				if (that == this)
					return true;

				TypeVariable<?> typeVariable = (TypeVariable<?>) that;

				return Objects.equals(getName(), typeVariable.getName())
						&& Objects.equals(getGenericDeclaration(), typeVariable.getGenericDeclaration());
			}

			@Override
			public int hashCode() {
				return Objects.hashCode(getName()) ^ Objects.hashCode(getGenericDeclaration());
			}

			@SuppressWarnings("unchecked")
			@Override
			public final <U extends Annotation> U getAnnotation(Class<U> annotationClass) {
				return (U) annotations.get(annotationClass);
			}

			@Override
			public final Annotation[] getAnnotations() {
				return annotations.values().toArray(new Annotation[annotations.size()]);
			}

			@Override
			public final Annotation[] getDeclaredAnnotations() {
				return getAnnotations();
			}

			@Override
			public T getGenericDeclaration() {
				return declaration;
			}

			@Override
			public String getName() {
				return name;
			}

			@Override
			public AnnotatedType[] getAnnotatedBounds() {
				return annotatedBounds;
			}
		};
	}
}
