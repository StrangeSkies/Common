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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;

import uk.co.strangeskies.text.properties.PropertyConfiguration.Defaults;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

/**
 * A {@link PropertyConfiguration configuration} over a particular
 * {@link Properties property accessor} interface.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the property accessor interface
 */
public class PropertyAccessorConfiguration<T> {
	@PropertyConfiguration
	private final class DefaultPropertyConfigurationAnnotation {}

	private final Class<T> accessor;
	private final PropertyConfiguration configuration;

	public PropertyAccessorConfiguration(Class<T> accessor) {
		this(accessor, accessor.getAnnotation(PropertyConfiguration.class));
	}

	private PropertyAccessorConfiguration(Class<T> accessor, PropertyConfiguration configuration) {
		this.accessor = accessor;
		this.configuration = configuration != null ? configuration
				: DefaultPropertyConfigurationAnnotation.class.getAnnotation(PropertyConfiguration.class);
	}

	public <A> PropertyAccessorConfiguration<A> derive(Class<A> accessor) {
		return new PropertyAccessorConfiguration<>(
				accessor,
				deriveConfiguration(accessor.getAnnotation(PropertyConfiguration.class)));
	}

	public PropertyAccessorConfiguration<T> derive(PropertyConfiguration configuration) {
		if (configuration != null) {
			return new PropertyAccessorConfiguration<>(accessor, deriveConfiguration(configuration));
		} else {
			return this;
		}
	}

	public Class<T> getAccessor() {
		return accessor;
	}

	public PropertyConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PropertyAccessorConfiguration))
			return false;

		PropertyAccessorConfiguration<?> other = (PropertyAccessorConfiguration<?>) obj;

		return accessor.equals(other.accessor) && configuration.equals(other.configuration);
	}

	@Override
	public int hashCode() {
		return accessor.hashCode() ^ configuration.hashCode();
	}

	@SuppressWarnings("unchecked")
	PropertyConfiguration deriveConfiguration(PropertyConfiguration configuration) {
		Class<? extends PropertyResourceStrategy<?>> strategy;
		String resource;
		Evaluation evaluate;
		Defaults defaults;
		String keySplitString;
		KeyCase keyCase;
		String key;

		boolean merged = false;

		if (configuration.strategy().equals(PropertyResourceStrategy.class)) {
			merged = true;
			strategy = (Class<? extends PropertyResourceStrategy<?>>) getConfiguration().strategy();
		} else {
			strategy = (Class<? extends PropertyResourceStrategy<?>>) configuration.strategy();
		}

		if (configuration.resource().equals(PropertyConfiguration.UNSPECIFIED_RESOURCE)) {
			merged = true;
			resource = getConfiguration().resource();
		} else {
			resource = configuration.resource();
		}

		if (configuration.evaluation().equals(Evaluation.UNSPECIFIED)) {
			merged = true;
			evaluate = getConfiguration().evaluation();
		} else {
			evaluate = configuration.evaluation();
		}

		if (configuration.defaults().equals(Defaults.UNSPECIFIED)) {
			merged = true;
			defaults = getConfiguration().defaults();
		} else {
			defaults = configuration.defaults();
		}

		if (configuration.keySplitString().equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			merged = true;
			keySplitString = getConfiguration().keySplitString();
		} else {
			keySplitString = configuration.keySplitString();
		}

		if (configuration.keyCase().equals(KeyCase.UNSPECIFIED)) {
			merged = true;
			keyCase = getConfiguration().keyCase();
		} else {
			keyCase = configuration.keyCase();
		}

		if (configuration.key().equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			merged = true;
			key = getConfiguration().key();
		} else {
			key = configuration.key();
		}

		if (merged) {
			configuration = new PropertyConfiguration() {
				@Override
				public Class<PropertyConfiguration> annotationType() {
					return PropertyConfiguration.class;
				}

				@Override
				public Class<? extends PropertyResourceStrategy<?>> strategy() {
					return strategy;
				}

				@Override
				public String resource() {
					return resource;
				}

				@Override
				public Evaluation evaluation() {
					return evaluate;
				}

				@Override
				public Defaults defaults() {
					return defaults;
				}

				@Override
				public String keySplitString() {
					return keySplitString;
				}

				@Override
				public KeyCase keyCase() {
					return keyCase;
				}

				@Override
				public String key() {
					return key;
				}

				@Override
				public int hashCode() {
					try {
						int hashCode = 0;
						for (Method propertyMethod : PropertyConfiguration.class.getDeclaredMethods()) {
							int nameHash = propertyMethod.getName().hashCode() * 127;
							Object valueHash = propertyMethod.invoke(this);

							hashCode += nameHash ^ valueHash.hashCode();
						}
						return hashCode;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}

				@Override
				public boolean equals(Object obj) {
					try {
						for (Method propertyMethod : PropertyConfiguration.class.getDeclaredMethods()) {
							if (!Objects.equals(propertyMethod.invoke(this), propertyMethod.invoke(obj))) {
								return false;
							}
						}
						return true;
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}

		return configuration;
	}
}
