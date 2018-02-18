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
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.EQUALITY;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.Types.wrapPrimitive;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * <p>
 * A constraint formula, as they are described in chapter 18 of that Java 8
 * language specification.
 * 
 * <p>
 * Roughly, a constraint formula describes an assertion of compatibility between
 * two types, with respect to a particular constraining relationship. This
 * relationship may be reduced into a number of secondary, tertiary, etc.
 * constraint formulae, and then ultimately into a number of bounds, which in
 * turn may be incorporated into a {@link BoundSet}.
 * 
 * @author Elias N Vasylenko
 */
public class ConstraintFormula {
	/**
	 * The kind of a constraint formula describes the nature of the constraint it
	 * represents.
	 * 
	 * @author Elias N Vasylenko
	 */
	public enum Kind {
		/**
		 * A loose compatibility constraint implies that two types be compatible within
		 * a loose invocation context, as described by
		 * {@link Types#isLooseInvocationContextCompatible(Type, Type)}.
		 */
		LOOSE_COMPATIBILILTY,
		/**
		 * A subtype constraint between two types implies that the first be assignable
		 * to the second.
		 */
		SUBTYPE,
		/**
		 * A containment constraint between two types implies that one contains the
		 * other, as described by {@link Types#isContainedBy(Type, Type)}.
		 */
		CONTAINMENT,
		/**
		 * An equality constraint between two types implies that they are exactly
		 * identical.
		 */
		EQUALITY
	}

	private final Kind kind;
	private final Type from, to;

	/**
	 * @param kind
	 *          the kind of the constraint formula to create
	 * @param from
	 *          the first type of the constraint formula
	 * @param to
	 *          the second type of the constraint formula
	 */
	public ConstraintFormula(Kind kind, Type from, Type to) {
		this.kind = kind;
		this.from = from;
		this.to = to;
	}

	@Override
	public String toString() {
		return kind + " between '" + from + "' and '" + to + "'";
	}

	/**
	 * Creates a {@link ConstraintFormula} and reduces it into the given
	 * {@link BoundSet}.
	 * 
	 * @param bounds
	 *          the bound set to reduce the created constraint formula into
	 * @return the constraint formula created
	 */
	public BoundSet reduce(BoundSet bounds) {
		bounds = bounds.copy();
		reduceInPlace(bounds);
		return bounds;
	}

	protected void reduceInPlace(BoundSet bounds) {
		try {
			logConstraint(this, bounds);

			switch (kind) {
			case LOOSE_COMPATIBILILTY:
				reduceLooseCompatibilityConstraint(bounds);
				break;
			case SUBTYPE:
				reduceSubtypeConstraint(bounds);
				break;
			case CONTAINMENT:
				reduceContainmentConstraint(bounds);
				break;
			case EQUALITY:
				reduceEqualityConstraint(bounds);
				break;
			default:
				throw new AssertionError();
			}
		} catch (Exception e) {
			throw new ReflectionException(REFLECTION_PROPERTIES.cannotReduceConstraint(this, bounds), e);
		}
	}

	private void logConstraint(ConstraintFormula constraintFormula, BoundSet boundSet) {
		// System.out.println(constraintFormula + " into '" + boundSet + "'.");
	}

