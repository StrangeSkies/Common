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

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.TypeVariableCapture.captureWildcard;
import static uk.co.strangeskies.reflection.TypeVariables.typeVariableExtending;
import static uk.co.strangeskies.reflection.TypeVariables.unboundedTypeVariable;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcard;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.matchers.IsAssignableTo.isAssignableTo;
import static uk.co.strangeskies.reflection.matchers.IsStrictlyAssignableTo.isStrictlyAssignableTo;
import static uk.co.strangeskies.reflection.matchers.IsSubtypeOf.isSubtypeOf;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.IntersectionType;
import uk.co.strangeskies.reflection.TypeVariableCapture;

@SuppressWarnings({ "rawtypes", "javadoc" })
public class AssignmentTest {
	interface StringComparable extends Comparable<String> {}

	interface RawComparable extends Comparable {}

	interface NumberComparable<T extends Number> extends Comparable<T>, Serializable {}

	static class EnclosingGenericType<T> {
		abstract class EnclosedGenericType<U> implements Comparable<T> {}
	}

	@Test
	public void classToClassAssignment() {
		assertThat(Object.class, isAssignableTo(Object.class));
		assertThat(String.class, isAssignableTo(String.class));
		assertThat(Object.class, not(isAssignableTo(String.class)));
		assertThat(String.class, isAssignableTo(Object.class));

		assertThat(Integer.class, isAssignableTo(Number.class));
		assertThat(Double.class, isAssignableTo(Number.class));
		assertThat(Number.class, not(isAssignableTo(Integer.class)));
		assertThat(Integer.class, not(isAssignableTo(Double.class)));
	}

	@Test
	public void classToUnknownAssignment() {
		assertThat(Object.class, not(isAssignableTo(new Type() {})));
		assertThat(String.class, not(isAssignableTo(new Type() {})));
	}

	@Test
	public void unknownToClassAssignment() {
		assertThat(new Type() {}, isAssignableTo(Object.class));
		assertThat(new Type() {}, not(isAssignableTo(String.class)));
	}

	@Test
	public void classToNullAssignment() {
		assertThat(Object.class, isAssignableTo(null));
		assertThat(String.class, isAssignableTo(null));
	}

	@Test
	public void nullToClassAssignment() {
		assertThat(null, isAssignableTo(Object.class));
		assertThat(null, isAssignableTo(String.class));
	}

	@Test
	public void nullToNullAssignment() {
		assertThat(null, isAssignableTo(null));
	}

	@Test
	public void classWithParameterizedSuperclassToParameterizedAssignment() {
		assertThat(StringComparable.class, isAssignableTo(parameterize(Comparable.class, String.class)));
		assertThat(StringComparable.class, not(isAssignableTo(parameterize(Comparable.class, Number.class))));
	}

	@Test
	public void classWithParameterizedSuperclassToRawAssignment() {
		assertThat(StringComparable.class, isAssignableTo(Comparable.class));
	}

	@Test
	public void classWithRawSupertypeToParameterizedAssignment() {
		assertThat(RawComparable.class, not(isAssignableTo(parameterize(Comparable.class, Number.class))));
	}

	@Test
	public void classWithRawSupertypeToRawAssignment() {
		assertThat(RawComparable.class, isAssignableTo(Comparable.class));
	}

	@Test
	public void primitiveToBoxedAssignment() {
		assertThat(int.class, isAssignableTo(Integer.class));
	}

	@Test
	public void classToPrimitiveStrictAssignment() {
		assertThat(int.class, not(isStrictlyAssignableTo(Integer.class)));
	}

	@Test
	public void primitiveToClassStrictAssignment() {
		assertThat(Integer.class, not(isStrictlyAssignableTo(int.class)));
	}

	@Test
	public void wideningPrimitiveAssignment() {
		assertThat(int.class, isAssignableTo(float.class));
		assertThat(int.class, isAssignableTo(double.class));
		assertThat(int.class, not(isAssignableTo(short.class)));
		assertThat(int.class, not(isAssignableTo(byte.class)));
		assertThat(int.class, not(isAssignableTo(char.class)));

		assertThat(int.class, isAssignableTo(long.class));
		assertThat(double.class, not(isAssignableTo(long.class)));
		assertThat(float.class, not(isAssignableTo(long.class)));

		assertThat(int.class, isAssignableTo(int.class));
		assertThat(float.class, not(isAssignableTo(int.class)));
		assertThat(char.class, isAssignableTo(int.class));
		assertThat(short.class, isAssignableTo(int.class));
		assertThat(byte.class, isAssignableTo(int.class));

		assertThat(long.class, isAssignableTo(float.class));
		assertThat(double.class, not(isAssignableTo(float.class)));
	}

