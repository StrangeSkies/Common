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

public class PrependingParser<T, U> implements AbstractParser<T> {
	private final Parser<T> main;
	private final Parser<U> prepend;

	private final BiFunction<T, U, ? extends T> combinor;

	public PrependingParser(Parser<T> main, Parser<U> prepend,
			BiFunction<T, U, ? extends T> combinor) {
		this.main = main;
		this.prepend = prepend;

		this.combinor = combinor;
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState state) {
		ParseResult<U> prependValue;
		ParseResult<T> mainValue;

		try {
			prependValue = prepend.parseSubstring(state.toEnd(false));
		} catch (ParseException e) {
			try {
				return main.parseSubstring(state).mapState(s -> s.addException(e));
			} catch (ParseException e2) {
				throw ParseException.getHigher(e, e2);
			} catch (Exception e2) {
				throw e;
			}
		} catch (Exception e) {
			return main.parseSubstring(state);
		}

		mainValue = main.parseSubstring(prependValue.state().toEnd(state.toEnd()));

		return mainValue.mapState(s -> s.addException(prependValue.state()))
				.mapResult(m -> combinor.apply(m, prependValue.result()));
	}

	@Override
	public String toString() {
		return "Prepending Parser [" + prepend + " > " + main + "]";
	}
}
