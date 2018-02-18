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
package uk.co.strangeskies.text;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static uk.co.strangeskies.text.CamelCaseFormatterTest.Direction.BOTH;
import static uk.co.strangeskies.text.CamelCaseFormatterTest.Direction.FORMATTING;
import static uk.co.strangeskies.text.CamelCaseFormatterTest.Direction.UNFORMATTING;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import uk.co.strangeskies.text.CamelCaseFormatter.UnformattingCase;

@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class CamelCaseFormatterTest {
	enum Direction {
		FORMATTING, UNFORMATTING, BOTH
	}

	@Parameters(name = "[{0}] <-> [{1}] : {2}")
	public static Collection<Object[]> validMappings() {
		return asList(new Object[][] {

				{ "two___Words", "two.words", UNFORMATTING },

				{ "two___words", "two.words", UNFORMATTING },

				{ "2___Words", "2.words", UNFORMATTING },

				{ "words:__2", "words.2", UNFORMATTING },

				{ "1000lowercase", "1000.lowercase", UNFORMATTING },

				{ "TwoWords", "two.words", UNFORMATTING },

				{ "twoWords", "two...words", FORMATTING },

				{ "twoWords", "...two.words...", FORMATTING },

				{ "", "", BOTH },

				{ "word", "word", BOTH },

				{ "twoWords", "two.words", BOTH },

				{ "CAPITALWord", "CAPITAL.word", BOTH },

				{ "capitalSECOND", "capital.SECOND", BOTH },

				{ "capitalINMiddle", "capital.IN.middle", BOTH },

				{ "capitalIMiddle", "capital.I.middle", BOTH },

				{ "CAPITAL1000", "CAPITAL.1000", BOTH },

				{ "lowercase1000", "lowercase.1000", BOTH },

				{ "1000CAPITAL", "1000.CAPITAL", BOTH },

				{ "1000Lowercase", "1000.lowercase", BOTH },

				{ "1A2B3C", "1.A.2.B.3.C", BOTH }

		});
	}

	private final String camelCase;
	private final String unformatted;
	private final Direction direction;

	private final CamelCaseFormatter formatter;

	public CamelCaseFormatterTest(String camelCase, String unformatted, Direction direction) {
		this.camelCase = camelCase;
		this.unformatted = unformatted;
		this.direction = direction;

		formatter = new CamelCaseFormatter(".", false, UnformattingCase.FLATTENED);
	}

	@Test
	public void testFormatMapping() {
		assumeThat(direction, not(UNFORMATTING));

		assertThat(formatter.format(unformatted), is(camelCase));
	}

	@Test
	public void testUnformatMapping() {
		assumeThat(direction, not(FORMATTING));

		assertThat(formatter.unformat(camelCase), is(unformatted));
	}

	@Test
	public void testFormatMappingFail() {
		assumeThat(direction, is(UNFORMATTING));

		assertThat(formatter.format(unformatted), not(camelCase));
	}

	@Test
	public void testUnformatMappingFail() {
		assumeThat(direction, is(FORMATTING));

		assertThat(formatter.unformat(camelCase), not(unformatted));
	}
}
