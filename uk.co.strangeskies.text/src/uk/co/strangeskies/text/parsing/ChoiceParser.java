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
import java.util.function.Supplier;

public class ChoiceParser<U, T> extends ParserProxy<U, T> {
	private final Parser<T> onFailure;

	public ChoiceParser(Supplier<Parser<U>> component,
			Parser<? extends U> onFailure, Function<? super U, ? extends T> transform) {
		super(component, transform);

		this.onFailure = onFailure.transform(transform);
	}

	@SuppressWarnings("unchecked")
	public ChoiceParser(Supplier<Parser<U>> component,
			Function<? super U, ? extends T> transform, Parser<? extends T> onFailure) {
		super(component, transform);

		this.onFailure = (Parser<T>) onFailure;
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState state) {
		ParseResult<T> value;
		try {
			value = super.parseSubstringImpl(state);
		} catch (ParseException e) {
			try {
				value = onFailure.parseSubstring(state)
						.mapState(s -> s.addException(e));
			} catch (ParseException e2) {
				throw ParseException.getHigher(e, e2);
			}
		} catch (Exception e) {
			value = onFailure.parseSubstring(state);
		}
		return value;
	}

	@Override
	public <V> Parser<V> transform(Function<? super T, ? extends V> transform) {
		return new ChoiceParser<>(getComponent(),
				getTransform().andThen(transform), onFailure.transform(transform));
	}
}
