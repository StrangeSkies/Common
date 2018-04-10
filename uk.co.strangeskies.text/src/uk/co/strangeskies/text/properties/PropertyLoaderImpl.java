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

import static java.util.Arrays.stream;
import static java.util.Collections.synchronizedSet;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import uk.co.strangeskies.collection.computingmap.CacheComputingMap;
import uk.co.strangeskies.collection.computingmap.ComputingMap;
import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;
import uk.co.strangeskies.observable.ObservableValue;
import uk.co.strangeskies.text.parsing.DateTimeParser;
import uk.co.strangeskies.text.parsing.Parser;

class PropertyLoaderImpl implements PropertyLoader {
  static class MethodSignature {
    private final Method method;
    private final Class<?>[] type;

    public MethodSignature(Method method) {
      this.method = method;
      this.type = method.getParameterTypes();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (obj == this)
        return true;
      if (!(obj instanceof MethodSignature))
        return false;

      MethodSignature other = (MethodSignature) obj;

      return name().equals(other.name()) && Arrays.equals(type(), other.type());
    }

    public Method method() {
      return method;
    }

    public String name() {
      return method.getName();
    }

    public Class<?>[] type() {
      return type;
    }

    @Override
    public int hashCode() {
      return name().hashCode() ^ Arrays.hashCode(type());
    }
  }

  private final Map<Class<? extends PropertyResourceStrategy<?>>, PropertyResourceStrategy<?>> resourceStrategies;
  private final Set<PropertyValueProviderFactory> propertyProviders;
  private final ComputingMap<PropertyAccessorConfiguration<?>, Object> localizationCache;

  private final LocaleProvider locale;
  private Log log;

  private final PropertyLoaderProperties text;

  /**
   * Create a new {@link PropertyLoader} instance for the given initial locale.
   * 
   * @param locale
   *          the initial locale
   * @param log
   *          the log for localization
   */
  public PropertyLoaderImpl(LocaleProvider locale, Log log) {
    resourceStrategies = new ConcurrentHashMap<>();
    registerResourceStrategy(PropertyResourceBundleStrategy.getInstance());
    setDefaultResourceStrategy(PropertyResourceBundleStrategy.getInstance());

    propertyProviders = synchronizedSet(new LinkedHashSet<>());
    registerValueProvider(stringProvider());
    registerValueProvider(listProvider());
    registerValueProvider(optionalProvider());
    registerValueProvider(localDateProvider());

    localizationCache = new CacheComputingMap<>(c -> instantiateProperties(c), true);

    this.locale = locale;
    this.log = log;

    PropertyLoaderProperties text;
    try {
      text = getProperties(PropertyLoaderProperties.class);
    } catch (Exception e) {
      text = new DefaultPropertyLoaderProperties();
    }
    this.text = text;

    if (log != null) {
      locale().observe(l -> {
        log.log(Level.INFO, getProperties().localeChanged(locale, getLocale()).toString());
      });
    }
  }

  private PropertyValueProviderFactory stringProvider() {
    return PropertyValueProviderFactory.over(
        String.class,
        a -> Parser.matchingAll().transform(s -> String.format(s, a.toArray())),
        (s, a) -> "?" + s + "?" + a);
  }