	/*
	 * A constraint formula of the form ‹S → T› is reduced as follows:
	 */
	private void reduceLooseCompatibilityConstraint(BoundSet bounds) {
		Type from = this.from;

		/*
		 * TODO should this capture be lifted to the invocation?:
		 */
		if (from instanceof ParameterizedType)
			if (InferenceVariable.isProperType(from)) {
				from = TypeVariableCapture.captureWildcardArguments((ParameterizedType) from);
			} else {
				from = InferenceVariable.captureConversion((ParameterizedType) from, bounds);
			}

		if (InferenceVariable.isProperType(from) && InferenceVariable.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is
			 * compatible in a loose invocation context with T (§5.3), and false otherwise.
			 */
			if (!Types.isLooseInvocationContextCompatible(from, to)) {
				bounds.incorporateFalsehood("Incompatible proper types: " + this);
			}
		} else if (from != null && Types.isPrimitive(from)) {
			/*
			 * Otherwise, if S is a primitive type, let S' be the result of applying boxing
			 * conversion (§5.1.7) to S. Then the constraint reduces to ‹S' → T›.
			 */
			new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, Types.wrapPrimitive(from), to)
					.reduceInPlace(bounds);
		} else if (to != null && Types.isPrimitive(to)) {
			/*
			 * Otherwise, if T is a primitive type, let T' be the result of applying boxing
			 * conversion (§5.1.7) to T. Then the constraint reduces to ‹S = T'›.
			 */
			new ConstraintFormula(EQUALITY, from, wrapPrimitive(to)).reduceInPlace(bounds);
		} else if (isUncheckedCompatibleOnly(from, to)) {
			/*
			 * Otherwise, if T is a parameterized type of the form G<T1, ..., Tn>, and there
			 * exists no type of the form G<...> that is a supertype of S, but the raw type
			 * G is a supertype of S, then the constraint reduces to true.
			 * 
			 * Otherwise, if T is an array type of the form G<T1, ..., Tn>[]k, and there
			 * exists no type of the form G<...>[]k that is a supertype of S, but the raw
			 * type G[]k is a supertype of S, then the constraint reduces to true. (The
			 * notation []k indicates an array type of k dimensions.)
			 */
			return;
		} else {
			/*
			 * Otherwise, the constraint reduces to ‹S <: T›.
			 */
			new ConstraintFormula(Kind.SUBTYPE, from, to).reduceInPlace(bounds);
		}
	}

	private static boolean isUncheckedCompatibleOnly(Type from, Type to) {
		Class<?> toRaw = Types.getErasedType(to);
		Class<?> fromRaw = Types.getErasedType(from);

		if (to instanceof ParameterizedType) {
			return toRaw.isAssignableFrom(fromRaw)
					&& new TypeHierarchy(from).resolveSupertype(toRaw) instanceof Class;

		} else if (to instanceof GenericArrayType) {
			return fromRaw.isArray()
					&& isUncheckedCompatibleOnly(Types.getComponentType(from), Types.getComponentType(to));

		} else {
			return false;
		}
	}

	/*
	 * A constraint formula of the form ‹S <: T› is reduced as follows:
	 */
	private void reduceSubtypeConstraint(BoundSet bounds) {
		if (InferenceVariable.isProperType(from) && InferenceVariable.isProperType(to)) {
			/*
			 * If S and T are proper types, the constraint reduces to true if S is a subtype
			 * of T (§4.10), and false otherwise.
			 */
			if (!Types.isSubtype(from, to)) {
				bounds.incorporateFalsehood("Incompatible proper types: " + this);
			} else
				return;
		} else if (from == null)
			/*
			 * Otherwise, if S is the null type, the constraint reduces to true.
			 */
			return;
		else if (to == null)
			/*
			 * Otherwise, if T is the null type, the constraint reduces to false.
			 */
			bounds.incorporateFalsehood("Cannot subtype null type: " + this);
		else if (from instanceof InferenceVariable)
			/*
			 * Otherwise, if S is an inference variable, α, the constraint reduces to the
			 * bound α <: T.
			 */
			bounds.incorporateSubtype(from, to);
		else if (to instanceof InferenceVariable)
			/*
			 * Otherwise, if T is an inference variable, α, the constraint reduces to the
			 * bound S <: α.
			 */
			bounds.incorporateSubtype(from, to);
		else {
			/*
			 * Otherwise, the constraint is reduced according to the form of T:
			 */
			if (to instanceof ParameterizedType) {
				/*
				 * If T is a parameterized class or interface type, or an inner class type of a
				 * parameterized class or interface type (directly or indirectly), let A1, ...,
				 * An be the type arguments of T. Among the supertypes of S, a corresponding
				 * class or interface type is identified, with type arguments B1, ..., Bn.
				 */
				Class<?> rawType = Types.getErasedType(to);
				if (!Types.isAssignable(from, rawType)) {
					/*
					 * If no such type exists, the constraint reduces to false.
					 */
					if (!(from instanceof InferenceVariable))
						bounds.incorporateFalsehood(
								"Type '"
										+ from
										+ "' cannot be assigned from '"
										+ Types.getErasedType(to)
										+ "': "
										+ this);
				} else {
					List<Map.Entry<TypeVariable<?>, Type>> toArguments = ParameterizedTypes
							.getAllTypeArguments((ParameterizedType) to)
							.collect(Collectors.toList());

					List<Type> fromSet;
					if (this.from instanceof WildcardType)
						fromSet = Arrays.asList(((WildcardType) from).getUpperBounds());
					else if (this.from instanceof IntersectionType)
						fromSet = Arrays.asList(((IntersectionType) from).getTypes());
					else
						fromSet = Arrays.asList(from);

					for (Type from : fromSet) {
						if (rawType.isAssignableFrom(Types.getErasedType(from))
								&& from instanceof ParameterizedType) {

							ParameterizedType fromParameterization = (ParameterizedType) new TypeHierarchy(from)
									.resolveSupertype(rawType);
							if (!(fromParameterization instanceof ParameterizedType))
								/*
								 * If no such type exists, the constraint reduces to false.
								 */
								bounds.incorporateFalsehood(
										"Cannot find parameterized supertype for which to verify containment: " + this);

							/*
							 * Otherwise, the constraint reduces to the following new constraints: for all i
							 * (1 ≤ i ≤ n), ‹Bi <= Ai›.
							 */
							Iterator<Map.Entry<TypeVariable<?>, Type>> toArgumentsIterator = toArguments
									.iterator();
							ParameterizedTypes
									.getAllTypeArguments(fromParameterization)
									.map(
											e -> new ConstraintFormula(
													Kind.CONTAINMENT,
													e.getValue(),
													toArgumentsIterator.next().getValue()))
									.forEach(c -> c.reduceInPlace(bounds));
						}
					}
				}
			} else if (to instanceof Class) {
				/*
				 * If T is any other class or interface type, then the constraint reduces to
				 * true if T is among the supertypes of S, and false otherwise.
				 */
				Type from = this.from;
				if (from instanceof InferenceVariable)
					from = intersectionOf(
							bounds.getBoundsOn((InferenceVariable) from).getUpperBounds().collect(toList()));
				if (!Types.isAssignable(from, to))
					bounds.incorporateFalsehood("Class types do not form subtype relation: " + this);
			} else if (!(to instanceof IntersectionType) && Types.getErasedType(to).isArray()) {
				/*
				 * If T is an array type, T'[], then among the supertypes of S that are array
				 * types, a most specific type is identified, S'[] (this may be S itself).
				 */
				Type fromComponent;
				if ((fromComponent = findMostSpecificArrayComponentType(from)) == null) {
					/*
					 * If no such array type exists, the constraint reduces to false.
					 */
					bounds.incorporateFalsehood("Cannot find compatible array type supertype: " + this);
				} else {
					/*
					 * Otherwise:
					 */
					Type toComponent = Types.getComponentType(to);
					if (!Types.isPrimitive(fromComponent) && !Types.isPrimitive(toComponent)) {
						/*
						 * - If neither S' nor T' is a primitive type, the constraint reduces to ‹S' <:
						 * T'›.
						 */
						new ConstraintFormula(Kind.SUBTYPE, fromComponent, toComponent).reduceInPlace(bounds);
					} else {
						/*
						 * - Otherwise, the constraint reduces to true if S' and T' are the same
						 * primitive type, and false otherwise.
						 */
						if ((!Types.isPrimitive(fromComponent) || !Types.equals(fromComponent, toComponent)))
							bounds.incorporateFalsehood("Primitive array component type is not equal: " + this);
					}
				}
			} else if (to instanceof TypeVariableCapture) {
				/*
				 * If T is a type variable, there are three cases:
				 */
				if (from instanceof IntersectionType
						&& Arrays.stream(((IntersectionType) from).getTypes()).anyMatch(f -> f.equals(to))) {
					/*
					 * - If S is an intersection type of which T is an element, the constraint
					 * reduces to true.
					 */
				} else if (((TypeVariableCapture) to).getLowerBounds().length > 0) {
					/*
					 * - Otherwise, if T has a lower bound, B, the constraint reduces to ‹S <: B›.
					 */
					new ConstraintFormula(
							Kind.SUBTYPE,
							from,
							intersectionOf(((TypeVariableCapture) to).getLowerBounds())).reduceInPlace(bounds);
				} else {
					/*
					 * - Otherwise, the constraint reduces to false.
					 */
					bounds.incorporateFalsehood("Type variable cannot be supertype of type: " + this);
				}
			} else if (to instanceof IntersectionType) {
				/*
				 * If T is an intersection type, I1 & ... & In, the constraint reduces to the
				 * following new constraints: for all i (1 ≤ i ≤ n), ‹S <: Ii›.
				 */
				for (Type typeComponent : ((IntersectionType) to).getTypes())
					new ConstraintFormula(Kind.SUBTYPE, from, typeComponent).reduceInPlace(bounds);
			} else {
				throw new ReflectionException(REFLECTION_PROPERTIES.unsupportedType(to));
			}
		}
	}

	private Type findMostSpecificArrayComponentType(Type from) {
		if (Types.getErasedType(from).isArray()) {
			return Types.getComponentType(from);
		}

		if (from instanceof WildcardType) {
			from = intersectionOf(((WildcardType) from).getUpperBounds());
		}

		if (from instanceof IntersectionType) {
			List<Type> candidates = Arrays.asList(((IntersectionType) from).getTypes());

			// attempt to find most specific from candidates
			return candidates
					.stream()
					.filter(t -> Types.getErasedType(t).isArray())
					.map(Types::getComponentType)
					.reduce(
							(a, b) -> (a == null || b == null)
									? null
									: Types.isAssignable(a, b) ? a : Types.isAssignable(b, a) ? b : null)
					.orElse(null);
		}

		return null;
	}

	/*
	 * A constraint formula of the form ‹S <= T›, where S and T are type arguments
	 * (§4.5.1), is reduced as follows:
	 */
	private void reduceContainmentConstraint(BoundSet bounds) {
		if (!(to instanceof WildcardType)) {
			/*
			 * If T is a type:
			 */
			if (!(from instanceof WildcardType)) {
				/*
				 * If S is a type, the constraint reduces to ‹S = T›.
				 */
				new ConstraintFormula(Kind.EQUALITY, from, to).reduceInPlace(bounds);
			} else {
				/*
				 * If S is a wildcard, the constraint reduces to false.
				 */
				bounds.incorporateFalsehood("Wildcard cannot be contained by type: " + this);
			}
		} else {
			WildcardType toWildcard = (WildcardType) to;

			if (toWildcard.getLowerBounds().length == 0) {
				if (toWildcard.getUpperBounds().length == 0
						|| (toWildcard.getUpperBounds().length == 1
								&& toWildcard.getUpperBounds()[0].equals(Object.class))) {
					/*
					 * If T is a wildcard of the form ?, the constraint reduces to true.
					 */
					return;
				} else {
					/*
					 * If T is a wildcard of the form ? extends T':
					 */
					Type intersectionT = intersectionOf(toWildcard.getUpperBounds());

					if (!(from instanceof WildcardType)) {
						/*
						 * If S is a type, the constraint reduces to ‹S <: T'›.
						 */
						new ConstraintFormula(Kind.SUBTYPE, from, intersectionT).reduceInPlace(bounds);
					} else {
						WildcardType from = (WildcardType) this.from;

						if (from.getLowerBounds().length == 0) {
							if (from.getUpperBounds().length == 0) {
								/*
								 * If S is a wildcard of the form ?, the constraint reduces to ‹Object <: T'›.
								 */
								new ConstraintFormula(Kind.SUBTYPE, Object.class, intersectionT)
										.reduceInPlace(bounds);
							} else {
								/*
								 * If S is a wildcard of the form ? extends S', the constraint reduces to ‹S' <:
								 * T'›.
								 */
								new ConstraintFormula(
										Kind.SUBTYPE,
										intersectionOf(from.getUpperBounds()),
										intersectionT).reduceInPlace(bounds);
							}
						} else {
							/*
							 * If S is a wildcard of the form ? super S', the constraint reduces to ‹Object
							 * = T'›.
							 */
							new ConstraintFormula(Kind.EQUALITY, Object.class, intersectionT)
									.reduceInPlace(bounds);
						}
					}
				}
			} else {
				/*
				 * If T is a wildcard of the form ? super T':
				 */
				Type intersectionT = intersectionOf(toWildcard.getLowerBounds());

				if (!(from instanceof WildcardType)) {
					/*
					 * If S is a type, the constraint reduces to ‹T' <: S›.
					 */
					new ConstraintFormula(Kind.SUBTYPE, intersectionT, from).reduceInPlace(bounds);
				} else {
					WildcardType from = (WildcardType) this.from;

					if (from.getLowerBounds().length > 0) {
						/*
						 * If S is a wildcard of the form ? super S', the constraint reduces to ‹T' <:
						 * S'›.
						 */
						new ConstraintFormula(
								Kind.SUBTYPE,
								intersectionT,
								intersectionOf(from.getLowerBounds())).reduceInPlace(bounds);
					} else {
						/*
						 * Otherwise, the constraint reduces to false.
						 */
						bounds.incorporateFalsehood("Wildcard cannot be contained by wildcard: " + this);
					}
				}
			}
		}
	}

	private void reduceEqualityConstraint(BoundSet bounds) {
		if (from instanceof WildcardType && to instanceof WildcardType) {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are type arguments
			 * (§4.5.1), is reduced as follows:
			 */
			WildcardType from = (WildcardType) this.from;
			WildcardType to = (WildcardType) this.to;

			if (from.getLowerBounds().length == 0) {
				if (from.getUpperBounds().length == 0
						|| (from.getUpperBounds().length == 1
								&& from.getUpperBounds()[0].equals(Object.class))) {
					if (to.getLowerBounds().length == 0) {
						if (to.getUpperBounds().length == 0
								|| (to.getUpperBounds().length == 1
										&& to.getUpperBounds()[0].equals(Object.class))) {
							/*
							 * If S has the form ? and T has the form ?, the constraint reduces to true.
							 */
							return;
						} else {
							/*
							 * If S has the form ? and T has the form ? extends T', the constraint reduces
							 * to ‹Object = T'›.
							 */
							new ConstraintFormula(
									Kind.EQUALITY,
									Object.class,
									intersectionOf(to.getUpperBounds())).reduceInPlace(bounds);
						}
					}
				} else if (to.getLowerBounds().length == 0) {
					if (to.getUpperBounds().length == 0) {
						/*
						 * If S has the form ? extends S' and T has the form ?, the constraint reduces
						 * to ‹S' = Object›.
						 */
						new ConstraintFormula(
								Kind.EQUALITY,
								intersectionOf(from.getUpperBounds()),
								Object.class).reduceInPlace(bounds);
					} else {
						/*
						 * If S has the form ? extends S' and T has the form ? extends T', the
						 * constraint reduces to ‹S' = T'›.
						 */
						new ConstraintFormula(
								Kind.EQUALITY,
								intersectionOf(from.getUpperBounds()),
								intersectionOf(to.getUpperBounds())).reduceInPlace(bounds);
					}
				}
			} else if (to.getLowerBounds().length > 0) {
				/*
				 * If S has the form ? super S' and T has the form ? super T', the constraint
				 * reduces to ‹S' = T'›.
				 */
				new ConstraintFormula(
						Kind.EQUALITY,
						intersectionOf(from.getLowerBounds()),
						intersectionOf(to.getLowerBounds())).reduceInPlace(bounds);
			} else {
				/*
				 * Otherwise, the constraint reduces to false.
				 */
				bounds.incorporateFalsehood("Wildcards cannot be equal in this form: " + this);
			}
		} else {
			/*
			 * A constraint formula of the form ‹S = T›, where S and T are types, is reduced
			 * as follows:
			 */
			if (!from.equals(to)) {
				/*
				 * If S and T are proper types, the constraint reduces to true if S is the same
				 * as T (§4.3.4), and false otherwise.
				 */
				if (InferenceVariable.isProperType(from) && InferenceVariable.isProperType(to)) {
					bounds.incorporateFalsehood("Proper types are not equal: " + this);
				} else if (from instanceof InferenceVariable) {
					/*
					 * Otherwise, if S is an inference variable, α, the constraint reduces to the
					 * bound α = T.
					 */
					bounds.incorporateEquality(from, to);
				} else if (to instanceof InferenceVariable) {
					/*
					 * Otherwise, if T is an inference variable, α, the constraint reduces to the
					 * bound S = α.
					 */
					bounds.incorporateEquality(from, to);
				} else if ((from instanceof Class<?> && ((Class<?>) from).isArray())
						&& (to instanceof Class<?> && ((Class<?>) to).isArray())) {
					/*
					 * Otherwise, if S and T are array types, S'[] and T'[], the constraint reduces
					 * to ‹S' = T'›.
					 */
					new ConstraintFormula(
							Kind.EQUALITY,
							Types.getComponentType(from),
							Types.getComponentType(to)).reduceInPlace(bounds);
				} else if (Types.getErasedType(from).equals(Types.getErasedType(to))) {
					/*
					 * Otherwise, if S and T are class or interface types with the same erasure,
					 * where S has type arguments B1, ..., Bn and T has type arguments A1, ..., An,
					 * the constraint reduces to the following new constraints: for all i (1 ≤ i ≤
					 * n), ‹Bi = Ai›.
					 */
					Type finalFrom = from;
					Type finalTo = to;
					if (from instanceof ParameterizedType) {
						if (to instanceof ParameterizedType) {
							Iterator<Type> fromArguments = getAllTypeArguments((ParameterizedType) finalFrom)
									.map(Entry::getValue)
									.iterator();

							getAllTypeArguments((ParameterizedType) finalTo)
									.map(Entry::getValue)
									.map(
											toArgument -> new ConstraintFormula(
													Kind.EQUALITY,
													fromArguments.next(),
													toArgument))
									.forEach(c -> c.reduceInPlace(bounds));
						} else {
							bounds.incorporateFalsehood("Wildcards cannot be equal in this form: " + this);
						}
					} else if (to instanceof ParameterizedType) {
						bounds.incorporateFalsehood("Types are not erasure-equal: " + this);
					}
				} else {
					/*
					 * Otherwise, the constraint reduces to false.
					 */
					bounds.incorporateFalsehood("Types of these types cannot be equal: " + this);
				}
			}
		}
	}
}