	@Test
	public void narrowingPrimitiveAssignment() {
		assertThat(long.class, not(isAssignableTo(int.class)));
	}

	@Test
	public void boxedToPrimitiveAssignment() {
		assertThat(Integer.class, isAssignableTo(int.class));
	}

	@Test
	public void primitiveArrayToBoxedArrayAssignment() {
		assertThat(arrayFromComponent(int.class), not(isAssignableTo(arrayFromComponent(Integer.class))));
	}

	@Test
	public void boxedArrayToPrimitiveArrayAssignment() {
		assertThat(arrayFromComponent(Integer.class), not(isAssignableTo(arrayFromComponent(int.class))));
	}

	@Test
	public void primitiveToBoxedSubtype() {
		assertThat(int.class, not(isSubtypeOf(Integer.class)));
	}

	@Test
	public void boxedToPrimitiveSubtype() {
		assertThat(Integer.class, not(isSubtypeOf(int.class)));
	}

	@Test
	public void classToIntersectionAssignment() {
		assertThat(Integer.class, isAssignableTo(intersectionOf(Number.class, Comparable.class)));

		assertThat(
				Integer.class,
				isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, wildcard()))));

		assertThat(
				Integer.class,
				not(isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, Number.class)))));
	}

	@Test
	public void intersectionToClassAssignment() {
		assertThat(intersectionOf(Comparable.class, Serializable.class), isAssignableTo(Comparable.class));
	}

	@Test
	public void parameterizedToIntersectionAssignment() {
		assertThat(
				parameterize(NumberComparable.class, Integer.class),
				isAssignableTo(intersectionOf(Serializable.class, parameterize(Comparable.class, wildcard()))));
	}

	@Test
	public void intersectionToParameterizedAssignment() {
		assertThat(
				intersectionOf(Comparable.class, Serializable.class),
				isAssignableTo(parameterize(Comparable.class, wildcard())));
	}

	@Test
	public void classToIntersectionSubtype() {
		assertThat(Integer.class, isSubtypeOf(intersectionOf(Number.class, Comparable.class)));
	}

	@Test
	public void intersectionToClassSubtype() {
		assertThat(intersectionOf(Comparable.class, Serializable.class), isSubtypeOf(Comparable.class));
	}

	@Test
	public void intersectionToParameterizedSubtype() {
		assertThat(
				intersectionOf(Comparable.class, Serializable.class),
				not(isSubtypeOf(parameterize(Comparable.class, wildcard()))));
	}

	@Test
	public void emptyIntersectionToClassSubtype() {
		assertThat(intersectionOf(), isSubtypeOf(Object.class));
		assertThat(intersectionOf(), not(isSubtypeOf(String.class)));

		Type emptyIntersection = new IntersectionType() {
			@Override
			public Type[] getTypes() {
				return new Type[0];
			}
		};

		assertThat(emptyIntersection, isSubtypeOf(Object.class));
		assertThat(emptyIntersection, not(isSubtypeOf(String.class)));
	}

	@Test
	public void classToUnboundedWildcardAssignment() {
		assertThat(Object.class, not(isAssignableTo(wildcard())));
		assertThat(Integer.class, not(isAssignableTo(wildcard())));
	}

	@Test
	public void classToLowerBoundedWildcardAssignment() {
		assertThat(Integer.class, isAssignableTo(wildcardSuper(Number.class)));
		assertThat(Integer.class, isAssignableTo(wildcardSuper(Comparable.class)));
		assertThat(Integer.class, isAssignableTo(wildcardSuper(parameterize(Comparable.class, Integer.class))));
	}

	@Test
	public void classToUpperBoundedWildcardAssignment() {
		assertThat(Integer.class, not(isAssignableTo(wildcardExtending(Integer.class))));
	}

	@Test
	public void unboundedWildcardToClassAssignment() {
		assertThat(wildcard(), isAssignableTo(Object.class));
		assertThat(wildcard(), not(isAssignableTo(Integer.class)));
	}

	@Test
	public void lowerBoundedWildcardToClassAssignment() {
		assertThat(wildcardSuper(Number.class), isAssignableTo(Object.class));
		assertThat(wildcardSuper(Integer.class), not(isAssignableTo(Number.class)));
	}

	@Test
	public void upperBoundedWildcardToClassAssignment() {
		assertThat(wildcardExtending(Number.class), isAssignableTo(Object.class));
		assertThat(wildcardExtending(Integer.class), isAssignableTo(Number.class));
		assertThat(wildcardExtending(Integer.class), isAssignableTo(Comparable.class));
	}

	@Test
	public void upperBoundedWildcardToParameterizedAssignment() {
		assertThat(wildcardExtending(Integer.class), isAssignableTo(parameterize(Comparable.class, Integer.class)));
	}

	@Test
	public void classToTypeVariableCaptureAssignment() {
		assertThat(Integer.class, not(isAssignableTo(captureWildcard(wildcard()))));

		assertThat(Integer.class, isAssignableTo(captureWildcard(wildcardSuper(Comparable.class))));

		assertThat(Integer.class, not(isAssignableTo(captureWildcard(wildcardExtending(Integer.class)))));
	}

	@Test
	public void typeVariableCaptureToClassAssignment() {
		assertThat(captureWildcard(wildcard()), not(isAssignableTo(Integer.class)));

		assertThat(captureWildcard(wildcardSuper(Number.class)), isAssignableTo(Object.class));
		assertThat(captureWildcard(wildcardSuper(Integer.class)), not(isAssignableTo(Number.class)));

		assertThat(captureWildcard(wildcardExtending(Integer.class)), isAssignableTo(Comparable.class));
	}

	@Test
	public void typeVariableCaptureToTypeVariableCaptureAssignment() {
		TypeVariableCapture type = captureWildcard(wildcard());
		assertThat(type, isAssignableTo(captureWildcard(wildcardSuper(type))));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(type));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(captureWildcard(wildcardSuper(type))));
	}

	@Test
	public void unboundedTypeVariableToClassAssignment() {
		TypeVariable<?> unbounded = unboundedTypeVariable(null, "");

		assertThat(unbounded, isAssignableTo(Object.class));
		assertThat(unbounded, not(isAssignableTo(Integer.class)));
	}

	@Test
	public void boundedTypeVariableToClassAssignment() {
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(Number.class))),
				isAssignableTo(Object.class));
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(Integer.class))),
				isAssignableTo(Number.class));
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(Integer.class))),
				isAssignableTo(Comparable.class));
	}

	@Test
	public void boundedTypeVariableToParameterizedAssignment() {
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(Integer.class))),
				isAssignableTo(parameterize(Comparable.class, Integer.class)));
	}

	@Test
	public void typeVariableToTypeVariableAssignment() {
		TypeVariable<?> unbounded = unboundedTypeVariable(null, "");

		assertThat(unbounded, isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(unbounded))),
				isAssignableTo(unbounded));
		assertThat(
				typeVariableExtending(null, "", AnnotatedTypes.annotated(wildcardExtending(unbounded))),
				isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
	}

	@Test
	public void classToArrayAssignment() {
		assertThat(String.class, not(isAssignableTo(arrayFromComponent(String.class))));
	}

	@Test
	public void arrayToClassAssignment() {
		assertThat(arrayFromComponent(String.class), not(isAssignableTo(String.class)));
	}

	@Test
	public void parameterizedToGenericArrayAssignment() {
		assertThat(
				parameterize(Comparable.class, String.class),
				not(isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class)))));
	}

	@Test
	public void genericArrayToParameterizedAssignment() {
		assertThat(
				arrayFromComponent(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(parameterize(Comparable.class, String.class))));
	}

	@Test
	public void genericArrayToClassAssignment() {
		assertThat(arrayFromComponent(parameterize(Comparable.class, String.class)), not(isAssignableTo(Comparable.class)));

		assertThat(arrayFromComponent(parameterize(Comparable.class, String.class)), isAssignableTo(Object.class));
	}

	@Test
	public void classToGenericArrayAssignment() {
		assertThat(String.class, not(isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class)))));

		assertThat(Object.class, not(isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class)))));
	}

	@Test
	public void classWithParameterizedSupertypeArrayToGenericArrayAssignment() {
		assertThat(
				arrayFromComponent(StringComparable.class),
				isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class))));
	}

	@Test
	public void arrayToGenericArrayAssignment() {
		assertThat(
				arrayFromComponent(Comparable.class),
				isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class))));

		assertThat(
				arrayFromComponent(String.class),
				isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class))));

		assertThat(
				arrayFromComponent(Object.class),
				not(isAssignableTo(arrayFromComponent(parameterize(Comparable.class, String.class)))));
	}

	@Test
	public void genericArrayToArrayAssignment() {
		assertThat(
				arrayFromComponent(parameterize(Comparable.class, String.class)),
				isAssignableTo(arrayFromComponent(Comparable.class)));

		assertThat(
				arrayFromComponent(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(arrayFromComponent(String.class))));

		assertThat(
				arrayFromComponent(parameterize(Comparable.class, String.class)),
				isAssignableTo(arrayFromComponent(Object.class)));
	}

	@Test
	public void genericArrayToGenericArrayAssignment() {
		assertThat(
				arrayFromComponent(parameterize(Comparable.class, Number.class)),
				isAssignableTo(arrayFromComponent(parameterize(Comparable.class, wildcardSuper(Integer.class)))));

		assertThat(
				arrayFromComponent(parameterize(Comparable.class, Number.class)),
				not(isAssignableTo(arrayFromComponent(parameterize(Comparable.class, Integer.class)))));
	}

	@Test
	public void enclosingParameterizedToRawAssignment() {
		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				isAssignableTo(Comparable.class));
	}

	@Test
	public void enclosingParameterizedToParameterizedAssignment() {
		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				not(isAssignableTo(parameterize(Comparable.class, Integer.class))));

		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				isAssignableTo(parameterize(Comparable.class, String.class)));
	}

	@Test
	public void enclosingParameterizedToEnclosingParameterizedAssignment() {
		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				isAssignableTo(Comparable.class));

		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				isAssignableTo(parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class)));

		assertThat(
				parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class),
				isAssignableTo(
						parameterize(
								EnclosingGenericType.EnclosedGenericType.class,
								String.class,
								wildcardExtending(Number.class))));

		assertThat(parameterize(EnclosingGenericType.EnclosedGenericType.class, String.class, Integer.class), not(
				isAssignableTo(
						parameterize(
								EnclosingGenericType.EnclosedGenericType.class,
								Double.class,
								wildcardExtending(Number.class)))));
	}

	@Test
	public void rawToParameterizedContainment() {
		assertThat(
				parameterize(Set.class, Set.class),
				not(isAssignableTo(parameterize(Set.class, parameterize(Set.class, String.class)))));
	}

	@Test
	public void parameterizedToRawContainment() {
		assertThat(
				parameterize(Set.class, parameterize(Set.class, String.class)),
				not(isAssignableTo(parameterize(Set.class, Set.class))));
	}

	@Test
	public void parameterizedToParameterizedContainment() {
		assertThat(
				parameterize(Set.class, parameterize(Set.class, Number.class)),
				isAssignableTo(parameterize(Set.class, parameterize(Set.class, Number.class))));

		assertThat(
				parameterize(Set.class, parameterize(Set.class, Number.class)),
				not(isAssignableTo(parameterize(Set.class, wildcardExtending(parameterize(Set.class, Object.class))))));

		assertThat(
				parameterize(Set.class, parameterize(Set.class, Number.class)),
				isAssignableTo(
						parameterize(Set.class, wildcardExtending(parameterize(Set.class, wildcardSuper(Integer.class))))));
	}

	@Test
	public void classToSingularIntersectionContainment() {
		assertThat(parameterize(Set.class, String.class), isAssignableTo(parameterize(Set.class, new IntersectionType() {
			@Override
			public Type[] getTypes() {
				return new Type[] { String.class };
			}
		})));
	}

	@Test
	public void singularIntersectionToClassContainment() {
		assertThat(parameterize(Set.class, new IntersectionType() {
			@Override
			public Type[] getTypes() {
				return new Type[] { String.class };
			}
		}), isAssignableTo(parameterize(Set.class, String.class)));
	}

	@Test
	public void wildcardToWildcardContainment() {
		assertThat(
				parameterize(Set.class, wildcardExtending(Integer.class)),
				isAssignableTo(parameterize(Set.class, wildcardExtending(Number.class))));
		assertThat(
				parameterize(Set.class, wildcardExtending(Number.class)),
				not(isAssignableTo(parameterize(Set.class, wildcardExtending(Integer.class)))));
		assertThat(
				parameterize(Set.class, wildcardExtending(Number.class)),
				not(isAssignableTo(parameterize(Set.class, wildcardSuper(Integer.class)))));

		assertThat(
				parameterize(Set.class, wildcardSuper(Integer.class)),
				not(isAssignableTo(parameterize(Set.class, wildcardExtending(Number.class)))));
		assertThat(
				parameterize(Set.class, wildcardSuper(Number.class)),
				not(isAssignableTo(parameterize(Set.class, wildcardExtending(Integer.class)))));
		assertThat(
				parameterize(Set.class, wildcardSuper(Number.class)),
				isAssignableTo(parameterize(Set.class, wildcardSuper(Integer.class))));

		assertThat(
				parameterize(Set.class, wildcard()),
				not(isAssignableTo(parameterize(Set.class, wildcardExtending(Number.class)))));
		assertThat(
				parameterize(Set.class, wildcard()),
				not(isAssignableTo(parameterize(Set.class, wildcardSuper(Number.class)))));

		assertThat(
				parameterize(Set.class, wildcardExtending(Number.class)),
				isAssignableTo(parameterize(Set.class, wildcard())));
		assertThat(
				parameterize(Set.class, wildcardSuper(Number.class)),
				isAssignableTo(parameterize(Set.class, wildcard())));
	}
}
