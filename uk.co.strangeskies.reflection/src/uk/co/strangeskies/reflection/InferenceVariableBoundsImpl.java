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

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.InferenceVariableBoundsImpl.BoundKind.EQUAILTY;
import static uk.co.strangeskies.reflection.InferenceVariableBoundsImpl.BoundKind.LOWER;
import static uk.co.strangeskies.reflection.InferenceVariableBoundsImpl.BoundKind.UPPER;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utility.Isomorphism;

class InferenceVariableBoundsImpl implements InferenceVariableBounds {
	enum BoundKind {
		UPPER, LOWER, EQUAILTY
	}

	static class Bound {
		private final BoundKind bound;
		private final Type type;
		private final List<InferenceVariable> mentions;

		public Bound(BoundKind bound, Type type) {
			this.bound = bound;
			this.type = type;
			this.mentions = InferenceVariable.getMentionedBy(type).collect(toList());
		}

		public BoundKind getKind() {
			return bound;
		}

		public Type getType() {
			return type;
		}

		public List<InferenceVariable> getMentions() {
			return mentions;
		}

		public boolean isProper() {
			return mentions.isEmpty();
		}
	}

	private final BoundSet boundSet;

	private final InferenceVariable inferenceVariable;
	private Type instantiation;

	private HashMap<Type, Bound> bounds;

	private CaptureConversion capture;

	private HashSet<InferenceVariable> remainingDependencies;

	public InferenceVariableBoundsImpl(BoundSet boundSet, InferenceVariable inferenceVariable) {
		this.boundSet = boundSet;
		this.inferenceVariable = inferenceVariable;

		bounds = new HashMap<>();

		bounds.put(inferenceVariable, new Bound(EQUAILTY, inferenceVariable));
	}

	@SuppressWarnings("unchecked")
	public InferenceVariableBoundsImpl(BoundSet boundSet, InferenceVariableBoundsImpl that) {
		this.boundSet = boundSet;
		this.inferenceVariable = that.inferenceVariable;
		this.instantiation = that.instantiation;

		this.bounds = (HashMap<Type, Bound>) that.bounds.clone();

		this.capture = that.capture;

		if (that.remainingDependencies != null) {
			this.remainingDependencies = (HashSet<InferenceVariable>) that.remainingDependencies.clone();
		} else {
			this.remainingDependencies = null;
		}
	}

	public void putBound(BoundKind boundKind, Type type) {
		Bound existingBound = bounds.get(type);

		if (existingBound != null) {
			BoundKind kind = existingBound.getKind();

			if (kind == boundKind || kind == EQUAILTY) {
				return;
			}

			boundKind = EQUAILTY;
		}

		Bound bound = new Bound(boundKind, type);
		bounds.put(type, bound);

		invalidateDependencies();

		switch (boundKind) {
		case EQUAILTY:
			addEquality(bound);
			break;

		case UPPER:
			addUpperBound(bound);
			break;

		case LOWER:
			addLowerBound(bound);
			break;

		default:
			break;
		}
	}

	BoundSet getBoundSet() {
		return boundSet;
	}

	protected InferenceVariableBoundsImpl copyInto(BoundSet boundSet) {
		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(boundSet, inferenceVariable);

		copy.capture = capture;

		if (boundSet.containsInferenceVariable(inferenceVariable))
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotCopyInferenceVariable(inferenceVariable, boundSet));

		for (Bound bound : bounds.values()) {
			if (bound.getKind() == EQUAILTY) {
				if (boundSet.containsInferenceVariable(bound.getType())) {
					InferenceVariableBoundsImpl bounds = boundSet
							.getBoundsOnImpl((InferenceVariable) bound.getType());

					copy.bounds = bounds.bounds;

					copy.instantiation = bounds.instantiation;

					return copy;
				}
			}
		}

		copy.bounds.putAll(bounds);

		copy.instantiation = instantiation;

