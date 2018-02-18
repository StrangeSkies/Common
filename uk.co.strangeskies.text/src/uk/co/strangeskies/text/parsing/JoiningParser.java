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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.parsing;

import java.util.function.BiFunction;

public class JoiningParser<T, U, V> implements AbstractParser<T> {
	private final Parser<U> first;
	private final Parser<V> second;
	private final BiFunction<U, V, ? extends T> combinor;

	public JoiningParser(Parser<U> first, Parser<V> second,
			BiFunction<U, V, ? extends T> combinor) {
		this.first = first;
		this.second = second;
		this.combinor = combinor;
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState state) {
		ParseResult<U> firstValue;
		ParseResult<V> secondValue;

		firstValue = first.parseSubstring(state.toEnd(false));
		secondValue = second
				.parseSubstring(firstValue.state().toEnd(state.toEnd()));

		return firstValue.mapState(
				s -> s.addException(secondValue.state()).fromIndex(
						secondValue.state().fromIndex())).mapResult(
				f -> combinor.apply(f, secondValue.result()));
	}

	@Override
	public String toString() {
		return "Joining Parser";
	}
}
