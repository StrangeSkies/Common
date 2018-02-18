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
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableValue;

/**
 * This interface represents a simple but powerful system for
 * internationalization. Instances of this class provide automatic
 * implementations of sub-interfaces of {@link Properties} according to a
 * {@link Locale} setting, which delegate method invocations to be fetched from
 * {@link ResourceBundle resource bundles}.
 * 
 * <p>
 * A property accessor interface is an interface to provide an API over static
 * properties and localized text and data. Users should not implement this class
 * themselves, instead they should define sub-interfaces, allowing
 * implementations to be automatically provided by {@link PropertyLoader}.
 * <p>
 * User defined methods on a property accessor interface may return values of
 * any type, so long as a {@link PropertyValueProvider} is given to the property
 * loader which supports that type. Special handling is performed for the
 * {@link Localized} type, and for methods returning nested accessor classes.
 * <p>
 * A key is generated for each method based on the class and method name. The
 * key is generated according to the {@link PropertyConfiguration} used to load
 * the {@link Properties} instance.
 * <p>
 * Default and static methods will be invoked directly.
 * <p>
 * A {@link Properties} instance is {@link Observable} over changes to its
 * locale, with the instance itself being passed as the message to observers.
 * <p>
 * For an example of how to use this interface, users may wish to take a look at
 * the {@link PropertyLoaderProperties} interface.
 * 
 * @author Elias N Vasylenko
 */
public interface PropertyLoader {
  /**
   * As returned by {@link #getDefaultPropertyLoader()}.
   */
  PropertyLoader DEFAULT_PROPERTY_LOADER = getPropertyLoader(LocaleManager.getDefaultManager());

  /**
   * @return the current locale of all localized texts implemented by this
   *         {@link PropertyLoader}
   */
  Locale getLocale();

  /**
   * @return an observable over changes to the locale
   */
  ObservableValue<Locale> locale();

  /**
   * @param propertyProvider
   *          a provider for a type of property
   * @return true if the provider was registered, false otherwise
   */
  boolean registerValueProvider(PropertyValueProviderFactory propertyProvider);

  /**
   * 
   * @param propertyProvider
   *          a provider for a type of property
   * @return true if the provider was unregistered, false otherwise
   */
  boolean unregisterValueProvider(PropertyValueProviderFactory propertyProvider);

  /**
   * @return all available property providers
   */
  List<PropertyValueProviderFactory> getValueProviders();

  /**
   * @param type
   *          the exact annotated type to provide
   * @return a property value provider aggregating all available
   *         {@link PropertyValueProviderFactory factories} over the given type
   */
  Optional<PropertyValueProvider<?>> getValueProvider(AnnotatedType type);

  /**
   * @param strategy
   *          the strategy to register
   * @return true if the strategy was registered, false otherwise
   */
  <T extends PropertyResourceStrategy<T>> boolean registerResourceStrategy(T strategy);

  /**
   * @param strategy
   *          the strategy to unregister
   * @return true if the strategy was unregistered, false otherwise
   */
  <T extends PropertyResourceStrategy<T>> boolean unregisterResourceStrategy(T strategy);

  Set<Class<? extends PropertyResourceStrategy<?>>> getResourceStrategies();

  <T extends PropertyResourceStrategy<T>> T getResourceStrategy(Class<T> strategy);

  <T extends PropertyResourceStrategy<T>> void setDefaultResourceStrategy(T strategy);

  /**
   * Generate an implementing instance of the given accessor interface class,
   * according to the rules described by {@link Properties}.
   * 
   * @param accessorConfiguration
   *          configuration object for the sub-interface of {@link Properties} we
   *          wish to implement, and the default property configuration to apply
   * @return an implementation of the accessor interface
   * @param <T>
   *          the type of the localization text accessor interface
   */
  <T> T getProperties(PropertyAccessorConfiguration<T> accessorConfiguration);

  /**
   * Generate an implementing instance of the given accessor interface class,
   * according to the rules described by {@link Properties}.
   * 
   * @param <T>
   *          the type of the localization text accessor interface
   * @param accessor
   *          the sub-interface of {@link Properties} we wish to implement
   * @return an implementation of the accessor interface
   */
  default <T> T getProperties(Class<T> accessor) {
    return getProperties(new PropertyAccessorConfiguration<>(accessor));
  }

  /**
   * @return the properties associated directly with the property loader itself
   */
  PropertyLoaderProperties getProperties();

  /**
   * Get a simple {@link PropertyLoader} implementation over the
   * {@link LocaleManager#getDefaultManager() system locale}.
   * 
   * @return a {@link PropertyLoader} for the system locale
   */
  static PropertyLoader getDefaultPropertyLoader() {
    return DEFAULT_PROPERTY_LOADER;
  }

  /**
   * Get a {@link #getProperties() property implementation} from the
   * {@link #getDefaultPropertyLoader() default loader}.
   * 
   * @param accessorConfiguration
   *          configuration object for the sub-interface of {@link Properties} we
   *          wish to implement, and the default property configuration to apply
   * @return an implementation of the accessor interface
   * @param <T>
   *          the type of the localization text accessor interface
   */
  static <T> T getDefaultProperties(PropertyAccessorConfiguration<T> accessorConfiguration) {
    return getDefaultPropertyLoader().getProperties(accessorConfiguration);
  }

  /**
   * Generate an implementing instance of the given accessor interface class,
   * according to the rules described by {@link Properties}.
   * 
   * @param <T>
   *          the type of the localization text accessor interface
   * @param accessor
   *          the sub-interface of {@link Properties} we wish to implement
   * @return an implementation of the accessor interface
   */
  static <T> T getDefaultProperties(Class<T> accessor) {
    return getDefaultPropertyLoader().getProperties(new PropertyAccessorConfiguration<>(accessor));
  }

  /**
   * Get a simple {@link PropertyLoader} implementation over the given locale
   * provider.
   * 
   * @param provider
   *          a provider to establish a locale setting
   * @return a {@link PropertyLoader} for the given locale
   */
  static PropertyLoader getPropertyLoader(LocaleProvider provider) {
    return getPropertyLoader(provider, null);
  }

  /**
   * Get a simple {@link PropertyLoader} implementation over the given locale
   * provider.
   * 
   * @param provider
   *          a provider to establish a locale setting
   * @param log
   *          a log for localization information
   * @return a {@link PropertyLoader} for the given locale
   */
  static PropertyLoader getPropertyLoader(LocaleProvider provider, Log log) {
    return new PropertyLoaderImpl(provider, log);
  }
}
