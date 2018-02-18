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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * A collection of utility methods relating to array types.
 * 
 * @author Elias N Vasylenko
 */
public class ArrayTypes {
	private static class GenericArrayTypeImpl implements GenericArrayType {
		private final Type component;

		public GenericArrayTypeImpl(Type component) {
			this.component = component;
		}

		@Override
		public Type getGenericComponentType() {
			return component;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder(Types.toString(getGenericComponentType()));
			if (getGenericComponentType() instanceof IntersectionType)
				builder.append(" ");
			return builder.append("[]").toString();
		}

		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (object == null || !(object instanceof GenericArrayType))
				return false;

			GenericArrayType that = (GenericArrayType) object;

			return component.equals(that.getGenericComponentType());
		}

		@Override
		public int hashCode() {
			return component.hashCode();
		}
	}

	private ArrayTypes() {}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(GenericArrayType component) {
		return fromGenericComponentType(component);
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(GenericArrayType component, int arrayDimensions) {
		GenericArrayType array;

		do {
			component = array = arrayFromComponent(component);
		} while (--arrayDimensions > 0);

		return array;
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(ParameterizedType component) {
		return fromGenericComponentType(component);
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(ParameterizedType component, int arrayDimensions) {
		GenericArrayType array = arrayFromComponent(component);

		while (--arrayDimensions > 0) {
			array = arrayFromComponent(array);
		}

		return array;
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(TypeVariable<?> component) {
		return fromGenericComponentType(component);
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(TypeVariable<?> component, int arrayDimensions) {
		GenericArrayType array = arrayFromComponent(component);

		while (--arrayDimensions > 0) {
			array = arrayFromComponent(array);
		}

		return array;
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(InferenceVariable component) {
		return fromGenericComponentType(component);
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(InferenceVariable component, int arrayDimensions) {
		GenericArrayType array = arrayFromComponent(component);

		while (--arrayDimensions > 0) {
			array = arrayFromComponent(array);
		}

		return array;
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(IntersectionType component) {
		return fromGenericComponentType(component);
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} derived from a given
	 * component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static GenericArrayType arrayFromComponent(IntersectionType component, int arrayDimensions) {
		GenericArrayType array = arrayFromComponent(component);

		while (--arrayDimensions > 0) {
			array = arrayFromComponent(array);
		}

		return array;
	}

	/**
	 * Obtain a reference to an array {@link Class} derived from a given component
	 * type.
	 * 
	 * @param <T>
	 *          The type of the component of the array.
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T[]> arrayFromComponent(Class<T> component) {
		return (Class<T[]>) Array.newInstance(component, 0).getClass();
	}

	/**
	 * Obtain a reference to an array {@link Class} derived from a given component
	 * type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static Class<?> arrayFromComponent(Class<?> component, int arrayDimensions) {
		Class<?> array;

		do {
			component = array = arrayFromComponent(component);
		} while (--arrayDimensions > 0);

		return array;
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} or array {@link Class}
	 * derived from a given component type.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @return A generic array type whose component type is the given type.
	 */
	public static Type arrayFromComponent(Type component) {
		if (component instanceof Class)
			return arrayFromComponent((Class<?>) component);
		else if (component instanceof GenericArrayType)
			return arrayFromComponent((GenericArrayType) component);
		else if (component instanceof ParameterizedType)
			return arrayFromComponent((ParameterizedType) component);
		else if (component instanceof TypeVariable)
			return arrayFromComponent((TypeVariable<?>) component);
		else if (component instanceof InferenceVariable)
			return arrayFromComponent((InferenceVariable) component);
		else if (component instanceof IntersectionType)
			return arrayFromComponent((IntersectionType) component);
		else
			throw new IllegalArgumentException("Given type '" + component + "' is not a valid array component.");
	}

	/**
	 * Obtain a reference to a {@link GenericArrayType} or array {@link Class}
	 * derived from a given component type, with the given number of dimensions.
	 * 
	 * @param component
	 *          The component type of the generic array type to be created.
	 * @param arrayDimensions
	 *          The number of dimensions to create over the given component.
	 * @return A generic array type whose component type is the given type.
	 */
	public static Type arrayFromComponent(Type component, int arrayDimensions) {
		while (arrayDimensions-- > 0) {
			component = arrayFromComponent(component);
		}

		return component;
	}

	private static GenericArrayType fromGenericComponentType(Type component) {
		return new GenericArrayTypeImpl(component);
	}
}