  private PropertyValueProviderFactory listProvider() {
    return new PropertyValueProviderFactory() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(
          AnnotatedType exactType,
          PropertyLoader loader) {

        if (exactType instanceof AnnotatedParameterizedType
            && ((ParameterizedType) exactType.getType()).getRawType().equals(List.class)) {

          AnnotatedType elementType = ((AnnotatedParameterizedType) exactType)
              .getAnnotatedActualTypeArguments()[0];

          return loader.getValueProvider(elementType).map(e -> {
            Delimit delimit = elementType.getAnnotation(Delimit.class);

            String delimiter = delimit != null ? delimit.value() : ",";
            if (delimit == null || delimit.ignoreWhitespace()) {
              delimiter = "\\s*" + delimiter + "\\s*";
            }
            String finalDelimiter = delimiter;

            if (delimit == null || delimit.eager()) {
              return (PropertyValueProvider<T>) PropertyValueProvider.over(
                  arguments -> Parser.matchingAll().transform(
                      s -> stream(s.split(finalDelimiter))
                          .map(e.getParser(arguments)::parse)
                          .collect(toList())));
            } else {
              return (PropertyValueProvider<T>) PropertyValueProvider.over(
                  arguments -> Parser.list(e.getParser(arguments), finalDelimiter),
                  (k, a) -> Collections.emptyList());
            }
          });
        } else {
          return empty();
        }
      }
    };
  }

  private PropertyValueProviderFactory optionalProvider() {
    return new PropertyValueProviderFactory() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(
          AnnotatedType exactType,
          PropertyLoader loader) {

        if (exactType instanceof AnnotatedParameterizedType
            && ((ParameterizedType) exactType.getType()).getRawType().equals(Optional.class)) {

          AnnotatedType elementType = ((AnnotatedParameterizedType) exactType)
              .getAnnotatedActualTypeArguments()[0];

          return loader.getValueProvider(elementType).<PropertyValueProvider<T>>map(
              p -> new PropertyValueProvider<T>() {
                @Override
                public Parser<T> getParser(List<?> arguments) {
                  Parser<T> optionalParser = p.getParser(arguments).transform(
                      v -> (T) Optional.ofNullable(v));

                  return optionalParser.orElse((T) Optional.empty());
                }

                @Override
                public boolean providesDefault() {
                  return true;
                };

                @Override
                public T getDefault(String keyString, List<?> arguments) {
                  return (T) Optional.empty();
                };
              });

        } else {
          return empty();
        }
      }
    };
  }

  private PropertyValueProviderFactory localDateProvider() {
    return PropertyValueProviderFactory.over(LocalDate.class, DateTimeParser.overIsoLocalDate());
  }

  @Override
  public PropertyLoaderProperties getProperties() {
    return text;
  }

  public Log getLog() {
    return Log.forwardingLog(() -> log);
  }

  @Override
  public Locale getLocale() {
    return locale.getLocale();
  }

  @Override
  public ObservableValue<Locale> locale() {
    return locale;
  }

  protected <T> T instantiateProperties(PropertyAccessorConfiguration<T> source) {
    return new PropertyAccessorDelegate<>(this, getLog(), source).getProxy();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getProperties(PropertyAccessorConfiguration<T> accessorConfiguration) {
    return (T) localizationCache.putGet(accessorConfiguration);
  }

  @Override
  public boolean registerValueProvider(PropertyValueProviderFactory propertyProvider) {
    return propertyProviders.add(propertyProvider);
  }

  @Override
  public boolean unregisterValueProvider(PropertyValueProviderFactory propertyProvider) {
    return propertyProviders.remove(propertyProvider);
  }

  @Override
  public List<PropertyValueProviderFactory> getValueProviders() {
    return new ArrayList<>(propertyProviders);
  }

  @Override
  public Optional<PropertyValueProvider<?>> getValueProvider(AnnotatedType type) {
    PropertyValueProviderFactory aggregateProvider = new PropertyValueProviderFactory() {
      @Override
      public <T> Optional<PropertyValueProvider<T>> getPropertyProvider(
          AnnotatedType exactType,
          PropertyLoader loader) {
        List<PropertyValueProvider<?>> providers = getValueProviders()
            .stream()
            .map(p -> p.getPropertyProvider(exactType, loader))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

        if (!providers.isEmpty()) {
          return Optional.of(new PropertyValueProvider<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public Parser<T> getParser(List<?> arguments) {
              return providers
                  .stream()
                  .map(p -> p.getParser(arguments))
                  .reduce(Parser::orElse)
                  .get()
                  .transform(v -> (T) v);
            }

            @Override
            public boolean providesDefault() {
              return providers.stream().anyMatch(PropertyValueProvider::providesDefault);
            };

            @SuppressWarnings("unchecked")
            @Override
            public T getDefault(String keyString, List<?> arguments) {
              return providers
                  .stream()
                  .filter(PropertyValueProvider::providesDefault)
                  .map(p -> (T) p.getDefault(keyString, arguments))
                  .findFirst()
                  .orElseGet(() -> PropertyValueProvider.super.getDefault(keyString, arguments));
            }
          });
        } else {
          return Optional.empty();
        }
      }
    };

    return aggregateProvider.getPropertyProvider(type, this).map(p -> (PropertyValueProvider<?>) p);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends PropertyResourceStrategy<T>> void setDefaultResourceStrategy(T strategy) {
    resourceStrategies.put(
        (Class<PropertyResourceStrategy<?>>) (Class<?>) PropertyResourceStrategy.class,
        strategy);

  }

  @Override
  public <T extends PropertyResourceStrategy<T>> boolean registerResourceStrategy(T strategy) {
    return resourceStrategies.putIfAbsent(strategy.strategyClass(), strategy) == null;
  }

  @Override
  public <T extends PropertyResourceStrategy<T>> boolean unregisterResourceStrategy(T strategy) {
    return resourceStrategies.remove(strategy.strategyClass()) != null;
  }

  @Override
  public Set<Class<? extends PropertyResourceStrategy<?>>> getResourceStrategies() {
    return Collections.unmodifiableSet(resourceStrategies.keySet());
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends PropertyResourceStrategy<T>> T getResourceStrategy(Class<T> strategy) {
    return (T) resourceStrategies.computeIfAbsent(strategy, k -> {
      try {
        return strategy.getConstructor().newInstance();
      } catch (
          InstantiationException
          | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
        throw new PropertyLoaderException(getProperties().cannotInstantiateStrategy(strategy), e);
      }
    });
  }
}
