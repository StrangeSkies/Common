/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.text.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static uk.co.strangeskies.text.properties.PropertyConfiguration.UNSPECIFIED_KEY_SPLIT_STRING;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import uk.co.strangeskies.text.CamelCaseFormatter;
import uk.co.strangeskies.text.CamelCaseFormatter.UnformattingCase;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Defaults;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;
import uk.co.strangeskies.text.properties.PropertyLoaderImpl.MethodSignature;
import uk.co.strangeskies.utilities.Log.Level;
import uk.co.strangeskies.utilities.ObservableImpl;
import uk.co.strangeskies.utilities.ObservablePropertyImpl;
import uk.co.strangeskies.utilities.ObservableValue;
import uk.co.strangeskies.utilities.classloading.DelegatingClassLoader;

/**
 * Delegate implementation object for proxy instances of LocalizationText
 * classes. This class deals with most method interception from the proxies
 * generated by {@link PropertyLoader}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <A>
 *          the type of the delegating {@link Properties} proxy
 */
public class PropertiesDelegate<A extends Properties<A>> extends ObservableImpl<A> implements Properties<A> {
	/*
	 * Implementation of localised property
	 */
	class LocalizedPropertyImpl<T> extends ObservablePropertyImpl<T, T> implements Localized<T>, Consumer<A> {
		private final PropertyResourceConfiguration<A> source;
		private final String key;
		private final Class<T> propertyClass;
		private final List<Object> arguments;
		private final Map<Locale, T> cache;

		public LocalizedPropertyImpl(PropertyResourceConfiguration<A> source, String key, Class<T> propertyClass,
				List<?> arguments) {
			super((r, t) -> r, Objects::equals, null);

			this.source = source;
			this.key = key;
			this.propertyClass = propertyClass;
			this.arguments = new ArrayList<>(arguments);
			this.cache = new ConcurrentHashMap<>();

			updateText();

			PropertiesDelegate.this.addWeakObserver(this);
		}

		private synchronized void updateText() {
			set(get(locale().get()));
		}

		@Override
		public String toString() {
			return get().toString();
		}

		@Override
		public T get(Locale locale) {
			return cache.computeIfAbsent(locale, l -> {
				PropertyValueProvider<T> provider = getClassProvider(key, propertyClass);

				return loadValue(source, provider, key, locale).instantiate(arguments);
			});
		}

		@Override
		public void accept(A t) {
			updateText();
		}

		@Override
		public ObservableValue<Locale> locale() {
			return loader.locale();
		}
	}

	private static final Set<MethodSignature> LOCALIZATION_HELPER_METHODS = getLocalizationHelperMethods();

	private static Set<MethodSignature> getLocalizationHelperMethods() {
		Set<MethodSignature> signatures = new HashSet<>();

		for (Method method : Properties.class.getMethods()) {
			signatures.add(new MethodSignature(method));
		}
		for (Method method : Object.class.getMethods()) {
			signatures.add(new MethodSignature(method));
		}

		return unmodifiableSet(signatures);
	}

	private static final Constructor<MethodHandles.Lookup> METHOD_HANDLE_CONSTRUCTOR = getMethodHandleConstructor();

	private static Constructor<Lookup> getMethodHandleConstructor() {
		try {
			Constructor<Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);

			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}

			return constructor;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private final PropertyLoaderImpl loader;
	private final PropertyResourceConfiguration<A> source;
	private final A proxy;

	private final Map<PropertyResourceConfiguration<?>, PropertyResourceBundle> bundleCache = new ConcurrentHashMap<>();
	private final Map<MethodSignature, PropertyValue<?>> valueCache = new ConcurrentHashMap<>();

	PropertiesDelegate(PropertyLoaderImpl loader, PropertyResourceConfiguration<A> source) {
		this.source = source;

		if (!source.getAccessor().isInterface()) {
			throw loader.log(Level.ERROR, new PropertyLoaderException(getText().mustBeInterface(source.getAccessor())));
		}
		this.loader = loader;

		proxy = createProxy(source.getAccessor());

		initialize(source.getAccessor());

		loader.locale().addWeakObserver(this, t -> l -> {
			t.fire(t.proxy);
		});
	}

	private PropertyLoaderProperties getText() {
		if (source.getAccessor().equals(PropertyLoaderProperties.class)) {
			return loader.getDefaultText();
		} else {
			return loader.getText();
		}
	}

	private void initialize(Class<A> accessor) {
		for (Method method : accessor.getMethods()) {
			MethodSignature signature = new MethodSignature(method);

			if (!LOCALIZATION_HELPER_METHODS.contains(signature) && !method.isDefault()) {
				PropertyConfiguration methodConfiguration = method.getAnnotation(PropertyConfiguration.class);

				Evaluation evaluate = source.getConfiguration().evaluation();
				if (methodConfiguration != null && methodConfiguration.evaluation() != Evaluation.UNSPECIFIED) {
					evaluate = methodConfiguration.evaluation();
				}

				if (evaluate == Evaluation.IMMEDIATE) {
					getPropertyValue(signature);
				}
			}
		}
	}

	private PropertyResourceBundle getBundle(PropertyResourceConfiguration<?> configuration) {
		return bundleCache.computeIfAbsent(configuration, c -> {
			PropertyResourceStrategy strategy = loader.getResourceStrategyInstance(source.getConfiguration().strategy());
			return strategy.getPropertyResourceBundle(source);
		});
	}

