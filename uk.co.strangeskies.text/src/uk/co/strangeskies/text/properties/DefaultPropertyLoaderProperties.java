/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import static java.lang.String.format;

import java.util.Locale;

final class DefaultPropertyLoaderProperties extends StaticPropertyAccessor<PropertyLoaderProperties>
		implements PropertyLoaderProperties {
	private static final String TRANSLATION_NOT_FOUND = "?%s?";
	private static final String TRANSLATION_NOT_FOUND_MESSAGE = "Translation not found for key %s";
	private static final String MUST_BE_INTERFACE = "Localization accessor %s must be an interface";
	private static final String ILLEGAL_RETURN_TYPE = "Illegal return type %s for key %s";
	private static final String LOCALE_CHANGED = "Locale changed to %s";
	private static final String CANNOT_INSTANTIATE_STRATEGY = "Cannot instantiate strategy %s";

	public DefaultPropertyLoaderProperties() {
		super(Locale.ENGLISH, PropertyLoaderProperties.class);
	}

	@Override
	public String translationNotFoundSubstitution(String key) {
		return format(TRANSLATION_NOT_FOUND, key);
	}

	@Override
	public Localized<String> translationNotFoundMessage(String key) {
		return localize(TRANSLATION_NOT_FOUND_MESSAGE, key);
	}

	@Override
	public Localized<String> mustBeInterface(Class<?> accessor) {
		return localize(MUST_BE_INTERFACE, accessor);
	}

	@Override
	public Localized<String> propertyValueTypeNotSupported(String typeName, String key) {
		return localize(ILLEGAL_RETURN_TYPE, typeName, key);
	}

	@Override
	public Localized<String> localeChanged(LocaleProvider manager, Locale locale) {
		return localize(LOCALE_CHANGED, locale);
	}

	@Override
	public Localized<String> cannotInstantiateStrategy(Class<? extends PropertyResourceStrategy<?>> strategy) {
		return localize(CANNOT_INSTANTIATE_STRATEGY, strategy);
	}
}
