/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.reflection.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;

/**
 * A collection of utility methods relating to annotated array types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedArrayTypes {
	private static class AnnotatedArrayTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedArrayType {
		private final AnnotatedType annotatedComponentType;

		public AnnotatedArrayTypeImpl(GenericArrayType type,
				Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedComponentType = AnnotatedTypes.over(type
					.getGenericComponentType());
		}

		public AnnotatedArrayTypeImpl(Class<?> type,
				Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedComponentType = AnnotatedTypes.over(type.getComponentType());
		}

		public AnnotatedArrayTypeImpl(AnnotatedType type,
				Collection<Annotation> annotations) {
			super(ArrayTypes.fromComponentType(type.getType()), annotations);

			annotatedComponentType = type;
		}

		@Override
		public AnnotatedType getAnnotatedGenericComponentType() {
			return annotatedComponentType;
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
	public static AnnotatedArrayType fromComponent(AnnotatedType component,
			Annotation... annotations) {
		return fromComponent(component, Arrays.asList(annotations));
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
	public static AnnotatedArrayType fromComponent(AnnotatedType component,
			Collection<Annotation> annotations) {
		return new AnnotatedArrayTypeImpl(component, annotations);
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
	public static AnnotatedArrayType over(GenericArrayType arrayType,
			Annotation... annotations) {
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
	public static AnnotatedArrayType over(GenericArrayType arrayType,
			Collection<Annotation> annotations) {
		return new AnnotatedArrayTypeImpl(arrayType, annotations);
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
	public static AnnotatedArrayType over(Class<?> arrayType,
			Annotation... annotations) {
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
	public static AnnotatedArrayType over(Class<?> arrayType,
			Collection<Annotation> annotations) {
		return new AnnotatedArrayTypeImpl(arrayType, annotations);
	}
}
