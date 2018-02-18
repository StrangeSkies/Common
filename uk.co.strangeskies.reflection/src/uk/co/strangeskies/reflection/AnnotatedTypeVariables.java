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
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeImpl;
import uk.co.strangeskies.reflection.AnnotatedTypes.AnnotatedTypeInternal;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to annotated type variables.
 * 
 * @author Elias N Vasylenko
 */
public final class AnnotatedTypeVariables {
	@SuppressWarnings("javadoc")
	public static interface AnnotatedTypeVariableInternal extends AnnotatedTypeVariable, AnnotatedTypeInternal {}

	private static class AnnotatedTypeVariableImpl extends AnnotatedTypeImpl implements AnnotatedTypeVariableInternal {
		private final AnnotatedTypeInternal[] annotatedBounds;

		public AnnotatedTypeVariableImpl(Isomorphism isomorphism, AnnotatedTypeVariable annotatedTypeVariable) {
			super(annotatedTypeVariable);

			annotatedBounds = AnnotatedTypes.wrapImpl(isomorphism, annotatedTypeVariable.getAnnotatedBounds());
		}

		public AnnotatedTypeVariableImpl(Isomorphism isomorphism, TypeVariable<?> type,
				Collection<Annotation> annotations) {
			super(type, annotations);

			annotatedBounds = AnnotatedTypes.wrapImpl(isomorphism, type.getAnnotatedBounds());
		}

		@Override
		public TypeVariable<?> getType() {
			return (TypeVariable<?>) super.getType();
		}

		@Override
		public int annotationHashImpl() {
			return super.annotationHashImpl() ^ annotationHash(annotatedBounds);
		}

		@Override
		public AnnotatedType[] getAnnotatedBounds() {
			return annotatedBounds.clone();
		}
	}

	private AnnotatedTypeVariables() {}

	/**
	 * Create a new annotated representation of a given generic type variable.
	 * 
	 * @param typeVariable
	 *          The type variable to be annotated.
	 * @param annotations
	 *          The annotations for the annotated type variable.
	 * @return A new annotated type variable over the given type variable and with
	 *         the given annotations.
	 */
	public static AnnotatedTypeVariable over(TypeVariable<?> typeVariable, Annotation... annotations) {
		return over(typeVariable, Arrays.asList(annotations));
	}

	/**
	 * Create a new annotated representation of a given generic type variable.
	 * 
	 * @param typeVariable
	 *          The type variable to be annotated.
	 * @param annotations
	 *          The annotations for the annotated type variable.
	 * @return A new annotated type variable over the given type variable and with
	 *         the given annotations.
	 */
	public static AnnotatedTypeVariable over(TypeVariable<?> typeVariable, Collection<Annotation> annotations) {
		return overImpl(new Isomorphism(), typeVariable, annotations);
	}

	static AnnotatedTypeVariableInternal overImpl(Isomorphism isomorphism, TypeVariable<?> type,
			Collection<Annotation> annotations) {
		if (annotations.isEmpty()) {
			return isomorphism.byIdentity().getProxiedMapping(type, AnnotatedTypeVariableInternal.class,
					t -> new AnnotatedTypeVariableImpl(isomorphism, t, annotations));
		} else {
			return new AnnotatedTypeVariableImpl(isomorphism, type, annotations);
		}
	}

	protected static AnnotatedTypeVariableInternal wrapImpl(AnnotatedTypeVariable type) {
		return wrapImpl(new Isomorphism(), type);
	}

	protected static AnnotatedTypeVariableInternal wrapImpl(Isomorphism isomorphism, AnnotatedTypeVariable type) {
		if (type instanceof AnnotatedTypeVariableInternal) {
			return (AnnotatedTypeVariableInternal) type;
		} else {
			return isomorphism.byIdentity().getProxiedMapping(type.getType(), AnnotatedTypeVariableInternal.class,
					t -> new AnnotatedTypeVariableImpl(isomorphism, type));
		}
	}

	/**
	 * Wrap an existing annotated type variable.
	 * 
	 * @param type
	 *          The type we wish to wrap.
	 * @return A new instance of {@link AnnotatedTypeVariable} which is equal to
	 *         the given type.
	 */
	public static AnnotatedTypeVariable wrap(AnnotatedTypeVariable type) {
		return wrapImpl(type);
	}
}
