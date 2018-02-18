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

public class ParseException extends RuntimeException {
	private static final long serialVersionUID = -1506209486799438577L;

	private final String message;
	private final String literal;
	private final int fromIndex;
	private final int toIndex;

	public ParseException(String message, String literal, int fromIndex, int toIndex, Throwable cause) {
		super(composeMessage(message, literal, fromIndex, toIndex), cause);
		this.message = message;
		this.literal = literal;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	public ParseException(String message, String literal, int fromIndex, int toIndex) {
		super(composeMessage(message, literal, fromIndex, toIndex));
		this.message = message;
		this.literal = literal;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
	}

	private static String composeMessage(String message, String literal, int fromIndex, int toIndex) {
		StringBuilder builder = new StringBuilder(message).append(System.lineSeparator()).append(literal)
				.append(System.lineSeparator());

		for (int i = 0; i < fromIndex; i++)
			builder.append(" ");
		builder.append("^");

		for (int i = fromIndex; i < toIndex; i++)
			builder.append(".");
		builder.append("^");

		return builder.toString();
	}

	public String getLiteral() {
		return literal;
	}

	public int getStartIndex() {
		return fromIndex;
	}

	public int getIndexReached() {
		return toIndex;
	}

	public String getSimpleMessage() {
		return message;
	}

	public static ParseException getHigher(ParseException first, ParseException second) {
		if (first.getIndexReached() > second.getIndexReached())
			return first;
		else
			return second;
	}
}
