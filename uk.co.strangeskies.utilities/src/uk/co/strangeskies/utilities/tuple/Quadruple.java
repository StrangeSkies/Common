/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.tuple;


/**
 * A four tuple.
 * 
 * @author Elias N Vasylenko
 *
 * @param <A>
 *          The type of the first item.
 * @param <B>
 *          The type of the second item.
 * @param <C>
 *          The type of the third item.
 * @param <D>
 *          The type of the fourth, and last, item.
 */
public class Quadruple<A, B, C, D> extends Tuple<A, Triple<B, C, D>> {
	/**
	 * Initialise a quadruple with the given five values.
	 * 
	 * @param a
	 *          The first item.
	 * @param b
	 *          The second item.
	 * @param c
	 *          The third item.
	 * @param d
	 *          The fourth, and last, item.
	 */
	public Quadruple(A a, B b, C c, D d) {
		super(a, new Triple<>(b, c, d));
	}

	/**
	 * @return The head value.
	 */
	public A get0() {
		return getHead();
	}

	/**
	 * @return The second value.
	 */
	public B get1() {
		return getTail().getHead();
	}

	/**
	 * @return The third value.
	 */
	public C get2() {
		return getTail().getTail().getHead();
	}

	/**
	 * @return The fourth value.
	 */
	public D get3() {
		return getTail().getTail().getTail().getHead();
	}
}
