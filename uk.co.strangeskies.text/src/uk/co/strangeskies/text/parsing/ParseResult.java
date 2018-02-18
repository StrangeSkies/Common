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

import java.util.function.Function;

public class ParseResult<T> {
	private final int fromIndex;
	private final ParseState state;
	private final T result;

	ParseResult(ParseState state, int toIndex, T result) {
		this(state.fromIndex(), state.fromIndex(toIndex), result);
	}

	ParseResult(int fromIndex, ParseState state, T result) {
		if (state.toEnd() && state.fromIndex() < state.literal().length())
			throw state
					.fromIndex(fromIndex)
					.addException("Cannot match literal as end of input not reached",
							state.fromIndex()).getException();

		this.fromIndex = fromIndex;
		this.state = state;
		this.result = result;
	}

	public ParseState state() {
		return state;
	}

	public T result() {
		return result;
	}

	public <U> ParseResult<U> mapResult(Function<? super T, ? extends U> transform) {
		try {
			return new ParseResult<U>(fromIndex, state, transform.apply(result));
		} catch (Exception e) {
			throw state
					.fromIndex(fromIndex)
					.addException("Cannot apply transformation to parse result",
							state.fromIndex(), e).getException();
		}
	}

	public ParseResult<T> mapState(Function<ParseState, ParseState> transform) {
		return new ParseResult<>(fromIndex, transform.apply(state), result);
	}
}
