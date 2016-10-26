/*
 * Copyright (C) 2016 ${copyright.holder.name} <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.reflection.test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.ArrayTypes.fromComponentType;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.TypeVariableCapture.captureWildcard;
import static uk.co.strangeskies.reflection.TypeVariables.unboundedTypeVariable;
import static uk.co.strangeskies.reflection.TypeVariables.upperBoundedTypeVariable;
import static uk.co.strangeskies.reflection.WildcardTypes.unboundedWildcard;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.test.matchers.IsAssignableTo.isAssignableTo;
import static uk.co.strangeskies.reflection.test.matchers.IsSubtypeOf.isSubtypeOf;

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
	public void rawToParameterizedContainment() {
		assertThat(parameterize(Set.class, Set.class),
				not(isAssignableTo(parameterize(Set.class, parameterize(Set.class, String.class)))));
	}

	@Test
	public void parameterizedToRawContainment() {
		assertThat(parameterize(Set.class, parameterize(Set.class, String.class)),
				not(isAssignableTo(parameterize(Set.class, Set.class))));
	}

	@Test
	public void primitiveToBoxedAssignment() {
		assertThat(int.class, isAssignableTo(Integer.class));
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
		assertThat(fromComponentType(int.class), not(isAssignableTo(fromComponentType(Integer.class))));
	}

	@Test
	public void boxedArrayToPrimitiveArrayAssignment() {
		assertThat(fromComponentType(Integer.class), not(isAssignableTo(fromComponentType(int.class))));
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

		assertThat(Integer.class,
				isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, unboundedWildcard()))));

		assertThat(Integer.class,
				not(isAssignableTo(intersectionOf(Number.class, parameterize(Comparable.class, Number.class)))));
	}

	@Test
	public void intersectionToClassAssignment() {
		assertThat(intersectionOf(Comparable.class, Serializable.class), isAssignableTo(Comparable.class));
	}

	@Test
	public void parameterizedToIntersectionAssignment() {
		assertThat(parameterize(NumberComparable.class, Integer.class),
				isAssignableTo(intersectionOf(Serializable.class, parameterize(Comparable.class, unboundedWildcard()))));
	}

	@Test
	public void intersectionToParameterizedAssignment() {
		assertThat(intersectionOf(Comparable.class, Serializable.class),
				isAssignableTo(parameterize(Comparable.class, unboundedWildcard())));
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
		assertThat(intersectionOf(Comparable.class, Serializable.class),
				not(isSubtypeOf(parameterize(Comparable.class, unboundedWildcard()))));
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
		assertThat(Object.class, not(isAssignableTo(unboundedWildcard())));
		assertThat(Integer.class, not(isAssignableTo(unboundedWildcard())));
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
		assertThat(unboundedWildcard(), isAssignableTo(Object.class));
		assertThat(unboundedWildcard(), not(isAssignableTo(Integer.class)));
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
		assertThat(Integer.class, not(isAssignableTo(captureWildcard(unboundedWildcard()))));

		assertThat(Integer.class, isAssignableTo(captureWildcard(wildcardSuper(Comparable.class))));

		assertThat(Integer.class, not(isAssignableTo(captureWildcard(wildcardExtending(Integer.class)))));
	}

	@Test
	public void typeVariableCaptureToClassAssignment() {
		assertThat(captureWildcard(unboundedWildcard()), not(isAssignableTo(Integer.class)));

		assertThat(captureWildcard(wildcardSuper(Number.class)), isAssignableTo(Object.class));
		assertThat(captureWildcard(wildcardSuper(Integer.class)), not(isAssignableTo(Number.class)));

		assertThat(captureWildcard(wildcardExtending(Integer.class)), isAssignableTo(Comparable.class));
	}

	@Test
	public void typeVariableCaptureToTypeVariableCaptureAssignment() {
		TypeVariableCapture type = captureWildcard(unboundedWildcard());
		assertThat(type, isAssignableTo(captureWildcard(wildcardSuper(type))));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(type));
		assertThat(captureWildcard(wildcardExtending(type)), isAssignableTo(captureWildcard(wildcardSuper(type))));
	}

	@Test
	public void assignFromTypeVariable() {
		TypeVariable<?> unbounded = unboundedTypeVariable(null, "");

		assertThat(unbounded, isAssignableTo(Object.class));
		assertThat(unbounded, not(isAssignableTo(Integer.class)));

		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Number.class))),
				isAssignableTo(Object.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(Number.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(Comparable.class));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(Integer.class))),
				isAssignableTo(parameterize(Comparable.class, Integer.class)));

		assertThat(unbounded, isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(unbounded))),
				isAssignableTo(unbounded));
		assertThat(upperBoundedTypeVariable(null, "", AnnotatedTypes.over(wildcardExtending(unbounded))),
				isAssignableTo(captureWildcard(wildcardSuper(unbounded))));
	}

	@Test
	public void assignToArrayType() {
		assertThat(String.class, not(isAssignableTo(fromComponentType(String.class))));
	}

	@Test
	public void assignFromArrayType() {
		assertThat(fromComponentType(String.class), not(isAssignableTo(String.class)));
	}

	@Test
	public void rawArrayToGenericArrayAssignment() {
		assertThat(parameterize(Comparable.class, String.class),
				not(isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class)))));

		assertThat(fromComponentType(StringComparable.class),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class))));
	}

	@Test
	public void assignFromGenericArrayType() {
		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(parameterize(Comparable.class, String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)), not(isAssignableTo(Comparable.class)));

		assertThat(fromComponentType(Comparable.class),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				isAssignableTo(fromComponentType(Comparable.class)));

		assertThat(fromComponentType(parameterize(Comparable.class, String.class)),
				not(isAssignableTo(fromComponentType(String.class))));

		assertThat(fromComponentType(parameterize(Comparable.class, Number.class)),
				isAssignableTo(fromComponentType(parameterize(Comparable.class, wildcardSuper(Integer.class)))));

		assertThat(fromComponentType(parameterize(Comparable.class, Number.class)),
				not(isAssignableTo(fromComponentType(parameterize(Comparable.class, Integer.class)))));
	}
}