		return copy;
	}

	protected InferenceVariableBoundsImpl withInferenceVariableSubstitution(Isomorphism isomorphism) {
		InferenceVariable inferenceVariableSubstitution = (InferenceVariable) isomorphism
				.byIdentity()
				.getMapping(inferenceVariable);

		InferenceVariableBoundsImpl copy = new InferenceVariableBoundsImpl(
				boundSet,
				inferenceVariableSubstitution);

		copy.addBoundsWithTypeSubstitution(this, isomorphism);

		return copy;
	}

	private void addBoundsWithTypeSubstitution(
			InferenceVariableBoundsImpl inferenceVariableBounds,
			Isomorphism isomorphism) {
		TypeSubstitution substitution = new TypeSubstitution().withIsomorphism(isomorphism);

		inferenceVariableBounds.bounds
				.replaceAll((t, b) -> new Bound(b.getKind(), substitution.resolve(t)));

		capture = (CaptureConversion) isomorphism.byIdentity().getMapping(
				inferenceVariableBounds.capture);
	}

	@Override
	public CaptureConversion getCaptureConversion() {
		return capture;
	}

	public void addCaptureConversion(CaptureConversion captureConversion) {
		if (capture != null)
			throw new ReflectionException(
					REFLECTION_PROPERTIES.cannotCaptureMultipleTimes(inferenceVariable, capture, captureConversion));
		else
			capture = captureConversion;

		invalidateDependencies();
	}

	public void removeCaptureConversion() {
		if (!getInstantiation().isPresent())
			throw new IllegalStateException(
					"Capture conversion '" + capture + "' should not be removed with no instantiation for '"
							+ inferenceVariable + "' in bound set '" + boundSet + "'");

		if (capture == null)
			throw new IllegalStateException("Attempt to remove missing capture from '" + capture + "'");

		/*
		 * No need to modify dependencies here, as capture conversion will only be
		 * removed alongside an instantiation, so dependencies will be cleared
		 * through that mechanism.
		 */
		capture = null;
	}

	@Override
	public InferenceVariable getInferenceVariable() {
		return inferenceVariable;
	}

	private Stream<Type> getBounds(BoundKind kind) {
		return bounds.values().stream().filter(b -> b.getKind() == kind).map(Bound::getType);
	}

	@Override
	public Stream<Type> getEqualities() {
		return getBounds(EQUAILTY);
	}

	@Override
	public Stream<Type> getUpperBounds() {
		return getBounds(UPPER);
	}

	@Override
	public Stream<Type> getLowerBounds() {
		return getBounds(LOWER);
	}

	@Override
	public Optional<Type> getInstantiation() {
		return Optional.ofNullable(instantiation);
	}

	private void invalidateDependencies() {
		boundSet
				.getInferenceVariables()
				.map(boundSet::getBoundsOnImpl)
				.filter(
						b -> b.remainingDependencies != null
								&& b.remainingDependencies.contains(inferenceVariable))
				.forEach(b -> b.remainingDependencies = null);
		remainingDependencies = null;
	}

	@Override
	public Stream<InferenceVariable> getRemainingDependencies() {
		Set<InferenceVariableBoundsImpl> recalculated = new HashSet<>();

		if (remainingDependencies == null) {
			recalculateRemainingDependencies();
			recalculated.add(this);
		}

		/*
		 * Inference variables which are part of capture conversions may create
		 * dependencies on this, so we should recalculate any which are dirty.
		 */
		Stream
				.concat(
						remainingDependencies.stream(),
						boundSet.getCaptureConversions().flatMap(c -> c.getInferenceVariables().stream()))
				.distinct()
				.filter(d -> !d.equals(inferenceVariable))
				.map(boundSet::getBoundsOnImpl)
				.filter(b -> b.remainingDependencies == null && !b.getInstantiation().isPresent())
				.forEach(d -> {
					d.recalculateRemainingDependencies();
					recalculated.add(d);
				});

		/*
		 * An inference variable α depends on the resolution of an inference
		 * variable β if there exists an inference variable γ such that α depends on
		 * the resolution of γ and γ depends on the resolution of β.
		 */
		boolean added;
		do {
			added = false;
			for (InferenceVariableBoundsImpl bounds : recalculated) {
				for (InferenceVariable dependency : new ArrayList<>(bounds.remainingDependencies)) {
					if (bounds.remainingDependencies.addAll(
							boundSet.getBoundsOnImpl(dependency).getRemainingDependencies().collect(toList()))) {
						added = true;
					}
				}
			}
		} while (added);

		return remainingDependencies.stream();
	}

	private void recalculateRemainingDependencies() {
		if (remainingDependencies == null) {
			remainingDependencies = new HashSet<>();
			if (!getInstantiation().isPresent()) {
				/*
				 * An inference variable α depends on the resolution of itself.
				 */
				remainingDependencies.add(inferenceVariable);

				Stream<InferenceVariableBoundsImpl> mentions = bounds
						.values()
						.stream()
						.map(Bound::getMentions)
						.flatMap(Collection::stream)
						.map(boundSet::getBoundsOnImpl)
						.filter(mention -> !mention.getInstantiation().isPresent());

				if (capture != null) {
					/*
					 * An inference variable α appearing on the left-hand side of a bound
					 * of the form G<..., α, ...> = capture(G<...>) depends on the
					 * resolution of every other inference variable mentioned in this
					 * bound (on both sides of the = sign).
					 */

					capture.getInferenceVariablesMentioned().forEach(remainingDependencies::add);

					/*
					 * Given a bound of one of the following forms, where T is either an
					 * inference variable β or a type that mentions β:
					 * 
					 * α = T
					 * 
					 * α <: T
					 * 
					 * T = α
					 * 
					 * T <: α
					 * 
					 * If α appears on the left-hand side of another bound of the form
					 * G<..., α, ...> = capture(G<...>), then β depends on the resolution
					 * of α...
					 */
					mentions.forEach(b -> b.remainingDependencies.add(inferenceVariable));
				} else {
					/*
					 * ...Otherwise, α depends on the resolution of β.
					 */
					mentions.map(b -> b.inferenceVariable).forEach(remainingDependencies::add);
				}
			}
		}
	}

	protected void addEquality(Bound bound) {
		if (bound.getType() instanceof InferenceVariable) {
			addInferenceVariableEquality((InferenceVariable) bound.getType());
		} else {
			addTypeEquality(bound);
		}
	}

	protected void addTypeEquality(Bound bound) {
		List<Bound> bounds = new ArrayList<>(this.bounds.values());

		if (bound.isProper()) {
			/*
			 * An instantiation has been found.
			 */

			if (getInstantiation().isPresent()) {
				/*
				 * If we already have an instantiation, make sure the new one is equal
				 * to it...
				 */
				new ConstraintFormula(Kind.EQUALITY, bound.getType(), getInstantiation().get())
						.reduce(emptyBoundSet());
			} else {
				/*
				 * ...Otherwise, make sure there are no captures present, and remove all
				 * remaining dependencies on this inference variable, and all those
				 * equal to it.
				 */
				if (capture != null)
					boundSet.withIncorporated().falsehood(
							"Cannot add instantiation with capture conversion present: " + this);

				instantiation = bound.getType();

				bounds
						.stream()
						.filter(b -> b.getKind() == EQUAILTY)
						.map(Bound::getType)
						.filter(InferenceVariable.class::isInstance)
						.map(InferenceVariable.class::cast)
						.map(boundSet::getBoundsOnImpl)
						.forEach(equality -> {
							equality.instantiation = bound.getType();
						});
			}
		}

		for (Bound existingBound : bounds) {
			if (existingBound != bound) {
				switch (existingBound.getKind()) {
				/*
				 * α = S and α = T imply ‹S = T›
				 */
				case EQUAILTY:
					incorporateTransitiveEquality(bound.getType(), existingBound.getType());
					break;

				/*
				 * α = S and α <: T imply ‹S <: T›
				 */
				case UPPER:
					incorporateSubtypeSubstitution(bound.getType(), existingBound.getType());
					break;

				/*
				 * α = S and T <: α imply ‹T <: S›
				 */
				case LOWER:
					incorporateSupertypeSubstitution(bound.getType(), existingBound.getType());
					break;
				}
			}
		}

		if (bound.isProper()) {
			for (InferenceVariable other : boundSet.getInferenceVariables().collect(toList())) {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

				for (Bound existingBound : new ArrayList<>(otherBounds.bounds.values())) {
					if (existingBound != bound) {
						/*
						 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
						 */
						switch (existingBound.getKind()) {
						case EQUAILTY:
							incorporateProperEqualitySubstitution(
									bound.getType(),
									otherBounds.inferenceVariable,
									existingBound);
							break;

						/*
						 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
						 */
						case UPPER:
							incorporateProperSubtypeSubstitution(
									bound.getType(),
									otherBounds.inferenceVariable,
									existingBound);
							break;
						case LOWER:
							incorporateProperSupertypeSubstitution(
									bound.getType(),
									existingBound,
									otherBounds.inferenceVariable);
							break;
						}
					}
				}
			}
		}

		for (CaptureConversion captureConversion : boundSet.getCaptureConversions().collect(toList())) {
			Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);

			if (capturedArgument instanceof WildcardType)
				incorporateCapturedEquality((WildcardType) capturedArgument, bound.getType());
		}
	}

	protected void addInferenceVariableEquality(InferenceVariable type) {
		addInferenceVariableEqualityImpl(type);
		boundSet.getBoundsOnImpl(type).addInferenceVariableEqualityImpl(inferenceVariable);
	}

	protected void addInferenceVariableEqualityImpl(InferenceVariable type) {
		List<Bound> bounds = new ArrayList<>(this.bounds.values());

		for (Bound existingBound : bounds) {
			if (existingBound.getType() != type) {
				switch (existingBound.getKind()) {
				case UPPER:
					/*-
					 * α = S and α <: T imply ‹S <: T›
					 */
					incorporateSubtypeSubstitution(type, existingBound.getType());
					break;
				case LOWER:

					/*-
					 * α = S and T <: α imply ‹T <: S›
					 */
					incorporateSupertypeSubstitution(type, existingBound.getType());
					break;
				case EQUAILTY:
					/*-
					 * α = S and α = T imply ‹S = T›
					 */
					incorporateTransitiveEquality(type, existingBound.getType());
					break;
				}
			}
		}
	}

	protected void addUpperBound(Bound bound) {
		Type type = bound.getType();
		List<Bound> bounds = new ArrayList<>(this.bounds.values());

		for (Bound existingBound : bounds) {
			if (existingBound != bound) {
				switch (existingBound.getKind()) {
				case EQUAILTY:
					/*
					 * α = S and α <: T imply ‹S <: T›
					 */
					incorporateSubtypeSubstitution(existingBound.getType(), type);
					break;
				case LOWER:
					/*
					 * S <: α and α <: T imply ‹S <: T›
					 */
					incorporateTransitiveSubtype(existingBound.getType(), type);
					break;
				default:
					break;
				}
			}
		}

		if (!(type instanceof InferenceVariable)) {
			/*
			 * Capture conversions...
			 */
			boundSet.getCaptureConversions().forEach(captureConversion -> {
				Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);
				TypeVariable<?> capturedParmeter = captureConversion
						.getCapturedParameter(inferenceVariable);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSubtype(
							captureConversion,
							(WildcardType) capturedArgument,
							capturedParmeter,
							type);
			});

			/*
			 * When a bound set contains a pair of bounds α <: S and α <: T, and there
			 * exists a supertype of S of the form G<S1, ..., Sn> and a supertype of T
			 * of the form G<T1, ..., Tn> (for some generic class or interface, G),
			 * then for all i (1 ≤ i ≤ n), if Si and Ti are types (not wildcards), the
			 * constraint formula ‹Si = Ti› is implied.
			 */

			for (Bound existingBound : bounds) {
				if (existingBound != bound && existingBound.getKind() == UPPER) {
					incorporateSupertypeParameterizationEquality(type, existingBound.getType());
				}
			}

			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			boundSet.getInferenceVariables().forEach(other -> {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

				for (Bound equality : new ArrayList<>(otherBounds.bounds.values()))
					if (equality.getKind() == EQUAILTY && equality.isProper())
						boundSet
								.getBoundsOnImpl(otherBounds.inferenceVariable)
								.incorporateProperSubtypeSubstitution(equality.getType(), inferenceVariable, bound);
			});
		}
	}

	protected void addLowerBound(Bound bound) {
		Type type = bound.getType();
		List<Bound> bounds = new ArrayList<>(this.bounds.values());

		for (Bound existingBound : bounds) {
			if (existingBound != bound) {
				switch (existingBound.getKind()) {
				case EQUAILTY:
					/*
					 * α = S and T <: α imply ‹T <: S›
					 */
					incorporateSupertypeSubstitution(bound.getType(), type);
					break;
				case UPPER:
					/*
					 * S <: α and α <: T imply ‹S <: T›
					 */
					incorporateTransitiveSubtype(type, bound.getType());
					break;
				default:
					break;
				}
			}
		}

		if (!(type instanceof InferenceVariable)) {
			/*
			 * Capture conversions...
			 */
			boundSet.getCaptureConversions().forEach(captureConversion -> {
				Type capturedArgument = captureConversion.getCapturedArgument(inferenceVariable);

				if (capturedArgument instanceof WildcardType)
					incorporateCapturedSupertype((WildcardType) capturedArgument, type);
			});

			/*
			 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
			 */
			boundSet.getInferenceVariables().forEach(other -> {
				InferenceVariableBoundsImpl otherBounds = boundSet.getBoundsOnImpl(other);

				for (Bound equality : new ArrayList<>(otherBounds.bounds.values()))
					if (equality.getKind() == EQUAILTY && equality != bound && equality.isProper())
						boundSet
								.getBoundsOnImpl(otherBounds.inferenceVariable)
								.incorporateProperSupertypeSubstitution(
										equality.getType(),
										bound,
										inferenceVariable);
			});
		}
	}

	/*
	 * (In this section, S and T are inference variables or types, and U is a
	 * proper type. For conciseness, a bound of the form α = T may also match a
	 * bound of the form T = α.)
	 * 
	 * When a bound set contains a pair of bounds that match one of the following
	 * rules, a new constraint formula is implied:
	 */

	/*
	 * α = S and α = T imply ‹S = T›
	 */
	private void incorporateTransitiveEquality(Type S, Type T) {
		new ConstraintFormula(Kind.EQUALITY, S, T).reduceInPlace(boundSet);
	}

	/*
	 * α = S and α <: T imply ‹S <: T›
	 */
	private void incorporateSubtypeSubstitution(Type S, Type T) {
		new ConstraintFormula(Kind.SUBTYPE, S, T).reduceInPlace(boundSet);
	}

	/*
	 * α = S and T <: α imply ‹T <: S›
	 */
	private void incorporateSupertypeSubstitution(Type S, Type T) {
		new ConstraintFormula(Kind.SUBTYPE, T, S).reduceInPlace(boundSet);
	}

	/*
	 * S <: α and α <: T imply ‹S <: T›
	 */
	private void incorporateTransitiveSubtype(Type S, Type T) {
		new ConstraintFormula(Kind.SUBTYPE, S, T).reduceInPlace(boundSet);
	}

	/*
	 * α = U and S = T imply ‹S[α:=U] = T[α:=U]›
	 */
	private void incorporateProperEqualitySubstitution(Type U, InferenceVariable S, Bound T) {
		if (T.getMentions().contains(inferenceVariable)) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

			Type tType = resolver.resolve(T.getType());

			new ConstraintFormula(Kind.EQUALITY, S, tType).reduceInPlace(boundSet);
		}
	}

	/*
	 * α = U and S <: T imply ‹S[α:=U] <: T[α:=U]›
	 */
	private void incorporateProperSubtypeSubstitution(Type U, InferenceVariable S, Bound T) {
		if (T.getMentions().contains(inferenceVariable)) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

			Type tType = resolver.resolve(T.getType());

			new ConstraintFormula(Kind.SUBTYPE, S, tType).reduceInPlace(boundSet);
		}
	}

	private void incorporateProperSupertypeSubstitution(Type U, Bound S, InferenceVariable T) {
		if (S.getMentions().contains(inferenceVariable)) {
			TypeSubstitution resolver = new TypeSubstitution().where(inferenceVariable, U);

			Type sType = resolver.resolve(S.getType());

			new ConstraintFormula(Kind.SUBTYPE, sType, T).reduceInPlace(boundSet);
		}
	}

	/*
	 * When a bound set contains a pair of bounds α <: S and α <: T, and there
	 * exists a supertype of S of the form G<S1, ..., Sn> and a supertype of T of
	 * the form G<T1, ..., Tn> (for some generic class or interface, G), then for
	 * all i (1 ≤ i ≤ n), if Si and Ti are types (not wildcards), the constraint
	 * formula ‹Si = Ti› is implied.
	 */
	private void incorporateSupertypeParameterizationEquality(Type S, Type T) {
		if (S.equals(T) || S.equals(Object.class) || T.equals(Object.class))
			return;

		if (S instanceof IntersectionType)
			for (Type itemS : ((IntersectionType) S).getTypes())
				incorporateSupertypeParameterizationEquality(itemS, T);
		else if (T instanceof IntersectionType)
			for (Type itemT : ((IntersectionType) T).getTypes())
				incorporateSupertypeParameterizationEquality(S, itemT);
		else
			new TypeVisitor() {
				@Override
				protected void visitClass(Class<?> type) {
					if (Types.isAssignable(T, type) && type.getTypeParameters().length > 0) {
						Type supertypeS = new TypeHierarchy(S).resolveSupertype(type);
						Type supertypeT = new TypeHierarchy(T).resolveSupertype(type);

						Iterator<Type> argumentsS = ParameterizedTypes
								.getAllTypeArguments((ParameterizedType) supertypeS)
								.map(Map.Entry::getValue)
								.iterator();

						Iterator<Type> argumentsT = ParameterizedTypes
								.getAllTypeArguments((ParameterizedType) supertypeT)
								.map(Map.Entry::getValue)
								.iterator();

						while (argumentsS.hasNext()) {
							Type argumentS = argumentsS.next();
							Type argumentT = argumentsT.next();

							if (!(argumentS instanceof WildcardType) && !(argumentT instanceof WildcardType))
								new ConstraintFormula(Kind.EQUALITY, argumentS, argumentT).reduceInPlace(boundSet);
						}
					} else {
						visit(type.getInterfaces());
						visit(type.getSuperclass());
					}
				}

				@Override
				protected void visitParameterizedType(ParameterizedType type) {
					visit(type.getRawType());
				}
			}.visit(S);
	}

	/*
	 * When a bound set contains a bound of the form G<α1, ..., αn> =
	 * capture(G<A1, ..., An>), new bounds are implied and new constraint formulas
	 * may be implied, as follows.
	 * 
	 * Let P1, ..., Pn represent the type parameters of G and let B1, ..., Bn
	 * represent the bounds of these type parameters. Let θ represent the
	 * substitution [P1:=α1, ..., Pn:=αn]. Let R be a type that is not an
	 * inference variable (but is not necessarily a proper type).
	 * 
	 * A set of bounds on α1, ..., αn is implied, constructed from the declared
	 * bounds of P1, ..., Pn as specified in §18.1.3.
	 * 
	 * In addition, for all i (1 ≤ i ≤ n):
	 */

	public void incorporateCapturedEquality(WildcardType A, Type R) {
		/*
		 * αi = R implies the bound false
		 */
		if (inferenceVariable.equals(R))
			boundSet.withIncorporated().falsehood("Cannot incorporate equality: " + this);
	}

	public void incorporateCapturedSubtype(
			CaptureConversion c,
			WildcardType A,
			TypeVariable<?> P,
			Type R) {
		TypeSubstitution θ = new TypeSubstitution();
		for (InferenceVariable variable : c.getInferenceVariables())
			θ = θ.where(c.getCapturedParameter(variable), variable);

		Type[] B = P.getBounds();

		if (A.getUpperBounds().length > 0) {
			Type[] T = A.getUpperBounds();

			/*
			 * If Ai is a wildcard of the form ? extends T:
			 */

			if (T.length == 0 || (T.length == 1 && T[0].equals(Object.class))) {
				/*
				 * If T is Object, then αi <: R implies the constraint formula ‹Bi θ <:
				 * R›
				 */
				new ConstraintFormula(Kind.SUBTYPE, θ.resolve(intersectionOf(B)), R)
						.reduceInPlace(boundSet);
			}

			if (B.length == 0 || (B.length == 1 && B[0].equals(Object.class))) {
				/*
				 * If Bi is Object, then αi <: R implies the constraint formula ‹T <: R›
				 */
				new ConstraintFormula(Kind.SUBTYPE, intersectionOf(T), R).reduceInPlace(boundSet);
			}
		} else if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			new ConstraintFormula(Kind.SUBTYPE, θ.resolve(intersectionOf(B)), R).reduceInPlace(boundSet);
		} else {
			/*
			 * If Ai is a wildcard of the form ?:
			 * 
			 * αi <: R implies the constraint formula ‹Bi θ <: R›
			 */
			new ConstraintFormula(Kind.SUBTYPE, θ.resolve(intersectionOf(B)), R).reduceInPlace(boundSet);
		}
	}

	public void incorporateCapturedSupertype(WildcardType A, Type R) {
		if (A.getLowerBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? super T:
			 * 
			 * R <: αi implies the constraint formula ‹R <: T›
			 */
			new ConstraintFormula(Kind.SUBTYPE, R, intersectionOf(A.getLowerBounds()))
					.reduceInPlace(boundSet);
		} else if (A.getUpperBounds().length > 0) {
			/*
			 * If Ai is a wildcard of the form ? extends T:
			 * 
			 * Else:
			 * 
			 * If Ai is a wildcard of the form ?:
			 * 
			 * R <: αi implies the bound false
			 */
			boundSet.withIncorporated().falsehood("Cannot incorporate supertype: " + this);
		}
	}
}
