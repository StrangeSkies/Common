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

import java.util.function.Function;

/**
 * A two tuple.
 * 
 * @author Elias N Vasylenko
 *
 * @param <L>
 *          The type of the first, left, item.
 * @param <R>
 *          The type of the second, right, item.
 */
public class Pair<L, R> extends Tuple<L, Unit<R>> {
	/**
	 * Initialise a pair with the given two values.
	 * 
	 * @param left
	 *          The first, left, item.
	 * @param right
	 *          The second, right, item.
	 */
	public Pair(L left, R right) {
		super(left, new Unit<>(right));
	}

	/**
	 * @return The head value.
	 */
	public L get0() {
		return getHead();
	}

	/**
	 * @return The head value.
	 */
	public L getLeft() {
		return getHead();
	}

	/**
	 * @return The tail value.
	 */
	public R get1() {
		return getTail().getHead();
	}

	/**
	 * @return The tail value.
	 */
	public R getRight() {
		return getTail().getHead();
	}

	@Override
	public <I> Pair<I, R> mapHead(Function<? super L, ? extends I> headMap) {
		return new Pair<>(headMap.apply(getLeft()), getRight());
	}
}
