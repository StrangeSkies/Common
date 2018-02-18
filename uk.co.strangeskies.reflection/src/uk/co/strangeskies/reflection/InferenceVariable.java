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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import uk.co.strangeskies.property.IdentityProperty;

/**
 * <p>
 * An inference variable can be thought of as a placeholder for an
 * <em>instantiation</em> of a {@link TypeVariable} of which we do not yet know
 * the exact type. An {@link InferenceVariable} alone has no bounds or type
 * information attached to it. Instead, typically, they are contained within the
 * context of one or more {@link BoundSet}s, which will track bounds on a set of
 * inference variables such they their exact type can ultimately be inferred.
 * 
 * 
 * @author Elias N Vasylenko
 */
public class InferenceVariable implements Type {
	private static final AtomicLong COUNTER = new AtomicLong();

	private final String name;
	private final long number;

	/**
	 * Create a new inference variable with a basic generated name, which is
	 * contained within this bound set.
	 */
	public InferenceVariable() {
		this("INF");
	}

	/**
	 * Create a new inference variable with the given name.
	 * 
	 * @param name
	 *          A name to assign to a new inference variable.
	 */
	public InferenceVariable(String name) {
		this.name = name;
		number = COUNTER.incrementAndGet();
	}

	/**
	 * @return The given name of the inference variable.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The internally assigned number which uniquely distinguishes the
	 *         inference variable from others with the same name.
	 */
	public long getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return name + "#" + number;
	}

	/**
	 * Create fresh {@link InferenceVariable}s for each parameter of the given
	 * type - and each non-statically enclosing type thereof - which is a
	 * {@link WildcardType}. New bounds based on the bounds of those wildcards,
	 * and the bounds of the {@link TypeVariable}s they substitute, will be
	 * incorporated into the given {@link BoundSet}, along with a
	 * {@link CaptureConversion} bound representing this capture conversion. The
	 * process of capture conversion is described in more detail in the Java 8
	 * language specification.
	 * 
	 * @param type
	 *          A parameterised type whose wildcard type arguments, if present, we
	 *          wish to capture as inference variables.
	 * @param bounds
	 *          The bound set we wish to create any fresh inference variables
	 *          within, and incorporate any newly implied bounds into.
	 * @return A new parameterized type derived from the given parameterized type,
	 *         with any fresh {@link InferenceVariable}s substituted for the type
	 *         arguments.
	 */
	/*
	 * Let G name a generic type declaration (§8.1.2, §9.1.2) with n type
	 * parameters A1,...,An with corresponding bounds U1,...,Un.
	 */
	public static ParameterizedType captureConversion(ParameterizedType type, BoundSet bounds) {
		if (ParameterizedTypes.getAllTypeArguments(type).map(Entry::getValue).anyMatch(
				WildcardType.class::isInstance)) {
			/*
			 * There exists a capture conversion from a parameterized type
			 * G<T1,...,Tn> (§4.5) to a parameterized type G<S1,...,Sn>, where, for 1
			 * ≤ i ≤ n :
			 */

			CaptureConversion captureConversion = new CaptureConversion(type);

			bounds.withIncorporated().captureConversion(captureConversion);

			return captureConversion.getCaptureType();
		} else
			return type;
	}

	/**
	 * Find all inference variables mentioned by a type, whether in any bounds,
	 * parameters, array types, etc. recursively.
	 * 
	 * @param type
	 *          The type in which to find inference variable mentions.
	 * @return The inference variables mentioned by the given type.
	 */
	public static Stream<InferenceVariable> getMentionedBy(Type type) {
		if (type instanceof Class<?>) {
			return Stream.empty();
		}

		Set<InferenceVariable> inferenceVariables = new HashSet<>();

		new TypeVisitor() {
			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				visit(type.getActualTypeArguments());
				visit(type.getOwnerType());
			}

			@Override
			protected void visitGenericArrayType(GenericArrayType type) {
				visit(type.getGenericComponentType());
			}

			@Override
			protected void visitWildcardType(WildcardType type) {
				visit(type.getUpperBounds());
				visit(type.getLowerBounds());
			}

			@Override
			protected void visitTypeVariableCapture(TypeVariableCapture type) {
				visit(type.getUpperBounds());
				visit(type.getLowerBounds());
			}

			@Override
			protected void visitTypeVariable(TypeVariable<?> type) {
				visit(type.getBounds());
			}

			@Override
			protected void visitInferenceVariable(InferenceVariable type) {
				inferenceVariables.add(type);
			}

			@Override
			protected void visitIntersectionType(IntersectionType type) {
				visit(type.getTypes());
			}
		}.visit(type);

		return inferenceVariables.stream();
	}

	/**
	 * Determine whether a given type is proper.
	 * 
	 * @param type
	 *          The type for which to determine properness.
	 * @return True if the given type is proper, false otherwise.
	 */
	public static boolean isProperType(Type type) {
		if (type instanceof Class<?>) {
			return true;
		}

		if (type instanceof InferenceVariable) {
			return false;
		}

		IdentityProperty<Boolean> proper = new IdentityProperty<>(true);

		new TypeVisitor() {
			@Override
			public synchronized final void visit(Collection<? extends Type> types) {
				if (proper.get()) {
					Iterator<? extends Type> typeIterator = types.iterator();
					while (typeIterator.hasNext() && proper.get()) {
						visit(typeIterator.next());
					}
				}
			}

			@Override
			protected void visitParameterizedType(ParameterizedType type) {
				visit(type.getActualTypeArguments());
				visit(type.getOwnerType());
			}

			@Override
			protected void visitGenericArrayType(GenericArrayType type) {
				visit(type.getGenericComponentType());
			}

			@Override
			protected void visitWildcardType(WildcardType type) {
				visit(type.getUpperBounds());
				visit(type.getLowerBounds());
			}

			@Override
			protected void visitTypeVariableCapture(TypeVariableCapture type) {}

			@Override
			protected void visitTypeVariable(TypeVariable<?> type) {}

			@Override
			protected void visitInferenceVariable(InferenceVariable type) {
				proper.set(false);
			}

			@Override
			protected void visitIntersectionType(IntersectionType type) {
				visit(type.getTypes());
			}
		}.visit(type);

		return proper.get();
	}
}
