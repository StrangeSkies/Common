/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.text.manifest;

import static uk.co.strangeskies.text.parsing.Parser.matching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import uk.co.strangeskies.text.EscapeFormatter;
import uk.co.strangeskies.text.parsing.Parser;
import uk.co.strangeskies.utilities.tuple.Pair;

public class ManifestAttributeParser {
	static final EscapeFormatter DOUBLE_QUOTE_ESCAPER = new EscapeFormatter('\\', "\"");
	static final EscapeFormatter SINGLE_QUOTE_ESCAPER = new EscapeFormatter('\\', "'");

	static final String ALPHANUMERIC = "a-zA-Z0-9";
	static final String KEY = "[\\-\\._" + ALPHANUMERIC + "]+";
	static final String SIMPLE_VALUE = "[^;,=]*";
	static final String WHITESPACE = "\\s*";

	private final Parser<? extends List<? extends Attribute>> attributes;
	private final Parser<? extends Attribute> attribute;
	private final Parser<? extends AttributeProperty<?>> attributeProperty;
	private final Parser<? extends String> valueString;

	public ManifestAttributeParser(PropertyType<?>... knownPropertyTypes) {
		this(Arrays.asList(knownPropertyTypes));
	}

	public ManifestAttributeParser(Collection<? extends PropertyType<?>> knownPropertyTypes) {
		Parser<PropertyType<?>> type = matching("([" + ALPHANUMERIC + "]+(<[" + ALPHANUMERIC + "]+>)?)|")
				.transform(s -> PropertyType.fromName(s, knownPropertyTypes));

		Parser<String> doubleQuotedString = matching("([^\"\\\\]*(\\\\.[^\"\\\\]*)*)").prepend("\"").append("\"")
				.transform(DOUBLE_QUOTE_ESCAPER::unescape);
		Parser<String> singleQuotedString = matching("([^'\\\\]*(\\\\.[^\'\\\\]*)*)").prepend("'").append("'")
				.transform(SINGLE_QUOTE_ESCAPER::unescape);

		valueString = doubleQuotedString.orElse(singleQuotedString).orElse(matching(SIMPLE_VALUE));

		attributeProperty = matching(KEY).appendTransform(type.prepend(":").orElse(() -> null), this::newPair).append("=")
				.appendTransform(valueString, this::newAttributeProperty);

		attribute = matching(KEY).appendTransform(Parser.list(attributeProperty, WHITESPACE + ";" + WHITESPACE, 0)
				.prepend(WHITESPACE + ";" + WHITESPACE).orElse(Collections.emptyList()), Attribute::new);

		attributes = Parser.list(attribute, WHITESPACE + "," + WHITESPACE);
	}

	private Pair<String, PropertyType<?>> newPair(String name, PropertyType<?> type) {
		return new Pair<>(name, type);
	}

	private AttributeProperty<?> newAttributeProperty(Pair<String, PropertyType<?>> nameAndType, String valueString) {
		String name = nameAndType.getLeft();
		PropertyType<?> type = nameAndType.getRight();

		if (type == null) {
			return AttributeProperty.untyped(name, valueString);
		} else {
			return AttributeProperty.parseValueString(nameAndType.getLeft(), nameAndType.getRight(), valueString);
		}
	}

	public String parseValueString(String valueString) {
		return this.valueString.parse(valueString);
	}

	public AttributeProperty<?> parseAttributeProperty(String attribute) {
		return this.attributeProperty.parse(attribute);
	}

	public Attribute parseAttribute(String entry) {
		return this.attribute.parse(entry);
	}

	public List<Attribute> parseAttributes(String entry) {
		return new ArrayList<>(this.attributes.parse(entry));
	}
}
