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

import static java.util.Arrays.asList;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to parameterized types.
 * 
 * @author Elias N Vasylenko
 */
public class ParameterizedTypes {
	static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
		private static final long serialVersionUID = 1L;

		private static final Integer PROCESSING = new Integer(0);

		private final Type ownerType;
		private final List<Type> typeArguments;
		private final Class<?> rawType;

		private Integer hashCode;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, List<Type> typeArguments) {
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.typeArguments = typeArguments;

			int i = 0;
			for (Type argument : typeArguments) {
				if (argument instanceof IntersectionType) {
					IntersectionType intersectionType = (IntersectionType) argument;
					if (intersectionType.getTypes().length == 0)
						typeArguments.set(i, Object.class);
					else if (intersectionType.getTypes().length == 1)
						typeArguments.set(i, intersectionType.getTypes()[0]);
				}
				i++;
			}
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments.toArray(new Type[typeArguments.size()]);
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public String toString() {
			return toString(Imports.empty());
		}

		public synchronized String toString(Imports imports) {
			return ParameterizedTypes.toString(this, imports);
		}

		@Override
		public int hashCode() {
			if (hashCode == null || hashCode == PROCESSING) {
				synchronized (this) {
					/*
					 * This way the hash code will return 0 if we encounter it again in
					 * the parameters, rather than recurring infinitely:
					 * 
					 * (this is not a problem for other threads as hashCode is
					 * synchronized until given a proper result)
					 */
					hashCode = PROCESSING;

					/*
					 * Calculate the hash code properly, now we're guarded against
					 * recursion:
					 */
					this.hashCode = Objects.hashCode(ownerType)
							^ Objects.hashCode(rawType)
							^ Objects.hashCode(typeArguments);
				}
			}

			return hashCode;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof Type))
				return false;
			else
				return Types.equals(this, (Type) other);
		}
	}

	private ParameterizedTypes() {}

	/**
	 * Format a string describing a given parameterized type, assuming the given
	 * {@link Imports imports}.
	 * 
	 * @param parameterizedType
	 *          the type we wish to express as a string
	 * @param imports
	 *          the imports with which to inform qualification omission
	 * @return a string representation of the given parameterized type
	 */
	public static String toString(ParameterizedType parameterizedType, Imports imports) {
		return toString(parameterizedType, imports, new Isomorphism());
	}

	/**
	 * Format a string describing a given parameterized type, assuming the given
	 * {@link Imports imports} and using the given {@link Isomorphism} to skip
	 * recurring types.
	 * 
	 * @param parameterizedType
	 *          the type we wish to express as a string
	 * @param imports
	 *          the imports with which to inform qualification omission
	 * @param isomorphism
	 *          the isomorphism with which to skip repeated types
	 * @return a string representation of the given parameterized type
	 */
	public static String toString(
			ParameterizedType parameterizedType,
			Imports imports,
			Isomorphism isomorphism) {
		return isomorphism.byIdentity().getPartialMapping(parameterizedType, (p, partial) -> {
			/*
			 * This way the string will return "..." if we encounter it again in the
			 * parameters, rather than recurring infinitely:
			 */
			partial.accept(() -> "...");

			Type ownerType = p.getOwnerType();
			Type rawType = p.getRawType();
			Type[] typeArguments = p.getActualTypeArguments();

			/*
			 * Calculate the string properly, now we're guarded against recursion:
			 */
			StringBuilder builder = new StringBuilder();
			if (ownerType == null) {
				builder.append(Types.toString(rawType, imports, isomorphism));
			} else {
				builder.append(Types.toString(ownerType, imports, isomorphism)).append(".");

				if (ownerType instanceof ParameterizedType) {
					String rawTypeName = rawType.getTypeName();
					int index = rawTypeName.indexOf('$');
					if (index > 0) {
						rawTypeName = rawTypeName.substring(index + 1);
					}
					builder.append(rawTypeName);
				} else {
					builder.append(Types.toString(rawType, imports, isomorphism));
				}
			}

			builder.append('<');

			builder.append(
					Arrays.stream(typeArguments).map(t -> Types.toString(t, imports, isomorphism)).collect(
							Collectors.joining(", ")));

			return builder.append('>').toString();
		});
	}

	/**
	 * This method retrieves a list of all type variables present on the given raw
	 * type, as well as all type variables on any enclosing types recursively, in
	 * the order encountered.
	 *
	 * @param rawType
	 *          The class whose generic type parameters we wish to determine.
	 * @return A list of all relevant type variables.
	 */
	public static Stream<TypeVariable<?>> getAllTypeParameters(Class<?> rawType) {
		Stream<TypeVariable<?>> typeParameters = Stream.empty();

		do {
			typeParameters = Stream.concat(Arrays.stream(rawType.getTypeParameters()), typeParameters);
		} while (!Types.isStatic(rawType) && (rawType = rawType.getEnclosingClass()) != null);

		return typeParameters;
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by {@link #getAllTypeParameters(Class)}, to their
	 * arguments within the context of this type.
	 *
	 * @param type
	 *          The type whose generic type arguments we wish to determine.
	 * @return A mapping of all type variables to their arguments in the context
	 *         of the given type.
	 */
	public static Stream<Map.Entry<TypeVariable<?>, Type>> getAllTypeArguments(
			ParameterizedType type) {
		Stream<Entry<TypeVariable<?>, Type>> typeArguments = Stream.empty();

		Class<?> rawType = (Class<?>) type.getRawType();
		do {
			TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
			Type[] actualTypeArguments = type.getActualTypeArguments();

			typeArguments = Stream.concat(
					IntStream.range(0, typeParameters.length).mapToObj(
							i -> new AbstractMap.SimpleEntry<>(typeParameters[i], actualTypeArguments[i])),
					typeArguments);

			type = type.getOwnerType() instanceof ParameterizedType
					? (ParameterizedType) type.getOwnerType()
					: null;
			rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass();

			if (rawType != null && type == null) {
				do {
					typeArguments = Stream.concat(
							typeArguments,
							Arrays.stream(rawType.getTypeParameters()).map(
									p -> new AbstractMap.SimpleEntry<>(p, p)));
				} while ((rawType = Types.isStatic(rawType) ? null : rawType.getEnclosingClass()) != null);
			}
		} while (type != null && rawType != null);

		return typeArguments;
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments and owning type. Type parameters
	 * with no provided argument will be parameterized with the type variables
	 * themselves.
	 * 
	 * @param ownerType
	 *          the owner type for the resulting parameterized type
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @param typeArguments
	 *          A mapping of generic type variables to arguments.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments.
	 */
	public static ParameterizedType parameterizeUnchecked(
			Type ownerType,
			Class<?> rawType,
			List<Type> typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, new ArrayList<>(typeArguments));
	}

	/**
	 * As @see {@link #parameterize(Class, Type[])}, but without checking type
	 * arguments for consistency.
	 */
	@SuppressWarnings("javadoc")
	public static ParameterizedType parameterizeUnchecked(Class<?> rawType, Type... typeArguments) {
		return parameterizeUnchecked(rawType, Arrays.asList(typeArguments));
	}

	/**
	 * As @see {@link #parameterize(Class, List)}, but without checking type
	 * arguments for consistency.
	 */
	@SuppressWarnings("javadoc")
	public static ParameterizedType parameterizeUnchecked(
			Class<?> rawType,
			List<Type> typeArguments) {
		List<TypeVariable<?>> parameters = getAllTypeParameters(rawType).collect(Collectors.toList());

		if (parameters.size() != typeArguments.size()) {
			throw new ReflectionException(
					REFLECTION_PROPERTIES
							.incorrectTypeArgumentCount(asList(rawType.getTypeParameters()), typeArguments));
		}

		return parameterizeUncheckedImpl(rawType, typeArguments);
	}

	private static ParameterizedType parameterizeUncheckedImpl(
			Class<?> rawType,
			List<Type> typeArguments) {
		int totalArgumentCount = typeArguments.size();
		int parametersOnTypeCount = rawType.getTypeParameters().length;
		int parametersOnOwnerCount = totalArgumentCount - parametersOnTypeCount;

		Type owner = rawType.getEnclosingClass();

		if (totalArgumentCount > parametersOnTypeCount) {
			owner = parameterizeUncheckedImpl(
					(Class<?>) owner,
					typeArguments.subList(0, parametersOnOwnerCount));

			typeArguments = typeArguments.subList(parametersOnOwnerCount, totalArgumentCount);
		}

		return parameterizeUnchecked(owner, rawType, typeArguments);
	}

	/**
	 * As @see {@link #parameterize(Class, Function)}, but without checking type
	 * arguments for consistency.
	 */
	@SuppressWarnings("javadoc")
	public static <T> ParameterizedType parameterizeUnchecked(
			Class<T> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		return (ParameterizedType) parameterizeUncheckedImpl(rawType, typeArguments);
	}

	private static <T> Type parameterizeUncheckedImpl(
			Class<T> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		Class<?> enclosing = rawType.getEnclosingClass();
		Type ownerType;
		if (enclosing == null || Types.isStatic(rawType)) {
			ownerType = enclosing;
		} else {
			ownerType = parameterizeUncheckedImpl(enclosing, argumentsForOwner(rawType, typeArguments));
		}

		if ((ownerType == null || ownerType instanceof Class)
				&& rawType.getTypeParameters().length == 0)
			return rawType;

		return new ParameterizedTypeImpl(ownerType, rawType, argumentsForClass(rawType, typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class},
	 * substituting the type parameters of that class as their own argument
	 * instantiations.
	 * 
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @return A {@link ParameterizedType} instance over the given class.
	 */
	public static ParameterizedType parameterize(Class<?> rawType) {
		return parameterizeUnchecked(rawType, i -> null);
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments. Type parameters with no provided
	 * argument will be parameterized with the type variables themselves.
	 * 
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}.
	 * @param typeArguments
	 *          A mapping of generic type variables to arguments.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments.
	 */
	public static ParameterizedType parameterize(
			Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		return validate(parameterizeUnchecked(rawType, typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments, in the order given.
	 * 
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param typeArguments
	 *          A list of {@link Type}s to substitute as type arguments for the
	 *          given generic class. There should be exactly as many type
	 *          arguments as there are type parameters for the given class.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments, in order
	 */
	public static ParameterizedType parameterize(Class<?> rawType, Type... typeArguments) {
		return parameterize(rawType, Arrays.asList(typeArguments));
	}

	/**
	 * Derive an instance of {@link ParameterizedType} from a raw {@link Class}
	 * using the given generic type arguments, in the order given.
	 * 
	 * @param rawType
	 *          A raw {@link Class} from which we wish to determine a
	 *          {@link ParameterizedType}. The raw type must not have a non
	 *          statically enclosing type which is itself generic.
	 * @param typeArguments
	 *          A list of {@link Type}s to substitute as type arguments for the
	 *          given generic class. There should be exactly as many type
	 *          arguments as there are type parameters for the given class.
	 * @return A {@link ParameterizedType} instance over the given class,
	 *         parameterized with the given type arguments, in order
	 */
	public static ParameterizedType parameterize(Class<?> rawType, List<Type> typeArguments) {
		return validate(parameterizeUnchecked(rawType, typeArguments));
	}

	protected static ParameterizedType validate(ParameterizedType type) {
		// TODO validation of ParameterizedTypeImpl parameters
		return type;
	}

	/*
	 * An enclosing type of a method local or constructor local type, such as an
	 * anonymous type, cannot be parameterized.
	 */
	private static Function<? super TypeVariable<?>, ? extends Type> argumentsForOwner(
			Class<?> enclosedClass,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {

		if (enclosedClass.getEnclosingConstructor() != null
				|| enclosedClass.getEnclosingMethod() != null) {
			return t -> {
				Type argument = typeArguments.apply(t);
				if (argument != t && argument != null) {
					throw new ReflectionException(
							REFLECTION_PROPERTIES.cannotParameterizeEnclosingExecutable(enclosedClass));
				}
				return argument;
			};

		} else {
			return typeArguments;
		}
	}

	private static List<Type> argumentsForClass(
			Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends Type> typeArguments) {
		List<Type> arguments = new ArrayList<>();
		for (int i = 0; i < rawType.getTypeParameters().length; i++) {
			Type argument = typeArguments.apply(rawType.getTypeParameters()[i]);
			arguments.add((argument != null) ? argument : rawType.getTypeParameters()[i]);
		}
		return arguments;
	}
}
