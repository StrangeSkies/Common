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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;
import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeInternal;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to annotated parameterised types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedParameterizedTypes {
	@SuppressWarnings("javadoc")
	public static interface AnnotatedParameterizedTypeInternal
			extends AnnotatedParameterizedType, AnnotatedTypeInternal {}

	private static class AnnotatedParameterizedTypeImpl extends AnnotatedTypeImpl
			implements AnnotatedParameterizedTypeInternal {
		private String string;
		private final AnnotatedTypeInternal[] annotatedTypeArguments;

		public AnnotatedParameterizedTypeImpl(
				Isomorphism isomorphism,
				AnnotatedParameterizedType annotatedParameterizedType) {
			super(annotatedParameterizedType);

			annotatedTypeArguments = AnnotatedTypes
					.wrapImpl(isomorphism, annotatedParameterizedType.getAnnotatedActualTypeArguments());
		}

		public AnnotatedParameterizedTypeImpl(
				Isomorphism isomorphism,
				ParameterizedType type,
				Collection<? extends Annotation> annotations) {
			super(type, annotations);

			annotatedTypeArguments = AnnotatedTypes.annotatedImpl(isomorphism, type.getActualTypeArguments());
		}

		public AnnotatedParameterizedTypeImpl(
				Isomorphism isomorphism,
				Class<?> rawType,
				Function<? super TypeVariable<?>, ? extends AnnotatedType> annotatedTypes,
				Collection<? extends Annotation> annotations) {
			super(ParameterizedTypes.parameterizeUnchecked(rawType, unannotatedTypes(annotatedTypes)), annotations);

			annotatedTypeArguments = AnnotatedTypes
					.wrapImpl(isomorphism, Arrays.stream(rawType.getTypeParameters()).map(p -> {
						AnnotatedType type = annotatedTypes.apply(p);
						if (type == null)
							return AnnotatedTypes.annotatedImpl(isomorphism, p);
						else
							return type;
					}).toArray(AnnotatedType[]::new));
		}

		/*
		 * This really shouldn't need to be factored out into its own method, but
		 * the JDT was having internal errors in Windows and this fixed it...
		 */
		private static Function<? super TypeVariable<?>, ? extends Type> unannotatedTypes(
				Function<? super TypeVariable<?>, ? extends AnnotatedType> annotatedTypes) {
			return annotatedTypes.andThen(AnnotatedType::getType);
		}

		@Override
		public AnnotatedType[] getAnnotatedActualTypeArguments() {
			return annotatedTypeArguments.clone();
		}

		@Override
		public ParameterizedType getType() {
			return (ParameterizedType) super.getType();
		}

		@Override
		public synchronized String toString(Imports imports) {
			if (string == null) {
				string = "...";

				StringBuilder builder = new StringBuilder(annotationString(imports, getAnnotations()))
						.append(Types.toString(getType().getRawType(), imports))
						.append("<");

				builder.append(
						Arrays.stream(getAnnotatedActualTypeArguments()).map(t -> AnnotatedTypes.toString(t, imports)).collect(
								Collectors.joining(", ")));

				string = builder.append(">").toString();
			}

			return string;
		}

		@Override
		public int annotationHashImpl() {
			return super.annotationHashImpl() ^ annotationHash(annotatedTypeArguments);
		}
	}

	private AnnotatedParameterizedTypes() {}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type, Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Annotate an existing {@link ParameterizedType} with the given annotations.
	 * 
	 * @param type
	 *          The parameterized type we wish to annotate.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance over the given
	 *         parameterized type, with the given annotations.
	 */
	public static AnnotatedParameterizedType over(ParameterizedType type, Collection<Annotation> annotations) {
		return overImpl(new Isomorphism(), type, annotations);
	}

	static AnnotatedParameterizedTypeInternal overImpl(
			Isomorphism isomorphism,
			ParameterizedType type,
			Collection<Annotation> annotations) {
		if (annotations.isEmpty()) {
			return isomorphism.byIdentity().getProxiedMapping(
					type,
					AnnotatedParameterizedTypeInternal.class,
					t -> new AnnotatedParameterizedTypeImpl(isomorphism, type, annotations));
		} else {
			return new AnnotatedParameterizedTypeImpl(isomorphism, type, annotations);
		}
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments. {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedParameterizedType parameterize(
			AnnotatedType rawType,
			Function<? super TypeVariable<?>, ? extends AnnotatedType> arguments) {
		return parameterize((Class<?>) rawType.getType(), arguments, rawType.getAnnotations());
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @param annotations
	 *          Annotations to put on the resulting
	 *          {@link AnnotatedParameterizedType}.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedParameterizedType parameterize(
			Class<?> rawType,
			Function<? super TypeVariable<?>, ? extends AnnotatedType> arguments,
			Annotation... annotations) {
		return new AnnotatedParameterizedTypeImpl(new Isomorphism(), rawType, arguments, Arrays.asList(annotations));
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedType parameterize(AnnotatedType rawType, AnnotatedType... arguments) {
		return parameterize(rawType, Arrays.asList(arguments));
	}

	/**
	 * Parameterize a generic class with the given annotated type arguments.
	 * 
	 * @param rawType
	 *          The annotated generic class we wish to parameterize.
	 * @param arguments
	 *          A mapping from the type variables on the generic class to their
	 *          annotated arguments.
	 * @return A new {@link AnnotatedParameterizedType} instance with the given
	 *         type arguments, and the given annotations.
	 */
	public static AnnotatedType parameterize(AnnotatedType rawType, List<AnnotatedType> arguments) {
		Map<TypeVariable<?>, AnnotatedType> annotatedTypes = new HashMap<>();
		TypeVariable<?>[] typeVariables = ((Class<?>) rawType.getType()).getTypeParameters();

		if (typeVariables.length != arguments.size())
			throw new IllegalArgumentException();

		for (int i = 0; i < arguments.size(); i++)
			annotatedTypes.put(typeVariables[i], arguments.get(i));

		return AnnotatedParameterizedTypes
				.parameterize((Class<?>) rawType.getType(), annotatedTypes::get, rawType.getAnnotations());
	}

	/**
	 * For a given parameterized type, we retrieve a mapping of all type variables
	 * on its raw type, as given by
	 * {@link ParameterizedTypes#getAllTypeParameters(Class)} applied to the raw
	 * type of this annotated type, to their annotated arguments within the
	 * context of this type.
	 *
	 * @param type
	 *          The type whose generic type arguments we wish to determine.
	 * @return A mapping of all type variables to their arguments in the context
	 *         of the given type.
	 */
	public static Stream<Map.Entry<TypeVariable<?>, AnnotatedType>> getAllTypeArguments(AnnotatedParameterizedType type) {
		Class<?> rawType = Types.getErasedType(type.getType());

		TypeVariable<?>[] parameters = rawType.getTypeParameters();
		AnnotatedType[] arguments = type.getAnnotatedActualTypeArguments();

		Stream<Map.Entry<TypeVariable<?>, AnnotatedType>> allArguments = IntStream
				.range(0, parameters.length)
				.mapToObj(i -> new AbstractMap.SimpleEntry<>(parameters[i], arguments[i]));

		allArguments = Stream.concat(
				allArguments,
				ParameterizedTypes
						.getAllTypeArguments((ParameterizedType) type.getType())
						.filter(entry -> !entry.getKey().getGenericDeclaration().equals(rawType))
						.map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), AnnotatedTypes.annotated(entry.getValue()))));

		return allArguments;
	}

	protected static AnnotatedParameterizedTypeInternal wrapImpl(AnnotatedParameterizedType type) {
		return wrapImpl(new Isomorphism(), type);
	}

	protected static AnnotatedParameterizedTypeInternal wrapImpl(
			Isomorphism isomorphism,
			AnnotatedParameterizedType type) {
		if (type instanceof AnnotatedParameterizedTypeInternal) {
			return (AnnotatedParameterizedTypeInternal) type;
		} else {
			return isomorphism.byIdentity().getProxiedMapping(
					type,
					AnnotatedParameterizedTypeInternal.class,
					t -> new AnnotatedParameterizedTypeImpl(isomorphism, t));
		}
	}

	/**
	 * Wrap an existing annotated parameterized type.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedParameterizedType} which is equal
	 *         to the given type.
	 */
	public static AnnotatedParameterizedType wrap(AnnotatedParameterizedType type) {
		return wrapImpl(type);
	}
}
