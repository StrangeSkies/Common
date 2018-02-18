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
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.InferenceVariableBoundsImpl.BoundKind;
import uk.co.strangeskies.utility.DeepCopyable;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A bound set as described in chapter 18 of the Java 8 language specification.
 * (Note that some sorts of bounds present in the document are missing from this
 * implementation, as this API is not intended to offer the full capabilities of
 * the compiler with respect to method references and closures.)
 * 
 * <p>
 * A bound set contains a number of {@link InferenceVariable} instances, and
 * maintains a set of bounds between them and between other types. Types which
 * are not inference variables, and do not mention inference variables, are
 * considered <em>proper types</em>.
 * 
 * <p>
 * Note that instances of {@link InferenceVariable} which are not contained
 * within a bound set are not considered inference variables within that
 * context, and are treated as proper types. Inference variables are considered
 * contained within a bound set if they were added through
 * {@link #withInferenceVariables(Collection)} , or as part of a capture
 * conversion added through a {@link IncorporationTarget} belonging to that
 * bound set.
 * 
 * <p>
 * The types of bounds which may be included in a bound set are as follows:
 * 
 * <ul>
 * <li>Equalities between inference variables and other types, which may or may
 * not be inference variables.</li>
 * <li>Upper bounds on inference variables, to types which may or may not
 * themselves be inference variables.</li>
 * <li>Lower bounds on inference variables, from types which may or may not
 * themselves be inference variables.</li>
 * <li>Instances of {@link CaptureConversion} which mention inference variables.
 * </li>
 * <li>The bound 'false', typically meaning that a type inference attempt has
 * failed.</li>
 * </ul>
 * 
 * <p>
 * An equality bound between an inference variable and a <em>proper type</em> is
 * considered that inference variable's <em>instantiation</em>.
 * 
 * <p>
 * When you add such a bound to a bound set, it may imply the addition of
 * further bounds, or the reduction of any number of {@link ConstraintFormula}
 * instances into the bound set.
 * 
 * <p>
 * Bound sets, along with the processes of incorporation and reduction
 * described, are typically used to facilitate inference of the type arguments
 * of a generic method invocations, and to resolve overloads for such
 * invocations between multiple methods when some are generic. This
 * implementation therefore allows us to type check and resolve such an
 * invocations at runtime.
 * 
 * <p>
 * We may also employ these processes towards other ends, such as type checking
 * for data serialization formats and libraries, injection frameworks, etc.,
 * which may have slightly different rules and requirements to generic method
 * invocation. There are also applications further outside these areas, such as
 * inference of the type arguments of a generic supertype of a given type.
 * 
 * @author Elias N Vasylenko
 */
public class BoundSet implements DeepCopyable<BoundSet> {
	/**
	 * Consumer of different sorts of bounds which can be a applied to inference
	 * variables, as per chapter 18 of the Java 8 language specification.
	 * 
	 * @author Elias N Vasylenko
	 */
	public class IncorporationTarget {
		/**
		 * Derive a new bound set containing the given equality. The receiving bound
		 * set will not be mutated.
		 * 
		 * <p>
		 * If one or both of the arguments passed are considered inference variables
		 * within the enclosing BoundSet, the appropriate equality bound is added
		 * and further bounds are inferred as per the Java language specification.
		 * Otherwise, the invocation has no effect.
		 * 
		 * @param first
		 *          the first of two types whose equality we wish to assert
		 * @param second
		 *          the second of two types whose equality we wish to assert
		 * @return the derived bound set
		 */
		public BoundSet equality(Type first, Type second) {
			BoundSet bounds = copyInternal();
			bounds.incorporateEquality(first, second);
			return bounds;
		}

		/**
		 * Derive a new bound set containing the given subtype. The receiving bound
		 * set will not be mutated.
		 * 
		 * <p>
		 * If one or both of the arguments passed are considered inference variables
		 * within the enclosing BoundSet, the appropriate subtype bound is added and
		 * further bounds are inferred as per the Java language specification.
		 * Otherwise, the invocation has no effect.
		 * 
		 * @param subtype
		 *          a type which we wish to assert is a subtype of another
		 * @param supertype
		 *          a type which we wish to assert is a supertype of another
		 * @return the derived bound set
		 */
		public BoundSet subtype(Type subtype, Type supertype) {
			BoundSet bounds = copyInternal();
			bounds.incorporateSubtype(subtype, supertype);
			return bounds;
		}

		/**
		 * Derive a new bound set containing the given capture conversion. The
		 * receiving bound set will not be mutated.
		 * 
		 * <p>
		 * The given capture conversion is added to the enclosing bound set, and
		 * further bounds may be inferred as per the Java language specification.
		 * 
		 * @param captureConversion
		 *          the capture conversion to be incorporated into the bound set
		 * @return the derived bound set
		 */
		public BoundSet captureConversion(CaptureConversion captureConversion) {
			BoundSet bounds = copyInternal();
			bounds.incorporateCaptureConversion(captureConversion);
			return bounds;
		}

		/**
		 * Derive a new bound set containing falsehood. The receiving bound set will
		 * not be mutated.
		 * 
		 * <p>
		 * False is added to the bound set, invalidating it, and an exception is
		 * thrown describing the problem.
		 * 
		 * @param message
		 *          message detailing the cause of the problem
		 * @return the derived bound set
		 */
		public BoundSet falsehood(String message) {
			BoundSet bounds = copyInternal();
			bounds.incorporateFalsehood(message);
			return bounds;
		}
	}

	private static final BoundSet EMPTY = new BoundSet();

	private final HashMap<InferenceVariable, InferenceVariableBoundsImpl> inferenceVariableBounds;
	private final Set<CaptureConversion> captureConversions;

	/**
	 * Create an empty bound set.
	 */
	private BoundSet() {
		inferenceVariableBounds = new HashMap<>();
		captureConversions = new HashSet<>();
	}

	@SuppressWarnings("unchecked")
	public BoundSet(BoundSet boundSet) {
		inferenceVariableBounds = (HashMap<InferenceVariable, InferenceVariableBoundsImpl>) boundSet.inferenceVariableBounds
				.clone();

		inferenceVariableBounds.replaceAll((k, v) -> new InferenceVariableBoundsImpl(this, v));
		captureConversions = new HashSet<>(boundSet.captureConversions);
	}

	public static BoundSet emptyBoundSet() {
		return EMPTY;
	}

	/**
	 * Create a copy of an existing bound set. All the inference variables
	 * contained within the given bound set will also be contained in the new
	 * bound set, and all the bounds on them will also be copied. Subsequent
	 * modifications to the given bound set will not affect the new one, and vice
	 * versa.
	 */
	@Override
	public BoundSet copy() {
		return this;
	}

	protected BoundSet copyInternal() {
		return new BoundSet(this);
	}

	/**
	 * Create a copy of an existing bound set. All the inference variables
	 * contained within the given bound set will be substituted for new inference
	 * variables in the new bound set, and all the bounds on them will be
	 * substituted for equivalent bounds. Any such inference variable
	 * substitutions made will be put into the given map.
	 * <p>
	 * Inference variables which already have proper instantiations may not be
	 * substituted, as this is generally unnecessary in practice and so avoiding
	 * it can save time.
	 * 
	 * @param isomorphism
	 *          an isomorphism for inference variables
	 * @return A deep copy of this bound set.
	 */
	@Override
	public BoundSet deepCopy(Isomorphism isomorphism) {
		/*
		 * Substitutions of inference variables:
		 */
		getInferenceVariables().forEach(
				i -> isomorphism.byIdentity().getMapping(i, t -> new InferenceVariable(t.getName())));

		return withInferenceVariableSubstitution(isomorphism);
	}

	protected BoundSet withInferenceVariableSubstitution(Isomorphism isomorphism) {
		if (isomorphism.byIdentity().isEmpty())
			return copy();

		BoundSet copy = new BoundSet();
		/*
		 * Substitutions of capture conversions:
		 */
		for (CaptureConversion captureConversion : captureConversions) {
			captureConversion = isomorphism
					.byIdentity()
					.getMapping(captureConversion, c -> c.withInferenceVariableSubstitution(isomorphism));

			copy.captureConversions.add(captureConversion);
		}

		for (Entry<InferenceVariable, InferenceVariableBoundsImpl> inferenceVariable : inferenceVariableBounds
				.entrySet()) {
			copy.addInferenceVariableBounds(
					(InferenceVariable) isomorphism.byIdentity().getMapping(inferenceVariable.getKey()),
					inferenceVariable.getValue().withInferenceVariableSubstitution(isomorphism).copyInto(
							copy));
		}

		return copy;
	}

	protected void incorporateFalsehood(String message) {
		throw new ReflectionException(REFLECTION_PROPERTIES.invalidBoundSet(message, BoundSet.this));
	}

	protected void incorporateEquality(Type first, Type second) {
		if (!first.equals(second)) {
			try {
				if (first instanceof InferenceVariable) {
					inferenceVariableBounds.get(first).putBound(BoundKind.EQUAILTY, second);
				} else if (second instanceof InferenceVariable) {
					inferenceVariableBounds.get(second).putBound(BoundKind.EQUAILTY, first);
				}
			} catch (Exception e) {
				throw new ReflectionException(
						REFLECTION_PROPERTIES.invalidEquality(first, second, BoundSet.this),
						e);
			}
		}
	}

	protected void incorporateSubtype(Type subtype, Type supertype) {
		if (!subtype.equals(supertype)) {
			try {
				if (subtype instanceof InferenceVariable)
					inferenceVariableBounds.get(subtype).putBound(BoundKind.UPPER, supertype);

				if (supertype instanceof InferenceVariable)
					inferenceVariableBounds.get(supertype).putBound(BoundKind.LOWER, subtype);
			} catch (Exception e) {
				throw new ReflectionException(
						REFLECTION_PROPERTIES.invalidSubtype(subtype, supertype, BoundSet.this),
						e);
			}
		}
	}

	protected void incorporateCaptureConversion(CaptureConversion captureConversion) {
		try {
			captureConversions.add(captureConversion);

			/*
			 * When a bound set contains a bound of the form G<α1, ..., αn> =
			 * capture(G<A1, ..., An>), new bounds are implied and new constraint
			 * formulas may be implied, as follows.
			 * 
			 * Let P1, ..., Pn represent the type parameters of G and let B1, ..., Bn
			 * represent the bounds of these type parameters. Let θ represent the
			 * substitution [P1:=α1, ..., Pn:=αn]. Let R be a type that is not an
			 * inference variable (but is not necessarily a proper type).
			 * 
			 * A set of bounds on α1, ..., αn is implied, constructed from the
			 * declared bounds of P1, ..., Pn as specified in §18.1.3.
			 * 
			 * In addition, for all i (1 ≤ i ≤ n):
			 */
			for (InferenceVariable inferenceVariable : captureConversion.getInferenceVariables()) {
				InferenceVariableBoundsImpl existingBounds = inferenceVariableBounds.get(inferenceVariable);

				InferenceVariableBoundsImpl bounds;
				if (existingBounds == null) {
					bounds = new InferenceVariableBoundsImpl(BoundSet.this, inferenceVariable);
					addInferenceVariableBounds(inferenceVariable, bounds);
				} else {
					bounds = existingBounds;
				}

				/*
				 * Recalculate existing dependencies on each inference variable due to
				 * capture, then add dependencies to all other inference variables
				 * mentioned by the capture.
				 */
				bounds.addCaptureConversion(captureConversion);

				Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);
				TypeVariable<?> capturedParmeter = captureConversion
						.getCapturedParameter(inferenceVariable);

				if (capturedArgument instanceof WildcardType) {
					/*
					 * If Ai is a wildcard of the form ?, or;
					 * 
					 * If Ai is a wildcard of the form ? extends T, or;
					 * 
					 * If Ai is a wildcard of the form ? super T:
					 */
					WildcardType capturedWildcard = (WildcardType) capturedArgument;

					bounds
							.getEqualities()
							.filter(equality -> !inferenceVariableBounds.containsKey(equality))
							.forEach(equality -> bounds.incorporateCapturedEquality(capturedWildcard, equality));

					bounds
							.getUpperBounds()
							.filter(upperBound -> !inferenceVariableBounds.containsKey(upperBound))
							.forEach(
									upperBound -> bounds.incorporateCapturedSubtype(
											captureConversion,
											capturedWildcard,
											capturedParmeter,
											upperBound));

					bounds
							.getLowerBounds()
							.filter(lowerBound -> !inferenceVariableBounds.containsKey(lowerBound))
							.forEach(
									lowerBound -> bounds.incorporateCapturedSupertype(capturedWildcard, lowerBound));
				} else
					/*
					 * If Ai is not a wildcard, then the bound αi = Ai is implied.
					 */
					withIncorporated().equality(inferenceVariable, capturedArgument);
			}
		} catch (Exception e) {
			throw new ReflectionException(
					REFLECTION_PROPERTIES.invalidCaptureConversion(captureConversion, BoundSet.this),
					e);
		}
	}

	/**
	 * @return A set of all inference variables contained by this bound set.
	 */
	public Stream<InferenceVariable> getInferenceVariables() {
		return inferenceVariableBounds.keySet().stream();
	}

	/**
	 * @return true if the set contains no actual bounds, false otherwise
	 */
	public boolean isEmpty() {
		return inferenceVariableBounds.isEmpty();
	}

	/**
	 * @return A set of all inference variables contained by this bound set.
	 */
	public Stream<InferenceVariableBounds> getInferenceVariableBounds() {
		return inferenceVariableBounds.values().stream().map(InferenceVariableBounds.class::cast);
	}

	/**
	 * @param inferenceVariable
	 *          An inference variable whose state we wish to query.
	 * @return A container representing the state of the given inference variable
	 *         with respect to its bounds.
	 */
	public InferenceVariableBounds getBoundsOn(InferenceVariable inferenceVariable) {
		return getBoundsOnImpl(inferenceVariable);
	}

	InferenceVariableBoundsImpl getBoundsOnImpl(InferenceVariable inferenceVariable) {
		return inferenceVariableBounds.get(inferenceVariable);
	}

	/**
	 * @return all capture conversion bounds contained within this bound set
	 */
	public Stream<CaptureConversion> getCaptureConversions() {
		return captureConversions.stream();
	}

	/**
	 * @param variables
	 *          inference variables mentioned by the capture conversions we wish
	 *          to identify
	 * @return all capture conversion bounds contained within this bound set
	 *         related to the given inference variables
	 */
	public Stream<CaptureConversion> getRelatedCaptureConversions(
			Collection<? extends InferenceVariable> variables) {
		return getCaptureConversions()
				.filter(c -> c.getInferenceVariables().stream().anyMatch(variables::contains));
	}

	/**
	 * @param type
	 *          the type we wish to classify
	 * @return true if the given type an inference variable within the context of
	 *         this bound set, false otherwise
	 */
	public boolean containsInferenceVariable(Type type) {
		return inferenceVariableBounds.containsKey(type);
	}

	private static StringBuilder comma(StringBuilder builder) {
		if (builder.length() > 0)
			builder.append(", ");

		return builder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		getCaptureConversions().forEach(c -> comma(builder).append(c));

		for (InferenceVariableBounds bounds : inferenceVariableBounds.values()) {
			String name = bounds.getInferenceVariable().getTypeName();

			bounds.getEqualities().forEach(
					equality -> comma(builder).append(name).append(" = ").append(equality.getTypeName()));
			bounds.getUpperBounds().forEach(
					supertype -> comma(builder).append(name).append(" <: ").append(supertype.getTypeName()));
			bounds.getLowerBounds().forEach(
					subtype -> comma(builder).append(subtype.getTypeName()).append(" <: ").append(name));
		}

		return builder.insert(0, "{ ").append(" }").toString();
	}

	/**
	 * @return A consumer through which bounds may be added to this bound set.
	 */
	public IncorporationTarget withIncorporated() {
		return new IncorporationTarget();
	}

	/**
	 * Incorporate each bound from this given bound set into the receiver bound
	 * set. Inference variables which are contained in the given bound set will
	 * also be contained within the receiver bound set after incorporation.
	 * 
	 * @param boundSet
	 *          The bound set whose bounds we wish to incorporate.
	 * @return the derived bound set
	 */
	public BoundSet withBounds(BoundSet boundSet) {
		if (boundSet.isEmpty())
			return this;

		if (isEmpty())
			return boundSet;

		BoundSet copy = copyInternal();
		copy.incorporate(boundSet);
		return copy;
	}

	private void incorporate(BoundSet boundSet) {
		if (boundSet.getInferenceVariables().findAny().isPresent()) {
			Set<InferenceVariable> newInferenceVariables = boundSet.inferenceVariableBounds.keySet();

			if (newInferenceVariables.stream().allMatch(i -> !inferenceVariableBounds.containsKey(i))) {
				/*
				 * All inference variables are unrelated, so we can just copy them
				 * directly in without worrying about implying any new bounds
				 */
				for (InferenceVariable inferenceVariable : newInferenceVariables) {
					InferenceVariableBoundsImpl filtered = boundSet
							.getBoundsOnImpl(inferenceVariable)
							.copyInto(this);
					addInferenceVariableBounds(inferenceVariable, filtered);
				}
			} else {
				/*
				 * Add the inference variables to this bound set.
				 */
				for (InferenceVariable inferenceVariable : newInferenceVariables) {
					addInferenceVariableImpl(inferenceVariable);
				}

				/*
				 * Incorporate their bounds.
				 */
				for (InferenceVariable inferenceVariable : newInferenceVariables) {
					InferenceVariableBounds bounds = boundSet.getBoundsOn(inferenceVariable);

					bounds.getEqualities().forEach(
							equality -> incorporateEquality(inferenceVariable, equality));

					bounds.getLowerBounds().forEach(
							lowerBound -> incorporateSubtype(lowerBound, inferenceVariable));

					bounds.getUpperBounds().forEach(
							upperBound -> incorporateSubtype(inferenceVariable, upperBound));

					CaptureConversion captureConversion = bounds.getCaptureConversion();
					if (captureConversion != null)
						incorporateCaptureConversion(captureConversion);
				}
			}
		}
	}

	/**
	 * Add an inference variable to this bound set such that bounds can be
	 * inferred over it.
	 * 
	 * @param inferenceVariables
	 *          the inference variables to add to the bound set
	 * @return the newly added bounds, or the existing bounds
	 */
	public BoundSet withInferenceVariables(InferenceVariable... inferenceVariables) {
		return withInferenceVariables(asList(inferenceVariables));
	}

	/**
	 * Add an inference variable to this bound set such that bounds can be
	 * inferred over it.
	 * 
	 * @param inferenceVariables
	 *          the inference variable to add to the bound set
	 * @return the newly added bounds, or the existing bounds
	 */
	public BoundSet withInferenceVariables(
			Collection<? extends InferenceVariable> inferenceVariables) {
		if (inferenceVariables.isEmpty()) {
			return this;
		}

		BoundSet bounds = copyInternal();
		for (InferenceVariable inferenceVariable : inferenceVariables) {
			bounds.addInferenceVariableImpl(inferenceVariable);
		}
		return bounds;
	}

	private void addInferenceVariableImpl(InferenceVariable inferenceVariable) {
		if (!inferenceVariableBounds.containsKey(inferenceVariable)) {
			addInferenceVariableBounds(
					inferenceVariable,
					new InferenceVariableBoundsImpl(this, inferenceVariable));
		}
	}

	private InferenceVariableBoundsImpl addInferenceVariableBounds(
			InferenceVariable inferenceVariable,
			InferenceVariableBoundsImpl bounds) {
		if (bounds.getBoundSet() != this || inferenceVariableBounds.containsKey(inferenceVariable)) {
			/*
			 * these conditions should be cleared before invocation
			 */
			throw new AssertionError();
		} else {
			inferenceVariableBounds.put(inferenceVariable, bounds);
		}

		return bounds;
	}

	public BoundSet withInstantiations(Map<InferenceVariable, Type> typeVariableCaptures) {
		BoundSet copy = copyInternal();

		for (Map.Entry<InferenceVariable, Type> inferenceVariable : typeVariableCaptures.entrySet()) {
			try {
				copy.incorporateEquality(inferenceVariable.getKey(), inferenceVariable.getValue());
			} catch (ReflectionException e) {
				throw new ReflectionException(
						REFLECTION_PROPERTIES.cannotCaptureInferenceVariable(
								inferenceVariable.getKey(),
								inferenceVariable.getValue(),
								copy),
						e);
			}
		}

		/*
		 * TODO detect when to remove capture conversions automatically (when all
		 * inference variables they mention are instantiated?). Currently, when
		 * instantiations are given manually and one at a time the capture
		 * conversions are left behind.
		 * 
		 * TODO perhaps better to disallow individual instantiation of inference
		 * variables when a type variable capture mentioning them is present...
		 */

		copy.removeCaptureConversions(
				getRelatedCaptureConversions(typeVariableCaptures.keySet()).collect(toList()));

		return copy;
	}

	private void removeCaptureConversions(
			Collection<? extends CaptureConversion> captureConversions) {
		this.captureConversions.removeAll(captureConversions);

		for (CaptureConversion captureConversion : captureConversions)
			for (InferenceVariable inferenceVariable : captureConversion.getInferenceVariables())
				getBoundsOnImpl(inferenceVariable).removeCaptureConversion();
	}
}
