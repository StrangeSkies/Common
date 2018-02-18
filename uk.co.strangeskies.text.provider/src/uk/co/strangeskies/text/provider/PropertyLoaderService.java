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
 * This file is part of uk.co.strangeskies.text.provider.
 *
 * uk.co.strangeskies.text.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.provider;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.observable.ObservableValue;
import uk.co.strangeskies.text.properties.LocaleProvider;
import uk.co.strangeskies.text.properties.PropertyAccessorConfiguration;
import uk.co.strangeskies.text.properties.PropertyLoader;
import uk.co.strangeskies.text.properties.PropertyLoaderProperties;
import uk.co.strangeskies.text.properties.PropertyResourceStrategy;
import uk.co.strangeskies.text.properties.PropertyValueProvider;
import uk.co.strangeskies.text.properties.PropertyValueProviderFactory;

@Component(scope = ServiceScope.PROTOTYPE)
@SuppressWarnings("javadoc")
public class PropertyLoaderService implements PropertyLoader {
  @Reference
  LocaleProvider provider;
  @Reference(cardinality = ReferenceCardinality.OPTIONAL)
  Log log;
  private PropertyLoader component;

  @Activate
  void activate(ComponentContext context) {
    component = PropertyLoader.getPropertyLoader(provider, Log.forwardingLog(() -> log));

    OsgiPropertyResourceStrategy osgiPropertyResourceStrategy = new OsgiPropertyResourceStrategy(
        context.getUsingBundle());

    component.setDefaultResourceStrategy(osgiPropertyResourceStrategy);
    component.registerResourceStrategy(osgiPropertyResourceStrategy);
  }

  @Override
  public Locale getLocale() {
    return provider.getLocale();
  }

  @Override
  public ObservableValue<Locale> locale() {
    return provider;
  }

  @Override
  public boolean registerValueProvider(PropertyValueProviderFactory propertyProvider) {
    return component.registerValueProvider(propertyProvider);
  }

  @Override
  public boolean unregisterValueProvider(PropertyValueProviderFactory propertyProvider) {
    return component.unregisterValueProvider(propertyProvider);
  }

  @Override
  public List<PropertyValueProviderFactory> getValueProviders() {
    return component.getValueProviders();
  }

  @Override
  public Optional<PropertyValueProvider<?>> getValueProvider(AnnotatedType type) {
    return component.getValueProvider(type);
  }

  @Override
  public <T extends PropertyResourceStrategy<T>> void setDefaultResourceStrategy(T strategy) {
    component.setDefaultResourceStrategy(strategy);
  }

  @Override
  public <T extends PropertyResourceStrategy<T>> boolean registerResourceStrategy(T strategy) {
    return component.registerResourceStrategy(strategy);
  }

  @Override
  public <T extends PropertyResourceStrategy<T>> boolean unregisterResourceStrategy(T strategy) {
    return component.unregisterResourceStrategy(strategy);
  }

  @Override
  public Set<Class<? extends PropertyResourceStrategy<?>>> getResourceStrategies() {
    return component.getResourceStrategies();
  }

  @Override
  public <T extends PropertyResourceStrategy<T>> T getResourceStrategy(Class<T> strategy) {
    return component.getResourceStrategy(strategy);
  }

  @Override
  public <T> T getProperties(Class<T> accessor) {
    return component.getProperties(accessor);
  }

  @Override
  public <T> T getProperties(PropertyAccessorConfiguration<T> accessorConfiguration) {
    return component.getProperties(accessorConfiguration);
  }

  @Override
  public PropertyLoaderProperties getProperties() {
    return component.getProperties();
  }
}
