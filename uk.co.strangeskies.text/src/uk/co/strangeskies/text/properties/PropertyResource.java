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

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A resource bundle with a configurable locale
 * 
 * @author Elias N Vasylenko
 */
public interface PropertyResource {
	/**
	 * @return the set of available keys according to the root locale
	 */
	default Set<String> getKeys() {
		return getKeys(Locale.ROOT);
	}

	/**
	 * @param locale
	 *          the locale
	 * @return the set of available keys according to the given locale
	 */
	Set<String> getKeys(Locale locale);

	/**
	 * @param key
	 *          the key of the property
	 * @return the value of the property of the given key according to the root
	 *         locale
	 */
	default String getValue(String key) {
		return getValue(key, Locale.ROOT);
	}

	/**
	 * @param key
	 *          the key of the property
	 * @param locale
	 *          the locale
	 * @return the value of the property of the given key according to the given
	 *         locale
	 */
	String getValue(String key, Locale locale);

	/**
	 * @return the strategy employed to locate the resources backing this bundle
	 */
	PropertyResourceStrategy<?> getStrategy();

	/**
	 * @return the property resource configuration
	 */
	Class<?> getAccessor();

	/**
	 * Create a {@link PropertyResource localizing resource bundle} over the given
	 * configuration.
	 * 
	 * 
	 * 
	 * 
	 * TODO
	 * 
	 * 
	 * 
	 * The created resource bundle will behave as a delegate to a series of
	 * {@link ResourceBundle resource bundles} , which in turn will behave
	 * according to {@link ResourceBundle#getBundle(String, Locale, ClassLoader)}
	 * with the given class loader, using the given locations as base names, and
	 * using the current locale of the {@link PropertyResource}. This locale may
	 * change, and the delegate resource bundles will be updated accordingly.
	 * 
	 * @param locale
	 *          the locale for the resource bundle
	 * @param accessor
	 *          the base names of properties files to load, and the class loaders
	 *          they exist in
	 * @return a resource bundle over all resources at each given location
	 */
	public static <T> PropertyResource getBundle(Locale locale, Class<T> accessor, String resource) {
    return PropertyResourceBundleStrategy
        .getInstance()
        .getPropertyResourceBundle(accessor, resource);
  }
}
