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

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theory;

import uk.co.strangeskies.utility.Enumeration;

/**
 * Careful design is necessary here, as in some cases we are testing what
 * happens during class initialisation, so extra thought must be put into test
 * isolation
 * 
 * @author Elias N Vasylenko
 *
 */
@SuppressWarnings("javadoc")
public class StringEscaperTest {
	@DataPoint
	public static final String SENTENCE = "The small brown dog.";

	/**
	 * Confirm that {@link Enumeration}s are properly initialised when accessed
	 * first by literal, rather than via {@link Enumeration#getConstants(Class)}.
	 * This is significant because of the odd initialisation logic to enforce
	 * instantiation only inside static initialisers.
	 */
	@Theory
	public void testEnumLiteralAccess() {}

	/**
	 * Confirm that an {@link Enumeration} works as an inner class (implementation
	 * may perform stack trace examination).
	 */
	@Theory
	public void testInnerEnum() {}
}
