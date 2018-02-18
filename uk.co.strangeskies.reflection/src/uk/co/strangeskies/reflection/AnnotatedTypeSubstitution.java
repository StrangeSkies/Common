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

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes.AnnotatedParameterizedTypeInternal;
import uk.co.strangeskies.reflection.AnnotatedTypeVariables.AnnotatedTypeVariableInternal;
import uk.co.strangeskies.reflection.AnnotatedWildcardTypes.AnnotatedWildcardTypeInternal;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * TODO update the javadocs to take about ANNOTATED types...
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * A TypeSubstitution object is a function mapping Type to Type, which
 * recursively visits each type mentioned by a given type and applies a
 * substitution to those it encounters which match a given condition.
 * 
 * @author Elias N Vasylenko
 *
 */
public class AnnotatedTypeSubstitution {
	private final Function<? super AnnotatedType, ? extends AnnotatedType> mapping;
	private final Supplier<Boolean> empty;
	private final boolean includeTypeVariables;

	/**
	 * Create a new TypeSubstitution with no initial substitution rules.
	 */
	public AnnotatedTypeSubstitution() {
		mapping = t -> null;
		empty = () -> true;
		includeTypeVariables = false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping function.
	 * Typically we do something like create an instance from a {@link Map} of
	 * Type instances to other Type instances, then pass the method reference of
	 * {@link Map#get(Object)} for that map to this constructor. For this specific
	 * example use case though, {@link #AnnotatedTypeSubstitution(Map)} would
	 * perform slightly better.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public AnnotatedTypeSubstitution(Function<? super AnnotatedType, ? extends AnnotatedType> mapping) {
		this.mapping = mapping;
		empty = () -> false;
		includeTypeVariables = false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping. This is more
	 * efficient than the more general
	 * {@link #AnnotatedTypeSubstitution(Function)} constructor, as it can skip
	 * type traversal for empty maps.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public AnnotatedTypeSubstitution(Map<?, ? extends AnnotatedType> mapping) {
		this.mapping = mapping::get;
		empty = mapping::isEmpty;
		includeTypeVariables = false;
	}

	private AnnotatedTypeSubstitution(AnnotatedTypeSubstitution substitution) {
		mapping = substitution.mapping;
		empty = substitution.empty;
		includeTypeVariables = true;
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from
	 *          The type to match in application of this rule.
	 * @param to
	 *          The type to substitute for types which match the rule.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public AnnotatedTypeSubstitution where(AnnotatedType from, AnnotatedType to) {
		return new AnnotatedTypeSubstitution(t -> Objects.equals(from, t) ? to : mapping.apply(t));
	}

	/**
	 * Create a new TypeSubstitution by adding a specific single substitution rule
	 * to the receiver of the invocation. The new rule will be checked and applied
	 * before any existing rules. The receiving TypeSubstitution of invocation of
	 * this method will remain unchanged.
	 * 
	 * @param from
	 *          The type matching condition of the new rule.
	 * @param to
	 *          The substitution transformation to apply to types matching the
	 *          given condition.
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public AnnotatedTypeSubstitution where(
			Predicate<? super AnnotatedType> from,
			Function<? super AnnotatedType, ? extends AnnotatedType> to) {
		return new AnnotatedTypeSubstitution(t -> from.test(t) ? to.apply(t) : mapping.apply(t));
	}

	/**
	 * Create a new TypeSubstitution which is the same as the receiver with the
	 * additional behavior that type variables are also included for bounds
	 * substitution. Normally it makes sense to exclude type variable bounds for
	 * substitution, as their type is immutable and baked into their defining
	 * class definition, but sometimes we may still want this behavior.
	 * 
	 * @return A new TypeSubstitution object with the rule added.
	 */
	public AnnotatedTypeSubstitution withTypeVariables() {
		return new AnnotatedTypeSubstitution(this);
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public AnnotatedType resolve(AnnotatedType type) {
		if (empty.get())
			return type;
		else
			return resolve(type, new Isomorphism());
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @param isomorphism
	 *          the isomorphism for dealing with self bounded and infinite types
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public AnnotatedType resolve(AnnotatedType type, Isomorphism isomorphism) {
		return resolve(type, isomorphism, new IdentityProperty<>(false));
	}

	protected AnnotatedType resolve(AnnotatedType type, Isomorphism isomorphism, IdentityProperty<Boolean> changed) {
		AnnotatedType mapping = this.mapping.apply(type);
		if (mapping != null) {
			if (mapping != type) {
				changed.set(true);
			}
			return mapping;

		} else if (type == null) {
			return null;

		} else if (type.getType() instanceof Class) {
			return type;

		} else if (type instanceof AnnotatedTypeVariable) {
			return includeTypeVariables ? resolveTypeVariable((AnnotatedTypeVariable) type, isomorphism, changed) : type;

		} else if (type instanceof AnnotatedWildcardType) {
			return resolveWildcardType((AnnotatedWildcardType) type, isomorphism, changed);

		} else if (type instanceof AnnotatedArrayType) {
			return AnnotatedArrayTypes.arrayFromComponent(
					resolve(((AnnotatedArrayType) type).getAnnotatedGenericComponentType(), isomorphism, changed),
					type.getAnnotations());

		} else if (type instanceof AnnotatedParameterizedType) {
			return resolveParameterizedType((AnnotatedParameterizedType) type, isomorphism, changed);
		}

		throw new IllegalArgumentException(
				"Cannot resolve unrecognised type '" + type + "' of class'" + type.getClass() + "' with type of class '"
						+ type.getType().getClass() + "'.");
	}

	private AnnotatedType resolveTypeVariable(
			AnnotatedTypeVariable type,
			Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedTypeVariableInternal.class, i -> {

			if (type.getAnnotatedBounds().length > 0) {
				List<AnnotatedType> bounds = resolveTypes(type.getAnnotatedBounds(), isomorphism, changed);
				if (changed.get()) {
					TypeVariable<?> typeVariable = (TypeVariable<?>) type.getType();

					return AnnotatedTypeVariables.over(
							TypeVariables.typeVariableExtending(typeVariable.getGenericDeclaration(), typeVariable.getName(), bounds),
							type.getAnnotations());
				} else {
					return type;
				}

			} else
				return type;
		});
	}

	private AnnotatedType resolveWildcardType(
			AnnotatedWildcardType type,
			Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedWildcardTypeInternal.class, i -> {

			if (type.getAnnotatedLowerBounds().length > 0) {
				List<AnnotatedType> bounds = resolveTypes(type.getAnnotatedLowerBounds(), isomorphism, changed);
				if (changed.get()) {
					return AnnotatedWildcardTypes.wildcardSuper(Arrays.asList(type.getAnnotations()), bounds);
				} else {
					return type;
				}

			} else if (type.getAnnotatedUpperBounds().length > 0) {
				List<AnnotatedType> bounds = resolveTypes(type.getAnnotatedUpperBounds(), isomorphism, changed);
				if (changed.get()) {
					return AnnotatedWildcardTypes.wildcardExtending(Arrays.asList(type.getAnnotations()), bounds);
				} else {
					return type;
				}

			} else
				return type;
		});
	}

	private AnnotatedType resolveParameterizedType(
			AnnotatedParameterizedType type,
			Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedParameterizedTypeInternal.class, i -> {

			List<AnnotatedType> arguments = resolveTypes(type.getAnnotatedActualTypeArguments(), isomorphism, changed);

			if (changed.get()) {
				return (AnnotatedParameterizedType) AnnotatedParameterizedTypes
						.parameterize(AnnotatedTypes.annotated(Types.getErasedType(type.getType()), type.getAnnotations()), arguments);
			} else {
				return type;
			}
		});
	}

	private List<AnnotatedType> resolveTypes(
			AnnotatedType[] types,
			Isomorphism isomorphism,
			IdentityProperty<Boolean> changed) {
		return Arrays.stream(types).map(t -> resolve(t, isomorphism, changed)).collect(Collectors.toList());
	}
}
