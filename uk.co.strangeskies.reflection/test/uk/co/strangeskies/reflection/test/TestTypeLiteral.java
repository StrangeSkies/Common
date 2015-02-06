/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
package uk.co.strangeskies.reflection.test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.ParameterizedTypeLiteral;
import uk.co.strangeskies.reflection.TypeParameter;

public class TestTypeLiteral {
	public static class A<T> {
		public class B {}
	}

	public static class B {
		public <T extends Number> void method(T a, T b) {}

		public <T> void method(@SuppressWarnings("unchecked") Collection<T>... a) {}

		public <T> void bethod(Collection<T> a) {}

		public <T extends Number, U extends List<? super T>> Map<T, U> method2(
				List<T> a, U b) {
			return null;
		}

		public <T extends Number, U extends List<? super T>> U method(
				Collection<? extends T> a, U b) {
			return null;
		}

		public <T extends Number, U extends List<? super T>> U method4(
				Collection<? extends T> a, U b) {
			return null;
		}

		public <T, R> void accept(Set<Invokable<T, R>> set) {}

		public strictfp <T extends Comparable<? super T>, U extends Collection<? extends Comparable<? super T>>> void bothways(
				T t, U u) {}

		public <U, R> Invokable<U, ? extends R> okay(
				Set<? extends Invokable<U, ? extends R>> candidates,
				List<? extends Type> parameters) {
			return null;
		}
	}

	public static <T> void main(String... args) throws NoSuchMethodException,
			SecurityException {
		System.out.println(new TypeParameter<T>() {});
		System.out.println(new ParameterizedTypeLiteral<List<String>>() {});
		System.out.println();

		System.out.println(ParameterizedTypes.from(HashSet.class,
				Arrays.asList(new BoundSet().createInferenceVariable()))
				.resolveSupertypeParameters(Collection.class));
		System.out.println();

		System.out
				.println(ParameterizedTypeLiteral.from(B.class).resolveMethodOverload("bothways",
						String.class, new ParameterizedTypeLiteral<List<String>>() {}.getType()));
		System.out.println();

		System.out.println(ParameterizedTypeLiteral.from(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(new ParameterizedTypeLiteral<List<? extends Number>>() {}.getType())
				.infer());
		System.out.println();

		System.out.println(ParameterizedTypeLiteral
				.from(Arrays.class)
				.resolveMethodOverload("asList", int.class, double.class)
				.withTargetType(
						new ParameterizedTypeLiteral<List<? super Comparable<? extends Number>>>() {}
								.getType()).infer());
		System.out.println();

		System.out.println(new ParameterizedTypeLiteral<B>() {}.resolveMethodOverload("okay",
				new ParameterizedTypeLiteral<Set<Invokable<T, ?>>>() {}.getType(),
				new ParameterizedTypeLiteral<List<? extends Type>>() {}.getType()));
		System.out.println();

		System.out.println(ParameterizedTypeLiteral.from(B.class).resolveMethodOverload(
				"method", new ParameterizedTypeLiteral<List<Integer>>() {}.getType(),
				new ParameterizedTypeLiteral<List<Number>>() {}.getType()));
		System.out.println();

		System.out.println(ParameterizedTypeLiteral.from(B.class).resolveMethodOverload(
				"method2", new ParameterizedTypeLiteral<List<Integer>>() {}.getType(),
				new ParameterizedTypeLiteral<List<Comparable<Integer>>>() {}.getType()));
		System.out.println();

		System.out.println(ParameterizedTypeLiteral
				.from(B.class)
				.resolveMethodOverload("method",
						new ParameterizedTypeLiteral<Collection<? super Integer>>() {}.getType())
				.infer());
		System.out.println();

		System.out.println(ParameterizedTypeLiteral
				.from(B.class)
				.resolveMethodOverload("method4",
						new ParameterizedTypeLiteral<Collection<? extends Integer>>() {}.getType(),
						new ParameterizedTypeLiteral<List<? super Number>>() {}.getType()).infer());
		System.out.println();

		/*-
		System.out.println(TypeLiteral.from(B.class).resolveMethodOverload(
				"method", new TypeLiteral<Collection<? super Integer>>() {}.getType(),
				new TypeLiteral<Collection<? super Integer>>() {}.getType()));
		System.out.println();
		 */
	}
}
