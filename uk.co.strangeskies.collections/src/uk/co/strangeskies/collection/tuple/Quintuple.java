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
 * This file is part of uk.co.strangeskies.collections.
 *
 * uk.co.strangeskies.collections is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.tuple;


/**
 * A five tuple.
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
 *          The type of the fourth item.
 * @param <E>
 *          The type of the fifth, and last, item.
 */
public class Quintuple<A, B, C, D, E> extends Tuple<A, Quadruple<B, C, D, E>> {
	/**
	 * Initialise a quintuple with the given five values.
	 * 
	 * @param a
	 *          The first item.
	 * @param b
	 *          The second item.
	 * @param c
	 *          The third item.
	 * @param d
	 *          The fourth item.
	 * @param e
	 *          The fifth, and last, item.
	 */
	public Quintuple(A a, B b, C c, D d, E e) {
		super(a, new Quadruple<>(b, c, d, e));
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

	/**
	 * @return The fifth value.
	 */
	public E get4() {
		return getTail().getTail().getTail().getTail().getHead();
	}
}
