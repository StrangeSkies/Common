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
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.empty;
import static uk.co.strangeskies.collection.stream.StreamUtilities.flatMapRecursive;
import static uk.co.strangeskies.collection.stream.StreamUtilities.iterateOptional;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.Types.isAssignable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.stream.StreamUtilities;

public class TypeHierarchy {
	private final Type lowerBound;
	private final Map<Class<?>, Type> supertypes;

	/**
	 * @param lowerBound
	 *          the type providing a context within which to determine the arguments
	 *          of the supertypes
	 */
	public TypeHierarchy(Type lowerBound) {
		this.lowerBound = lowerBound;
		this.supertypes = new HashMap<>();
	}

	public Stream<Type> resolveSuperClasses() {
		if (lowerBound == Object.class) {
			return Stream.of(Object.class);
		}
		if (Types.getErasedType(lowerBound).isInterface()) {
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotResolveSupertype(lowerBound, Object.class));
		}
		return StreamUtilities.iterate(
				lowerBound,
				t -> resolveSupertypeHierarchyImpl(t, getErasedType(t).getSuperclass())
						.reduce((a, b) -> b)
						.get());
	}

	/**
	 * Determine the recursive sequence of direct supertypes of a given type which
	 * lead to either the given superclass or a parameterization thereof.
	 * 
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return a stream returning the given type and then each direct supertype
	 *         recursively until the given superclass, or a parameterization
	 *         thereof, is reached
	 */
	public Stream<Type> resolveDirectSupertypeHierarchy(Class<?> superclass) {
		if (!Types.isAssignable(lowerBound, superclass)) {
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotResolveSupertype(lowerBound, superclass));
		}

		return resolveSupertypeHierarchyImpl(lowerBound, superclass);
	}

	/**
	 * Determine the recursive sequence of direct supertypes of a given type which
	 * lead to either the given superclass or a parameterization thereof.
	 * 
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return a stream returning the given type and then each direct supertype
	 *         recursively until the given superclass, or a parameterization
	 *         thereof, is reached
	 */
	public Stream<Type> resolveCompleteSupertypeHierarchy(Class<?> superclass) {
		Set<Class<?>> encountered = new HashSet<>();

		return flatMapRecursive(
				lowerBound,
				t -> resolveImmediateSupertypes(encountered, t, superclass));
	}

	/**
	 * Determine the super type of a given type which is either equal to the given
	 * superclass or a parameterization thereof.
	 * 
	 * @param superclass
	 *          the class of the supertype parameterization we wish to determine
	 * @return the supertype of the requested class
	 */
	public Type resolveSupertype(Class<?> superclass) {
		if (supertypes.containsKey(superclass)) {
			return supertypes.get(superclass);
		}

		if (!Types.isAssignable(lowerBound, superclass)) {
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotResolveSupertype(lowerBound, superclass));
		} else if (!Types.isGeneric(superclass)) {
			return superclass;
		}

		return resolveSupertypeHierarchyImpl(lowerBound, superclass).reduce((a, b) -> b).get();
	}

	/**
	 * Determine the immediate supertypes of the given type.
	 * 
	 * @return a stream of the supertypes of the requested class
	 */
	public Stream<Type> resolveImmediateSupertypes() {
		return resolveImmediateSupertypes(null, lowerBound, Object.class);
	}

	private Stream<Type> resolveSupertypeHierarchyImpl(Type type, Class<?> superclass) {
		return iterateOptional(type, t -> resolveImmediateSupertypes(null, t, superclass).findFirst());
	}

	private Stream<Type> resolveImmediateSupertypes(
			Set<Class<?>> encountered,
			Type type,
			Class<?> superclass) {
		List<Type> lesserSubtypes;
		if (type instanceof Class<?> || type instanceof ParameterizedType) {
			lesserSubtypes = resolveClassLesserSubtypes(encountered, type, superclass);
		} else if (type instanceof IntersectionType) {
			lesserSubtypes = asList(((IntersectionType) type).getTypes());
		} else if (type instanceof TypeVariableCapture) {
			lesserSubtypes = asList(((TypeVariableCapture) type).getUpperBounds());
		} else if (type instanceof TypeVariable<?>) {
			lesserSubtypes = asList(((TypeVariable<?>) type).getBounds());
		} else {
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotResolveSupertype(type, superclass),
					new ReflectionException(REFLECTION_PROPERTIES.unsupportedType(type)));
		}

		if (lesserSubtypes.isEmpty()) {
			return empty();
		}

		/*
		 * If there is more than one supertype in evaluation
		 */
		boolean removeEncounters = encountered != null
				&& (!encountered.isEmpty() || lesserSubtypes.size() > 1);

		for (Iterator<Type> lesserSubtypeIterator = lesserSubtypes.iterator(); lesserSubtypeIterator
				.hasNext();) {
			Type lesserSubtype = lesserSubtypeIterator.next();

			if (!isAssignable(lesserSubtype, superclass)) {
				lesserSubtypeIterator.remove();
			} else if (removeEncounters) {
				Class<?> rawClass;
				if (lesserSubtype instanceof Class<?>) {
					rawClass = (Class<?>) lesserSubtype;
				} else if (lesserSubtype instanceof ParameterizedType) {
					rawClass = (Class<?>) ((ParameterizedType) lesserSubtype).getRawType();
				} else {
					rawClass = null;
				}

				if (rawClass != null && encountered.stream().anyMatch(rawClass::isAssignableFrom)) {
					lesserSubtypeIterator.remove();
				} else if (!isAssignable(lesserSubtype, superclass)) {
					lesserSubtypeIterator.remove();
				} else {
					encountered.add(rawClass);
				}
			}
		}

		return lesserSubtypes.stream().map(subtype -> {
			Class<?> rawType = Types.getErasedType(subtype);

			if (type instanceof ParameterizedType) {
				return supertypes.computeIfAbsent(
						rawType,
						r -> new TypeSubstitution(
								getAllTypeArguments((ParameterizedType) type)
										.collect(toMap(Entry::getKey, Entry::getValue))).resolve(subtype));
			} else {
				supertypes.put(rawType, subtype);
				return subtype;
			}
		});
	}

	private List<Type> resolveClassLesserSubtypes(
			Set<Class<?>> encountered,
			Type type,
			Class<?> superclass) {
		Class<?> subclass = Types.getErasedType(type);
		if (subclass == superclass) {
			return emptyList();
		}

		if (encountered != null && !encountered.isEmpty()) {
			encountered.remove(subclass);
		}

		Type genericSuperclass;
		Type[] genericInterfaces;
		if (Types.isGeneric(subclass) && type instanceof Class<?>) {
			genericSuperclass = subclass.getSuperclass();
			genericInterfaces = subclass.getInterfaces();
		} else {
			genericSuperclass = subclass.getGenericSuperclass();
			genericInterfaces = subclass.getGenericInterfaces();
		}

		List<Type> lesserSubtypes = new ArrayList<>(genericInterfaces.length + 1);
		stream(genericInterfaces).forEach(lesserSubtypes::add);
		if (genericSuperclass != null) {
			lesserSubtypes.add(genericSuperclass);
		} else if (genericInterfaces.length == 0) {
			lesserSubtypes.add(Object.class);
		}
		return lesserSubtypes;
	}

	@Override
	public String toString() {
		return lowerBound + " <: " + supertypes;
	}
}