	private Object getInstantiatedValue(MethodSignature signature, Object... arguments) {
		List<?> argumentList;
		if (arguments == null) {
			argumentList = emptyList();
		} else {
			argumentList = asList(arguments);
		}

		try {
			PropertyValue<?> value = getPropertyValue(signature);

			return value.instantiate(argumentList);
		} catch (Exception e) {
			if (source.getAccessor().equals(PropertyLoaderProperties.class)) {
				try {
					return signature.method().invoke(new DefaultPropertyLoaderProperties(), arguments);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					throw new RuntimeException(e1);
				}
			} else {
				throw e;
			}
		}
	}

	private PropertyValue<?> getPropertyValue(MethodSignature signature) {
		return valueCache.computeIfAbsent(signature, s -> {
			PropertyResourceConfiguration<A> source;
			PropertyConfiguration configuration = signature.method().getAnnotation(PropertyConfiguration.class);
			if (configuration != null) {
				source = this.source.derive(configuration);
			} else {
				source = this.source;
			}

			String key = getKey(source, signature);
			AnnotatedType propertyType = signature.method().getAnnotatedReturnType();

			PropertyValueProvider<?> provider = loader.getProvider(propertyType);

			return loadValue(source, provider, key, Locale.ROOT);
		});
	}

	private String getKey(PropertyResourceConfiguration<A> source, MethodSignature signature) {
		String key = source.getConfiguration().key();
		if (key.equals(PropertyConfiguration.UNSPECIFIED_KEY)) {
			key = PropertyConfiguration.QUALIFIED_SCOPED;
		}

		Object[] substitution = new Object[3];
		substitution[0] = formatKeyComponent(source, source.getAccessor().getPackage().getName());
		substitution[1] = formatKeyComponent(source,
				PropertyLoader.removePropertiesPostfix(source.getAccessor().getSimpleName()));
		substitution[2] = formatKeyComponent(source, signature.method().getName());

		return String.format(key, substitution);
	}

	private Object formatKeyComponent(PropertyResourceConfiguration<A> source, String component) {
		String splitString = source.getConfiguration().keySplitString();
		if (!splitString.equals("") && !splitString.equals(UNSPECIFIED_KEY_SPLIT_STRING)) {
			component = new CamelCaseFormatter(splitString, false, UnformattingCase.PRESERVED).unformat(component);
		}

		KeyCase keyCase = source.getConfiguration().keyCase();
		if (keyCase == KeyCase.LOWER) {
			component = component.toLowerCase();
		} else if (keyCase == KeyCase.UPPER) {
			component = component.toUpperCase();
		}

		return component;
	}

	private <T> PropertyValue<T> loadValue(PropertyResourceConfiguration<A> source, PropertyValueProvider<T> provider,
			String key, Locale locale) {
		try {
			return provider.getParser().parse(getBundle(source).getValue(key, locale));
		} catch (MissingResourceException e) {
			if (source.getConfiguration().defaults() != Defaults.IGNORE) {
				Optional<PropertyValue<T>> defaultValue = provider.getDefault(key);
				if (defaultValue.isPresent()) {
					return defaultValue.get();
				}
			}
			PropertyLoaderException ple = new PropertyLoaderException(getText().translationNotFoundMessage(key), e);
			loader.log(Level.WARN, ple);
			throw ple;
		}
	}

	@SuppressWarnings("unchecked")
	private <U extends Properties<U>> U getPropertiesUnsafe(Class<?> returnType, PropertyConfiguration configuration) {
		return loader.getProperties((Class<U>) returnType, configuration);
	}

	private Class<?> getPropertyClass(String key, Type propertyType) {
		if (propertyType instanceof Class<?>) {
			return (Class<?>) propertyType;
		} else if (propertyType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) propertyType).getRawType();
		} else {
			throw new PropertyLoaderException(getText().illegalReturnType(propertyType, key));
		}
	}

	private Class<?> getElementClass(String key, Type propertyType) {
		Class<?> elementClass;

		if (propertyType instanceof ParameterizedType) {
			Type elementType = ((ParameterizedType) propertyType).getActualTypeArguments()[0];
			elementClass = getPropertyClass(key, elementType);
		} else {
			elementClass = Object.class;
		}

		return elementClass;
	}

	@Override
	public Locale getLocale() {
		return loader.getLocale();
	}

	@Override
	public A copy() {
		return proxy;
	}

	@SuppressWarnings("unchecked")
	A createProxy(Class<A> accessor) {
		ClassLoader classLoader = new DelegatingClassLoader(getClass().getClassLoader(), accessor.getClassLoader());

		return (A) Proxy.newProxyInstance(classLoader, new Class<?>[] { accessor },
				(Object p, Method method, Object[] args) -> {
					MethodSignature signature = new MethodSignature(method);

					if (LOCALIZATION_HELPER_METHODS.contains(signature)) {
						return method.invoke(PropertiesDelegate.this, args);
					}

					if (method.isDefault()) {
						return METHOD_HANDLE_CONSTRUCTOR.newInstance(method.getDeclaringClass(), MethodHandles.Lookup.PRIVATE)
								.unreflectSpecial(method, method.getDeclaringClass()).bindTo(p).invokeWithArguments(args);
					}

					return getInstantiatedValue(signature, args);
				});
	}
}
