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

import java.lang.reflect.AnnotatedType;
import java.util.Locale;
import java.util.Properties;

import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;

/**
 * A {@link Properties} interface to provide localized texts for use by the
 * {@link PropertyLoader} class itself, such as for reporting errors for
 * improperly structured localization classes, etc.
 * <p>
 * And implementation of this interface should be generated via
 * {@link PropertyLoader#getProperties(Class)}.
 * 
 * @author Elias N Vasylenko
 */
@PropertyConfiguration(resource = "OSGI-INF.l10n.bundle", evaluation = Evaluation.IMMEDIATE)
public interface PropertyLoaderProperties {
	/**
	 * Load the property with the key {@code property.loader.must.be.interface}.
	 * 
	 * @param accessor
	 *          the accessor to localize
	 * @return the accessor class must be an interface
	 */
	String mustBeInterface(Class<?> accessor);

	/**
	 * Load the property with the key {@code property.loader.illegal.return.type}.
	 * 
	 * @param type
	 *          the type of the property to parse
	 * @param key
	 *          the key for the value with the illegal type
	 * @return the method must return the expected type
	 */
	default String propertyValueTypeNotSupported(AnnotatedType type, String key) {
		return propertyValueTypeNotSupported(type.getType().getTypeName(), key);
	}

	String propertyValueTypeNotSupported(String typeName, String key);

	/**
	 * Load the property with the key
	 * {@code property.loader.translation.not.found.message}.
	 * 
	 * @param key
	 *          the method to find a translation for
	 * @return no translation for the given method
	 */
	String translationNotFoundMessage(String key);

	/**
	 * Load the property with the key {@code property.loader.locale.changed}.
	 * 
	 * @param manager
	 *          manager
	 * @param locale
	 *          the new locale
	 * @return locale has been changed for manager
	 */
	String localeChanged(LocaleProvider manager, Locale locale);

	/**
	 * Load the property with the key
	 * {@code property.loader.cannot.instantiate.strategy}.
	 * 
	 * @param strategy
	 *          the requested strategy
	 * @return cannot get an instance of the given resource strategy
	 */
	String cannotInstantiateStrategy(Class<? extends PropertyResourceStrategy<?>> strategy);
}
