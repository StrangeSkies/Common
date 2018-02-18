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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser<T> implements AbstractParser<T> {
	private final Pattern pattern;
	private final Function<String, T> transform;

	public RegexParser(String pattern, Function<String, T> transform) {
		this.pattern = Pattern.compile(pattern);
		this.transform = transform;
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState state) {
		Matcher matcher = pattern.matcher(state.literal()).region(
				state.fromIndex(), state.literal().length());

		boolean matches;
		if (state.toEnd())
			matches = matcher.matches();
		else
			matches = matcher.lookingAt();

		if (!matches) {
			for (int endIndex = state.fromIndex() + 1; endIndex < state.literal()
					.length(); endIndex++) {
				// TODO binary search for length

				Matcher partialMatcher = matcher.region(state.fromIndex(), endIndex);
				if (!partialMatcher.matches()) {
					throw state.addException("Cannot match pattern '" + pattern + "'",
							endIndex - 1).getException();
				}
			}

			throw state.addException("Cannot match pattern '" + pattern + "'",
					state.literal().length()).getException();
		}

		return state.parseTo(matcher.end(), transform);
	}

	@Override
	public String toString() {
		return "Regular Expression Parser " + pattern;
	}
}
