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
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;
import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeInternal;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to annotated wildcard types.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedWildcardTypes {
	@SuppressWarnings("javadoc")
	public static interface AnnotatedWildcardTypeInternal extends AnnotatedWildcardType, AnnotatedTypeInternal {}

	private static class AnnotatedWildcardTypeImpl extends AnnotatedTypeImpl implements AnnotatedWildcardTypeInternal {
		private final AnnotatedTypeInternal[] annotatedUpperBounds;
		private final AnnotatedTypeInternal[] annotatedLowerBounds;

		public AnnotatedWildcardTypeImpl(Isomorphism isomorphism, AnnotatedWildcardType annotatedWildcardType) {
			super(annotatedWildcardType);

			annotatedUpperBounds = AnnotatedTypes.wrapImpl(isomorphism, annotatedWildcardType.getAnnotatedUpperBounds());
			annotatedLowerBounds = AnnotatedTypes.wrapImpl(isomorphism, annotatedWildcardType.getAnnotatedLowerBounds());
		}

		public AnnotatedWildcardTypeImpl(Isomorphism isomorphism, WildcardType type, Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedUpperBounds = AnnotatedTypes.annotatedImpl(isomorphism, type.getUpperBounds());
			annotatedLowerBounds = AnnotatedTypes.annotatedImpl(isomorphism, type.getLowerBounds());
		}

		public AnnotatedWildcardTypeImpl(Isomorphism isomorphism, Collection<? extends AnnotatedType> upperBounds,
				Collection<? extends AnnotatedType> lowerBounds, Collection<Annotation> annotations) {
			super(wildcardFrom(upperBounds, lowerBounds), annotations);

			annotatedUpperBounds = AnnotatedTypes.wrapImpl(isomorphism,
					upperBounds.toArray(new AnnotatedType[upperBounds.size()]));
			annotatedLowerBounds = AnnotatedTypes.wrapImpl(isomorphism,
					lowerBounds.toArray(new AnnotatedType[lowerBounds.size()]));
		}

		private static Type wildcardFrom(Collection<? extends AnnotatedType> upperBounds,
				Collection<? extends AnnotatedType> lowerBounds) {
			if (!upperBounds.isEmpty())
				return WildcardTypes
						.wildcardExtending(upperBounds.stream().map(AnnotatedType::getType).collect(Collectors.toList()));
			else if (!lowerBounds.isEmpty())
				return WildcardTypes
						.wildcardSuper(lowerBounds.stream().map(AnnotatedType::getType).collect(Collectors.toList()));
			else
				return WildcardTypes.wildcard();
		}

		@Override
		public AnnotatedType[] getAnnotatedUpperBounds() {
			return annotatedUpperBounds.clone();
		}

		@Override
		public AnnotatedType[] getAnnotatedLowerBounds() {
			return annotatedLowerBounds.clone();
		}

		@Override
		public WildcardType getType() {
			return (WildcardType) super.getType();
		}

		@Override
		public String toString(Imports imports) {
			StringBuilder builder = new StringBuilder(annotationString(imports, getAnnotations()));

			AnnotatedType[] bounds;
			if ((bounds = getAnnotatedUpperBounds()).length > 1
					|| (bounds.length == 1 && !bounds[0].equals(AnnotatedTypes.annotated(Object.class)))) {
				builder.append("? extends ");
			} else if ((bounds = getAnnotatedLowerBounds()).length > 0) {
				builder.append("? super ");
			} else {
				bounds = new AnnotatedType[0];
				builder.append("?");
			}

			return builder.append(annotatedBounds(bounds, imports)).toString();
		}

		private String annotatedBounds(AnnotatedType[] bounds, Imports imports) {
			return Arrays.stream(bounds).map(t -> AnnotatedTypes.toString(t, imports)).collect(Collectors.joining(" & "));
		}

		@Override
		public int annotationHashImpl() {
			return super.annotationHashImpl() ^ annotationHash(annotatedUpperBounds) ^ annotationHash(annotatedLowerBounds);
		}
	}

	private AnnotatedWildcardTypes() {}

	/**
	 * Create a new annotated wildcard type over the given wildcard type.
	 * 
	 * @param type
	 *          The wildcard over which to create an annotated wildcard type.
	 * @param annotations
	 *          The annotations to put on the new type.
	 * @return A new AnnotatedWildcardType over the given wildcard type with the
	 *         given annotations.
	 */
	public static AnnotatedWildcardType over(WildcardType type, Annotation... annotations) {
		return over(type, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated wildcard type over the given wildcard type.
	 * 
	 * @param type
	 *          The wildcard over which to create an annotated wildcard type.
	 * @param annotations
	 *          The annotations to put on the new type.
	 * @return A new AnnotatedWildcardType over the given wildcard type with the
	 *         given annotations.
	 */
	public static AnnotatedWildcardType over(WildcardType type, Collection<Annotation> annotations) {
		return overImpl(new Isomorphism(), type, annotations);
	}

	static AnnotatedWildcardTypeInternal overImpl(Isomorphism isomorphism, WildcardType type,
			Collection<Annotation> annotations) {
		if (annotations.isEmpty()) {
			return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedWildcardTypeInternal.class,
					t -> new AnnotatedWildcardTypeImpl(isomorphism, t, annotations));
		} else {
			return new AnnotatedWildcardTypeImpl(isomorphism, type, annotations);
		}
	}

	/**
	 * Create an unbounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @return An instance of {@link AnnotatedWildcardType} representing an
	 *         unbounded wildcard.
	 */
	public static AnnotatedWildcardType wildcard(Annotation... annotations) {
		return wildcard(Arrays.asList(annotations));
	}

	/**
	 * Create an unbounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @return An instance of {@link AnnotatedWildcardType} representing an
	 *         unbounded wildcard.
	 */
	public static AnnotatedWildcardType wildcard(Collection<Annotation> annotations) {
		return over(WildcardTypes.wildcard(), annotations);
	}

	/**
	 * Create a lower bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the lower bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.
	 */
	public static AnnotatedWildcardType wildcardSuper(Collection<Annotation> annotations, AnnotatedType... bounds) {
		return wildcardSuper(annotations, Arrays.asList(bounds));
	}

	/**
	 * Create a lower bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          the annotations to be contained by the new annotated type
	 * @param bounds
	 *          the types we wish form the lower bounds for a wildcard
	 * @return an instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound.\
	 */
	public static AnnotatedWildcardType wildcardSuper(Collection<Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		return new AnnotatedWildcardTypeImpl(new Isomorphism(), Collections.emptySet(), bounds, annotations);
	}

	/**
	 * Create a lower bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          the types we wish form the lower bounds for a wildcard
	 * @return an instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound
	 */
	public static AnnotatedWildcardType wildcardSuper(AnnotatedType... bounds) {
		return wildcardSuper(Arrays.asList(bounds));
	}

	/**
	 * Create a lower bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          the types we wish form the lower bounds for a wildcard
	 * @return an instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given lower bound
	 */
	public static AnnotatedWildcardType wildcardSuper(Collection<? extends AnnotatedType> bounds) {
		return wildcardSuper(Collections.emptySet(), bounds);
	}

	/**
	 * Create an upper bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType wildcardExtending(Collection<Annotation> annotations, AnnotatedType... bounds) {
		return wildcardExtending(annotations, Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded annotated wildcard type with the given annotations.
	 * 
	 * @param annotations
	 *          The annotations to be contained by the new annotated type.
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType wildcardExtending(Collection<Annotation> annotations,
			Collection<? extends AnnotatedType> bounds) {
		return new AnnotatedWildcardTypeImpl(new Isomorphism(), bounds, Collections.emptySet(), annotations);
	}

	/**
	 * Create an upper bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType wildcardExtending(AnnotatedType... bounds) {
		return wildcardExtending(Arrays.asList(bounds));
	}

	/**
	 * Create an upper bounded annotated wildcard type.
	 * 
	 * @param bounds
	 *          The types we wish form the upper bounds for a wildcard.
	 * @return An instance of {@link AnnotatedWildcardType} representing a
	 *         wildcard with the given upper bound.
	 */
	public static AnnotatedWildcardType wildcardExtending(Collection<? extends AnnotatedType> bounds) {
		return wildcardExtending(Collections.emptySet(), bounds);
	}

	protected static AnnotatedWildcardTypeInternal wrapImpl(Isomorphism isomorphism, AnnotatedWildcardType type) {
		if (type instanceof AnnotatedWildcardTypeInternal) {
			return (AnnotatedWildcardTypeInternal) type;
		} else {
			return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedWildcardTypeInternal.class,
					t -> new AnnotatedWildcardTypeImpl(isomorphism, t));
		}
	}

	/**
	 * Wrap an existing annotated wildcard type.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedWildcardType} which is equal to
	 *         the given type.
	 */
	public static AnnotatedWildcardType wrap(AnnotatedWildcardType type) {
		return wrapImpl(new Isomorphism(), type);
	}
}
