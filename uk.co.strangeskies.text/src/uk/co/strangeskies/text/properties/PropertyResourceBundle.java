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

import static java.util.Collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;

/**
 * A simple {@link PropertyResource} implementation backed by one or more
 * {@link ResourceBundle resource bundles}.
 * 
 * @author Elias N Vasylenko
 */
public class PropertyResourceBundle implements PropertyResource {
	private static final String PROPERTIES_POSTFIX = "Properties";

	private final PropertyResourceStrategy<?> strategy;
	private final Class<?> accessor;
	private final Set<ResourceBundleDescriptor> resources;
	private final MultiMap<Locale, ResourceBundle, List<ResourceBundle>> localizedResourceBundles;

	/**
	 * Create a resource bundle with the given initial locale.
	 * 
	 * @param strategy
	 *          the strategy responsible for initializing this resource from the
	 *          appropriate {@link PropertyConfiguration}
	 * @param accessor
	 *          the accessor class type
	 * @param resource
	 *          the resource location setting from the appropriate
	 *          {@link PropertyConfiguration}
	 */
	protected PropertyResourceBundle(PropertyResourceStrategy<?> strategy, Class<?> accessor, String resource) {
		this.strategy = strategy;
		this.accessor = accessor;
		localizedResourceBundles = new MultiHashMap<>(ArrayList::new);

		resources = new LinkedHashSet<>(getResources(accessor, resource));

		if (getResourceBundles(Locale.ROOT).isEmpty()) {
			throw new MissingResourceException(
					"Cannot find resources for any of " + resources + " for " + accessor,
					accessor.toString(),
					"");
		}
	}

	@Override
	public PropertyResourceStrategy<?> getStrategy() {
		return strategy;
	}

	@Override
	public Class<?> getAccessor() {
		return accessor;
	}

	@Override
	public Set<String> getKeys(Locale locale) {
		Set<String> keys = new LinkedHashSet<>();

		for (ResourceBundle bundle : getResourceBundles(locale)) {
			keys.addAll(list(bundle.getKeys()));
		}

		return keys;
	}

	@Override
	public String getValue(String key, Locale locale) {
		for (ResourceBundle bundle : getResourceBundles(locale)) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {}
		}

		throw new MissingResourceException(
				"Cannot find resources for key " + key + " in locale " + locale + " in any of " + resources + " for "
						+ accessor,
				accessor.toString(),
				"");
	}

	protected synchronized List<ResourceBundle> getResourceBundles(Locale locale) {
		if (localizedResourceBundles.containsKey(locale)) {
			return localizedResourceBundles.get(locale);
		} else {
			List<ResourceBundle> resourceBundles = localizedResourceBundles.getCollection(locale);

			for (ResourceBundleDescriptor resource : resources) {
				try {
					resourceBundles.add(ResourceBundle.getBundle(resource.getLocation(), locale, resource.getClassLoader()));
				} catch (MissingResourceException e) {}
			}

			return resourceBundles;
		}
	}

	protected <T> List<ResourceBundleDescriptor> getResources(Class<T> accessor, String resource) {
		if (resource.equals(PropertyConfiguration.UNSPECIFIED_RESOURCE)) {
			resource = removePropertiesPostfix(accessor.getName());
		}

		return Arrays.asList(new ResourceBundleDescriptor(accessor.getClassLoader(), resource));
	}

	/**
	 * @param name
	 *          the string to remove the postfix from
	 * @return the given string, with the simple class name {@link Properties}
	 *         removed from the end, if present.
	 */
	public static String removePropertiesPostfix(String name) {
		if (name.endsWith(PROPERTIES_POSTFIX) && name.length() > PROPERTIES_POSTFIX.length()) {
			name = name.substring(0, name.length() - PROPERTIES_POSTFIX.length());
		}

		return name;
	}
}
