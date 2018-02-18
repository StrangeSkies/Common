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

import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public class DateTimeParser<T> implements AbstractParser<T> {
	private final DateTimeFormatter format;
	private final Function<TemporalAccessor, T> accessorFunction;

	protected DateTimeParser(DateTimeFormatter format, Function<TemporalAccessor, T> accessorFunction) {
		this.format = format;
		this.accessorFunction = accessorFunction;
	}

	public static DateTimeParser<LocalDate> overIsoLocalDate() {
		return over(DateTimeFormatter.ISO_LOCAL_DATE, LocalDate::from);
	}

	public static DateTimeParser<LocalDate> over(DateTimeFormatter format) {
		return over(format, LocalDate::from);
	}

	public static <T> DateTimeParser<T> over(DateTimeFormatter format, Function<TemporalAccessor, T> accessorFunction) {
		return new DateTimeParser<>(format, accessorFunction);
	}

	@Override
	public ParseResult<T> parseSubstringImpl(ParseState currentState) {
		ParsePosition position = new ParsePosition(currentState.fromIndex());

		try {
			TemporalAccessor accessor = format.parse(currentState.literal(), position);

			return currentState.parseTo(position.getIndex(), s -> accessorFunction.apply(accessor));
		} catch (Exception e) {
			throw currentState.addException("Cannot parse temporal accessor",
					position.getErrorIndex() > 0 ? position.getErrorIndex() : position.getIndex(), e).getException();
		}
	}

	public static void main(String... args) {
		System.out.println(Parser.list(overIsoLocalDate(), ",").parse("4567-01-23,010-01-23"));
	}
}
