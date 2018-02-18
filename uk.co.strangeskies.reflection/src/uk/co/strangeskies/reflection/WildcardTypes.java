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

import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A collection of utility methods relating to wildcard types.
 * 
 * @author Elias N Vasylenko
 */
public class WildcardTypes {
	private static final Type[] DEFAULT_UPPER_BOUND = new Type[] { Object.class };
	private static final Type[] EMPTY_BOUND = new Type[] {};

	/*
	 * TODO the spec currently only allows one upper or lower bound. We should
	 * probably move in line with this since the rest of the spec isn't designed
	 * to handle the alternative? Or do we want bounds which are intersection
	 * types to be expanded into arrays?
	 */

	private static final WildcardType UNBOUNDED = new WildcardType() {
		@Override
		public Type[] getUpperBounds() {
			return DEFAULT_UPPER_BOUND;
		}

		@Override
		public Type[] getLowerBounds() {
			return EMPTY_BOUND;
		}

		@Override
		public String toString() {
			return "?";
		}

		@Override
		public boolean equals(Object that) {
			if (!(that instanceof WildcardType))
				return false;
			if (that == this)
				return true;

			WildcardType wildcard = (WildcardType) that;

			return wildcard.getLowerBounds().length == 0
					&& (wildcard.getUpperBounds().length == 0 || (wildcard.getUpperBounds().length == 1
							&& wildcard.getUpperBounds()[0].equals(Object.class)));
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
		}
	};

	private WildcardTypes() {}

	/**
	 * Create an unbounded wildcard type.
	 * 
	 * @return An instance of {@link WildcardType} representing an unbounded
	 *         wildcard.
	 */
	public static WildcardType wildcard() {
		return UNBOUNDED;
	}

	/**
	 * Create an lower bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given lower bound.
	 */
	public static WildcardType wildcardSuper(Type... bounds) {
		return wildcardSuper(Arrays.asList(bounds));
	}

	/**
	 * Create an lower bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given lower bound.
	 */
	public static WildcardType wildcardSuper(Collection<? extends Type> bounds) {
		Type type = intersectionOf(bounds);

		Supplier<Type[]> types;

		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getUpperBounds().length == 0)
				types = () -> DEFAULT_UPPER_BOUND;
			else
				types = () -> wildcardType.getUpperBounds();
		} else if (type instanceof IntersectionType)
			types = ((IntersectionType) type)::getTypes;
		else
			types = () -> new Type[] { type };

		return new WildcardType() {
			private Integer hashCode;

			@Override
			public Type[] getUpperBounds() {
				return DEFAULT_UPPER_BOUND;
			}

			@Override
			public Type[] getLowerBounds() {
				return types.get();
			}

			@Override
			public String toString() {
				return "? super "
						+ Arrays.stream(types.get()).map(Types::toString).collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;

				WildcardType wildcard = (WildcardType) that;

				Type[] thatUpperBounds = wildcard.getUpperBounds();
				if (thatUpperBounds.length == 0)
					thatUpperBounds = DEFAULT_UPPER_BOUND;

				return Arrays.equals(types.get(), wildcard.getLowerBounds())
						&& Arrays.equals(thatUpperBounds, DEFAULT_UPPER_BOUND);
			}

			@Override
			public synchronized int hashCode() {
				if (hashCode == null) {
					/*
					 * This way the hash code will return 0 if we encounter it again in
					 * the parameters, rather than recurring infinitely:
					 * 
					 * (this is not a problem for other threads as hashCode is
					 * synchronized)
					 */
					hashCode = 0;

					/*
					 * Calculate the hash code properly, now we're guarded against
					 * recursion:
					 */
					this.hashCode = Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
				}

				return hashCode;
			}
		};
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static WildcardType wildcardExtending(Type... bounds) {
		return wildcardExtending(Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link WildcardType} representing a wildcard with
	 *         the given upper bound.
	 */
	public static WildcardType wildcardExtending(Collection<? extends Type> bounds) {
		return new WildcardType() {
			private Integer hashCode;
			private Type[] types;
			private final Runnable typeInitialiser = () -> {
				if (bounds.isEmpty()) {
					types = DEFAULT_UPPER_BOUND;
				} else {
					types = bounds.toArray(new Type[bounds.size()]);
					Type type = intersectionOf(bounds);

					if (type instanceof WildcardType) {
						WildcardType wildcardType = ((WildcardType) type);
						if (wildcardType.getLowerBounds().length == 0) {
							throw new ReflectionException(REFLECTION_PROPERTIES.invalidUpperBound(wildcardType));
						} else {
							types = wildcardType.getLowerBounds();
						}
					} else if (type instanceof IntersectionType) {
						types = ((IntersectionType) type).getTypes();
					} else {
						types = new Type[] { type };
					}
				}
			};

			@Override
			public Type[] getUpperBounds() {
				if (types == null)
					typeInitialiser.run();
				return types;
			}

			@Override
			public Type[] getLowerBounds() {
				return EMPTY_BOUND;
			}

			@Override
			public String toString() {
				Type[] bounds = getUpperBounds();
				if (bounds.length == 0 || (bounds.length == 1 && bounds[0].equals(Object.class)))
					return "?";
				else
					return "? extends "
							+ Arrays.stream(bounds).map(Types::toString).collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;

				Type[] thisUpperBounds = getUpperBounds();
				if (thisUpperBounds.length == 1 && thisUpperBounds[0].equals(Object.class))
					thisUpperBounds = EMPTY_BOUND;

				Type[] thatUpperBounds = wildcard.getUpperBounds();
				if (thatUpperBounds.length == 1 && thatUpperBounds[0].equals(Object.class))
					thatUpperBounds = EMPTY_BOUND;

				return wildcard.getLowerBounds().length == 0
						&& Arrays.equals(thisUpperBounds, thatUpperBounds);
			}

			@Override
			public synchronized int hashCode() {
				if (hashCode == null) {
					/*
					 * This way the hash code will return 0 if we encounter it again in
					 * the parameters, rather than recurring infinitely:
					 * 
					 * (this is not a problem for other threads as hashCode is
					 * synchronized)
					 */
					hashCode = 0;

					/*
					 * Calculate the hash code properly, now we're guarded against
					 * recursion:
					 */
					this.hashCode = Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
				}

				return hashCode;
			}
		};
	}
}
