/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.utilities.IdentityProperty;
import uk.co.strangeskies.utilities.Isomorphism;

/**
 * A TypeSubstitution object is a function mapping Type to Type, which
 * recursively visits each type mentioned by a given type and applies a
 * substitution to those it encounters which match a given condition.
 * 
 * @author Elias N Vasylenko
 *
 */
public class TypeSubstitution {
	private final Function<? super Type, ? extends Type> mapping;
	private final Supplier<Boolean> empty;

	/**
	 * Create a new TypeSubstitution with no initial substitution rules.
	 */
	public TypeSubstitution() {
		mapping = t -> null;
		empty = () -> true;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping function.
	 * Typically we do something like create an instance from a {@link Map} of
	 * Type instances to other Type instances, then pass the method reference of
	 * {@link Map#get(Object)} for that map to this constructor. For this specific
	 * example use case though, {@link #TypeSubstitution(Map)} would perform
	 * slightly better.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public TypeSubstitution(Function<? super Type, ? extends Type> mapping) {
		this.mapping = mapping;
		empty = () -> false;
	}

	/**
	 * Create a new TypeSubstitution to apply the given mapping. This is more
	 * efficient than the more general {@link #TypeSubstitution(Function)}
	 * constructor, as it can skip type traversal for empty maps.
	 * 
	 * @param mapping
	 *          A mapping function for transforming encountered types to their
	 *          substitution types.
	 */
	public TypeSubstitution(Map<?, ? extends Type> mapping) {
		this.mapping = mapping::get;
		empty = mapping::isEmpty;
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
	public TypeSubstitution where(Type from, Type to) {
		return new TypeSubstitution(t -> Objects.equals(from, t) ? to : mapping.apply(t));
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
	public TypeSubstitution where(Predicate<Type> from, Function<Type, Type> to) {
		return new TypeSubstitution(t -> from.test(t) ? to.apply(t) : mapping.apply(t));
	}

	/**
	 * Resolve the result of this substitution as applied to the given type.
	 * 
	 * @param type
	 *          The type for which we want to make a substitution.
	 * @return The result of application of this substitution. The result is
	 *         <em>not</em> guaranteed to be well formed with respect to bounds.
	 */
	public Type resolve(Type type) {
		if (empty.get())
			return type;
		else
			return resolve(type, new Isomorphism());
	}

	private Type resolve(Type type, Isomorphism isomorphism) {
		Type mapping = this.mapping.apply(type);
		if (mapping != null) {
			return mapping;

		} else if (type == null) {
			return null;

		} else if (type instanceof Class) {
			return type;

		} else if (type instanceof TypeVariable) {
			return type;

		} else if (type instanceof InferenceVariable) {
			return type;

		} else if (type instanceof IntersectionType) {
			return resolveIntersectionType((IntersectionType) type, isomorphism);

		} else if (type instanceof WildcardType) {
			return resolveWildcardType((WildcardType) type, isomorphism);

		} else if (type instanceof GenericArrayType) {
			return ArrayTypes.fromComponentType(resolve(((GenericArrayType) type).getGenericComponentType(), isomorphism));

		} else if (type instanceof ParameterizedType) {
			return resolveParameterizedType((ParameterizedType) type, isomorphism);
		}

		throw new IllegalArgumentException(
				"Cannot resolve unrecognised type '" + type + "' of class'" + type.getClass() + "'.");
	}

	private Type resolveWildcardType(WildcardType type, Isomorphism isomorphism) {
		return isomorphism.byIdentity().getProxiedMapping(type, WildcardType.class, i -> {

			if (type.getLowerBounds().length > 0) {
				return WildcardTypes.lowerBounded(resolve(IntersectionType.uncheckedFrom(type.getLowerBounds()), isomorphism));

			} else if (type.getUpperBounds().length > 0) {
				return WildcardTypes.upperBounded(resolve(IntersectionType.uncheckedFrom(type.getUpperBounds()), isomorphism));

			} else
				return WildcardTypes.unbounded();
		});
	}

	private Type resolveIntersectionType(IntersectionType type, Isomorphism isomorphism) {
		return isomorphism.byIdentity().getPartialMapping(type, (i, partial) -> {

			IdentityProperty<IntersectionType> property = new IdentityProperty<>();
			Type proxy = IntersectionType.proxy(property::get);
			partial.accept(() -> proxy);

			IntersectionType result = IntersectionType
					.uncheckedFrom(Arrays.stream(type.getTypes()).map(t -> resolve(t, isomorphism)).collect(Collectors.toList()));

			property.set(result);

			return result;
		});
	}

	private Type resolveParameterizedType(ParameterizedType type, Isomorphism isomorphism) {
		return isomorphism.byIdentity().getProxiedMapping(type, ParameterizedType.class, i -> {

			return ParameterizedTypes.uncheckedFrom(resolve(type.getOwnerType(), isomorphism), Types.getRawType(type),
					Arrays.stream(type.getActualTypeArguments()).map(t -> resolve(t, isomorphism)).collect(Collectors.toList()));
		});
	}
}
