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
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;
import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeInternal;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to annotated array types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedArrayTypes {
	@SuppressWarnings("javadoc")
	public static interface AnnotatedArrayTypeInternal extends AnnotatedArrayType, AnnotatedTypeInternal {}

	private static class AnnotatedArrayTypeImpl extends AnnotatedTypeImpl implements AnnotatedArrayTypeInternal {
		private final AnnotatedTypeInternal annotatedComponentType;

		public AnnotatedArrayTypeImpl(Isomorphism isomorphism, AnnotatedArrayType annotatedArrayType) {
			super(annotatedArrayType);

			annotatedComponentType = AnnotatedTypes
					.wrapImpl(isomorphism, annotatedArrayType.getAnnotatedGenericComponentType());
		}

		public AnnotatedArrayTypeImpl(Isomorphism isomorphism, GenericArrayType type, Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedComponentType = AnnotatedTypes.annotatedImpl(isomorphism, type.getGenericComponentType());
		}

		public AnnotatedArrayTypeImpl(Isomorphism isomorphism, Class<?> type, Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedComponentType = AnnotatedTypes.annotatedImpl(isomorphism, type.getComponentType());
		}

		public AnnotatedArrayTypeImpl(Isomorphism isomorphism, AnnotatedType type, Collection<Annotation> annotations) {
			super(ArrayTypes.arrayFromComponent(type.getType()), annotations);

			annotatedComponentType = AnnotatedTypes.wrapImpl(isomorphism, type);
		}

		@Override
		public AnnotatedType getAnnotatedGenericComponentType() {
			return annotatedComponentType;
		}

		@Override
		public String toString(Imports imports) {
			StringBuilder builder = new StringBuilder();

			AnnotatedType type = this;
			do {
				if (type.getAnnotations().length > 0) {
					builder.append(" ").append(annotationString(imports, type.getAnnotations()));
				}

				builder.append("[]");

				type = ((AnnotatedArrayType) type).getAnnotatedGenericComponentType();
			} while (type instanceof AnnotatedArrayType);

			return builder.insert(0, AnnotatedTypes.wrapImpl(type).toString(imports)).toString();
		}

		@Override
		public int annotationHashImpl() {
			return super.annotationHashImpl() ^ annotationHash(annotatedComponentType);
		}
	}

	private AnnotatedArrayTypes() {}

	/**
	 * Derive a new {@link AnnotatedArrayType} instance from a given annotated
	 * component type, and the given annotations.
	 * 
	 * @param component
	 *          The annotated component type of the new annotated array.
	 * @param annotations
	 *          The annotations for the new annotated array type.
	 * @return A new annotated array type with the given component and given
	 *         annotations.
	 */
	public static AnnotatedArrayType arrayFromComponent(AnnotatedType component, Annotation... annotations) {
		return arrayFromComponent(component, Arrays.asList(annotations));
	}

	/**
	 * Derive a new {@link AnnotatedArrayType} instance from a given annotated
	 * component type, and the given annotations.
	 * 
	 * @param component
	 *          The annotated component type of the new annotated array.
	 * @param annotations
	 *          The annotations for the new annotated array type.
	 * @return A new annotated array type with the given component and given
	 *         annotations.
	 */
	public static AnnotatedArrayType arrayFromComponent(AnnotatedType component, Collection<Annotation> annotations) {
		return new AnnotatedArrayTypeImpl(new Isomorphism(), component, annotations);
	}

	/**
	 * Create a new annotated representation of a given generic array type.
	 * 
	 * @param arrayType
	 *          The array type to be annotated.
	 * @param annotations
	 *          The annotations for the annotated array type.
	 * @return A new annotated array type over the given array type and with the
	 *         given annotations.
	 */
	public static AnnotatedArrayType over(GenericArrayType arrayType, Annotation... annotations) {
		return over(arrayType, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated representation of a given generic array type.
	 * 
	 * @param arrayType
	 *          The array type to be annotated.
	 * @param annotations
	 *          The annotations for the annotated array type.
	 * @return A new annotated array type over the given array type and with the
	 *         given annotations.
	 */
	public static AnnotatedArrayType over(GenericArrayType arrayType, Collection<Annotation> annotations) {
		return overImpl(new Isomorphism(), arrayType, annotations);
	}

	static AnnotatedArrayTypeInternal overImpl(
			Isomorphism isomorphism,
			GenericArrayType arrayType,
			Collection<Annotation> annotations) {
		if (annotations.isEmpty()) {
			return isomorphism.byIdentity().getProxiedMapping(
					arrayType,
					AnnotatedArrayTypeInternal.class,
					t -> new AnnotatedArrayTypeImpl(isomorphism, t, annotations));
		} else {
			return new AnnotatedArrayTypeImpl(isomorphism, arrayType, annotations);
		}
	}

	/**
	 * Create a new annotated representation of a given array type.
	 * 
	 * @param arrayType
	 *          The array type to be annotated.
	 * @param annotations
	 *          The annotations for the annotated array type.
	 * @return A new annotated array type over the given array type and with the
	 *         given annotations.
	 */
	public static AnnotatedArrayType over(Class<?> arrayType, Annotation... annotations) {
		return over(arrayType, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated representation of a given array type.
	 * 
	 * @param arrayType
	 *          The array type to be annotated.
	 * @param annotations
	 *          The annotations for the annotated array type.
	 * @return A new annotated array type over the given array type and with the
	 *         given annotations.
	 */
	public static AnnotatedArrayType over(Class<?> arrayType, Collection<Annotation> annotations) {
		return overImpl(new Isomorphism(), arrayType, annotations);
	}

	static AnnotatedArrayTypeInternal overImpl(
			Isomorphism isomorphism,
			Class<?> arrayType,
			Collection<Annotation> annotations) {
		if (annotations.isEmpty()) {
			return isomorphism.byIdentity().getProxiedMapping(
					arrayType,
					AnnotatedArrayTypeInternal.class,
					t -> new AnnotatedArrayTypeImpl(isomorphism, t, annotations));
		} else {
			return new AnnotatedArrayTypeImpl(isomorphism, arrayType, annotations);
		}
	}

	protected static AnnotatedArrayTypeInternal wrapImpl(AnnotatedArrayType type) {
		return wrapImpl(new Isomorphism(), type);
	}

	protected static AnnotatedArrayTypeInternal wrapImpl(Isomorphism isomorphism, AnnotatedArrayType type) {
		if (type instanceof AnnotatedArrayTypeInternal) {
			return (AnnotatedArrayTypeInternal) type;
		} else
			return isomorphism.byIdentity().getProxiedMapping(
					type,
					AnnotatedArrayTypeInternal.class,
					t -> new AnnotatedArrayTypeImpl(isomorphism, t));
	}

	/**
	 * Wrap an existing annotated array type.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedArrayType} which is equal to the
	 *         given type.
	 */
	public static AnnotatedArrayType wrap(AnnotatedArrayType type) {
		return wrapImpl(type);
	}
}
