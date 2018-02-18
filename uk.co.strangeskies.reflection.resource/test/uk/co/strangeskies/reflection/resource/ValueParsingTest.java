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
 * This file is part of uk.co.strangeskies.reflection.resource.
 *
 * uk.co.strangeskies.reflection.resource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.resource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.resource;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.reflection.resource.ManifestAttributeParser;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class ValueParsingTest {
	public static class ValueTheory {
		private final String value;
		private final String parses;

		private String parsed;

		public ValueTheory(String parses, String value) {
			this.value = value;

			this.parses = parses;
		}

		public String value() {
			return value;
		}

		public String parse() {
			if (parsed == null) {
				parsed = new ManifestAttributeParser().parseValueString(parses);
			}
			return parsed;
		}
	}

	private static ValueTheory newTheory(String parses, String value) {
		return new ValueTheory(parses, value);
	}

	@DataPoint
	public static final ValueTheory simpleString = newTheory("simpleString", "simpleString");

	@DataPoint
	public static final ValueTheory doubleQuotedString = newTheory("\"quotedString\"", "quotedString");

	@DataPoint
	public static final ValueTheory doubleQuotedEscapedString = newTheory("\"quoted\\\"String\"", "quoted\"String");

	@Theory
	public void testValueParserValid(ValueTheory theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testValueParser(ValueTheory theory) {
		Assert.assertEquals(theory.value(), theory.parse());
	}
}
