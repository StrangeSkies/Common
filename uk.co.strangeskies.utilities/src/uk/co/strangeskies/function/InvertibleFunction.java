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

import java.util.function.Function;

/**
 * Describes a function from F to T. A function should be stateless.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          Operand type.
 * @param <R>
 *          Result type.
 */
public interface InvertibleFunction<T, R> extends Function<T, R> {
	/**
	 * This returns the mathematical inverse of the receiving function.
	 * 
	 * @return A new Invertible function performing the inverse operation.
	 */
	public InvertibleFunction<R, T> getInverse();

	/**
	 * @param <T>
	 *          The operand type of the forward function, and the result type of
	 *          its reverse.
	 * @param <R>
	 *          The result type of the forward function, and the operand type of
	 *          its reverse.
	 * @param function
	 *          The function in forward direction.
	 * @param reverse
	 *          The reverse of the function.
	 * @return An invertible function using the two given functions.
	 */
	public static <T, R> InvertibleFunction<T, R> over(
			Function<? super T, ? extends R> function,
			Function<? super R, ? extends T> reverse) {
		return new InvertibleFunction<T, R>() {
			@Override
			public R apply(T t) {
				return function.apply(t);
			}

			@Override
			public InvertibleFunction<R, T> getInverse() {
				return over(reverse, function);
			}
		};
	}

	/**
	 * @param <T>
	 *          The operand type of the first function.
	 * @param <I>
	 *          An intermediate type which the result type of the first function
	 *          can be assigned to, and which can assign to the operand type of
	 *          the second function.
	 * @param <R>
	 *          The result type of the second function.
	 * @param first
	 *          The first function to compose.
	 * @param second
	 *          The second function to compose.
	 * @return Composition of two invertible functions into a single invertible
	 *         function.
	 */
	public static <T, I, R> InvertibleFunction<T, R> compose(
			InvertibleFunction<T, I> first, InvertibleFunction<I, R> second) {
		return over(first.andThen(second),
				second.getInverse().andThen(first.getInverse()));
	}
}
