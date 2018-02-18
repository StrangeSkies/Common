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
package uk.co.strangeskies.text.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.co.strangeskies.text.properties.LocaleManager.getManager;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class PropertiesTest {
	public TestProperties text(LocaleManager manager) {
		try {
			return PropertyLoader.getPropertyLoader(manager).getProperties(TestProperties.class);
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	@Test
	public void managerTest() {
		getManager();
	}

	@Test
	public void propertiesTest() {
		text(getManager());
	}

	@Test
	public void simpleTextTest() {
		TestProperties text = text(getManager());

		assertEquals("simple property value", text.simple());
	}

	@Test
	public void substitutionTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of substitution", text.substitution("substitution"));
	}

	@Test
	public void multipleSubstitutionTextTest() {
		TestProperties text = text(getManager());

		assertEquals(
				"values of substitution one and substitution two",
				text.multipleSubstitution("substitution one", "substitution two"));
	}

	@Test
	public void defaultTextTest() {
		TestProperties text = text(getManager());

		assertEquals("value of default", text.defaultMethod());
	}

	@Test
	public void copyTextTest() {
		TestProperties text = text(getManager());

		assertEquals("simple property value", text.simple());
	}

	@Test
	public void nestedTextTest() {
		TestProperties text = text(getManager());

		assertEquals("nested text value", text.nesting().nestedText());
	}

	@Test
	public void deeplyNestedTextTest() {
		TestProperties text = text(getManager());

		assertEquals("deeply nested text value", text.nesting().deeply().deeplyNestedText());
	}

	@Test
	public void optionalPresentTextTest() {
		TestProperties text = text(getManager());

		assertTrue(text.optional().isPresent());
	}

	@Test
	public void optionalMissingTextTest() {
		TestProperties text = text(getManager());

		assertFalse(text.optionalMissing().isPresent());
	}

	@Test(expected = PropertyLoaderException.class)
	public void immediateRequirementTest() {
		text(getManager()).immediateRequirements();
	}

	@Test(expected = PropertyLoaderException.class)
	public void requiredPropertyTest() {
		TestProperties text = text(getManager());

		text.requiredProperty();
	}

	@Test
	public void immediateDefaultingPropertyTest() {
		TestProperties text = text(getManager());

		assertNotNull(text.immediateDefaultingProperty());
	}
}
