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

import uk.co.strangeskies.reflection.resource.AttributeProperty;
import uk.co.strangeskies.reflection.resource.ManifestAttributeParser;
import uk.co.strangeskies.reflection.resource.PropertyType;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class AttributePropertyParsingTest {
  public static class AttributePropertyParsingTheory<T> extends AttributeProperty<T> {
    private final String parses;

    private AttributeProperty<?> parsed;

    public AttributePropertyParsingTheory(
        String parses,
        String name,
        PropertyType<T> type,
        T value) {
      super(name, type, value);

      this.parses = parses;
    }

    public AttributeProperty<?> parse() {
      if (parsed == null) {
        parsed = new ManifestAttributeParser().parseAttributeProperty(parses);
      }
      return parsed;
    }
  }

  private static <T> AttributePropertyParsingTheory<T> newTheory(
      String parses,
      String name,
      PropertyType<T> type,
      T value) {
    return new AttributePropertyParsingTheory<>(parses, name, type, value);
  }

  @DataPoint
  public static final AttributePropertyParsingTheory<String> simpleString = newTheory(
      "simpleString=test",
      "simpleString",
      null,
      "test");

  @DataPoint
  public static final AttributePropertyParsingTheory<String[]> singleStringArray = newTheory(
      "singleStringArray:List<String>=\"test\"",
      "singleStringArray",
      PropertyType.STRINGS,
      new String[] { "test" });

  @DataPoint
  public static final AttributePropertyParsingTheory<String[]> stringArray = newTheory(
      "stringArray:List<String>=\"test,test2\"",
      "stringArray",
      PropertyType.STRINGS,
      new String[] { "test", "test2" });

  public static final AttributePropertyParsingTheory<String[]> emptyStringArray = newTheory(
      "emptyStringArray:List<String>=\"\"",
      "emptyStringArray",
      PropertyType.STRINGS,
      new String[0]);

  @DataPoint
  public static final AttributePropertyParsingTheory<String[]> stringArrayWithCommas = newTheory(
      "stringArrayWithCommas:List<String>=\"test,te\\\\,st2,te\\\\\\\\,st3\"",
      "stringArrayWithCommas",
      PropertyType.STRINGS,
      new String[] { "test", "te,st2", "te\\,st3" });

  @DataPoint
  public static final AttributePropertyParsingTheory<String> emptyType = newTheory(
      "emptyType:=test",
      "emptyType",
      PropertyType.DIRECTIVE,
      "test");

  @DataPoint
  public static final AttributePropertyParsingTheory<String> typedString = newTheory(
      "typedString:String=test",
      "typedString",
      PropertyType.STRING,
      "test");

  @DataPoint
  public static final AttributePropertyParsingTheory<String> quotedString = newTheory(
      "quotedString:String=\"test\"",
      "quotedString",
      PropertyType.STRING,
      "test");

  @DataPoint
  public static final AttributePropertyParsingTheory<String> escapedQuoteString = newTheory(
      "escapedQuoteString:String=\"te\\\"st\"",
      "escapedQuoteString",
      PropertyType.STRING,
      "te\"st");

  @Theory
  public void testManifestEntryAttributeParserValid(AttributePropertyParsingTheory<?> theory) {
    Assert.assertNotNull(theory.parse());
  }

  @Theory
  public void testManifestEntryAttributeNameParser(AttributePropertyParsingTheory<?> theory) {
    Assert.assertNotNull(theory.parse().name());
    Assert.assertEquals(theory.name(), theory.parse().name());
  }

  @Theory
  public void testManifestEntryAttributeTypeParser(AttributePropertyParsingTheory<?> theory) {
    Assert.assertEquals(theory.type(), theory.parse().type());
  }

  @Theory
  public void testManifestEntryAttributeValueParser(AttributePropertyParsingTheory<?> theory) {
    Assert.assertNotNull(theory.parse().value());
    if (theory.value().getClass().isArray()) {
      Assert.assertArrayEquals((Object[]) theory.value(), (Object[]) theory.parse().value());
    } else {
      Assert.assertEquals(theory.value(), theory.parse().value());
    }
  }
}
