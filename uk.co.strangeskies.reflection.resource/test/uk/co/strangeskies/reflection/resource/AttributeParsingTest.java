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

import uk.co.strangeskies.reflection.resource.Attribute;
import uk.co.strangeskies.reflection.resource.AttributeProperty;
import uk.co.strangeskies.reflection.resource.ManifestAttributeParser;
import uk.co.strangeskies.reflection.resource.PropertyType;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class AttributeParsingTest {
	public static class AttributeParsingTheory extends Attribute {
		private final String parses;

		private Attribute parsed;

		public AttributeParsingTheory(String parses, String name, AttributeProperty<?>... attributeProperties) {
			super(name, attributeProperties);

			this.parses = parses;
		}

		public Attribute parse() {
			if (parsed == null) {
				parsed = new ManifestAttributeParser().parseAttribute(parses);
			}
			return parsed;
		}
	}

	private static AttributeParsingTheory newTheory(String parses, String name,
			AttributeProperty<?>... attributeProperties) {
		return new AttributeParsingTheory(parses, name, attributeProperties);
	}

	@DataPoint
	public static final AttributeParsingTheory SIMPLE_STRING = newTheory("simpleString", "simpleString");

	@DataPoint
	public static final AttributeParsingTheory TYPED_PROPERTY = newTheory("typedProperty;property1:String=test",
			"typedProperty", new AttributeProperty<>("property1", PropertyType.STRING, "test"));

	@DataPoint
	public static final AttributeParsingTheory TWO_TYPED_PROPERTIES = newTheory(
			"twoTypedProperties;typedString:String=test;typedString2:String=\"test2\"", "twoTypedProperties",
			new AttributeProperty<>("typedString", PropertyType.STRING, "test"),
			new AttributeProperty<>("typedString2", PropertyType.STRING, "test2"));

	@Theory
	public void testManifestEntryAttributeParserValid(AttributeParsingTheory theory) {
		Assert.assertNotNull(theory.parse());
	}

	@Theory
	public void testManifestEntryAttributeNameParser(AttributeParsingTheory theory) {
		Assert.assertNotNull(theory.parse().name());
		Assert.assertEquals(theory.name(), theory.parse().name());
	}
}
