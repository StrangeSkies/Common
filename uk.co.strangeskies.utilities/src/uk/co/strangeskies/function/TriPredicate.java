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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.function;

/**
 *
 * @author Elias N Vasylenko
 * @param <O1>
 *          the type of the first argument to the predicate
 * @param <O2>
 *          the type of the second argument the predicate
 * @param <O3>
 *          the type of the third argument the predicate
 */
@FunctionalInterface
public interface TriPredicate<O1, O2, O3> {
	/**
	 * Evaluates this predicate on the given arguments.
	 * 
	 * @param firstOperand
	 *          the first input argument
	 * @param secondOperand
	 *          the second input argument
	 * @param thirdOperand
	 *          the third input argument
	 * @return true if the input arguments match the predicate, otherwise false
	 */
	public boolean test(O1 firstOperand, O2 secondOperand, O3 thirdOperand);

	/**
	 * Returns a composed predicate that represents a short-circuiting logical AND
	 * of this predicate and another. When evaluating the composed predicate, if
	 * this predicate is false, then the other predicate is not evaluated.
	 * <p>
	 * 
	 * Any exceptions thrown during evaluation of either predicate are relayed to
	 * the caller; if evaluation of this predicate throws an exception, the other
	 * predicate will not be evaluated.
	 * 
	 * @param other
	 *          a predicate that will be logically-ANDed with this predicate
	 * @return a composed predicate that represents the short-circuiting logical
	 *         AND of this predicate and the other predicate
	 */
	public default TriPredicate<O1, O2, O3> and(
			TriPredicate<? super O1, ? super O2, ? super O3> other) {
		return (o1, o2, o3) -> test(o1, o2, o3) && other.test(o1, o2, o3);
	}

	/**
	 * Returns a predicate that represents the logical negation of this predicate.
	 * 
	 * @return a predicate that represents the logical negation of this predicate
	 */
	public default TriPredicate<O1, O2, O3> negate() {
		return (o1, o2, o3) -> !test(o1, o2, o3);
	}

	/**
	 * Returns a composed predicate that represents a short-circuiting logical OR
	 * of this predicate and another. When evaluating the composed predicate, if
	 * this predicate is true, then the other predicate is not evaluated.
	 * <p>
	 * 
	 * Any exceptions thrown during evaluation of either predicate are relayed to
	 * the caller; if evaluation of this predicate throws an exception, the other
	 * predicate will not be evaluated.
	 * 
	 * @param other
	 *          a predicate that will be logically-ORed with this predicate
	 * @return a composed predicate that represents the short-circuiting logical
	 *         OR of this predicate and the other predicate
	 */
	public default TriPredicate<O1, O2, O3> or(
			TriPredicate<? super O1, ? super O2, ? super O3> other) {
		return (o1, o2, o3) -> test(o1, o2, o3) || other.test(o1, o2, o3);
	}
}
