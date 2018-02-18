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

import static java.util.Optional.empty;

import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.text.parsing.Parser;

public interface PropertyValueProviderFactory {
	/**
	 * The method is generic over the return type to signify that the method is
	 * inherently unsafe, by forcing implementors to unsafe-cast the result.
	 * Implementors are responsible for ensuring the type T according to the
	 * returned object is a subtype of the given annotated type.
	 * 
	 * @param exactType
	 *          the type of the property provider
	 * @param loader
	 *          the property loader making the request
	 * @return a property provider over the given type
	 */
	<T> Optional<PropertyValueProvider<T>> getPropertyProvider(AnnotatedType exactType, PropertyLoader loader);

	static <T> PropertyValueProviderFactory over(Class<T> propertyClass, PropertyValueProvider<T> provider) {
		return new PropertyValueProviderFactory() {
			@SuppressWarnings("unchecked")
			@Override
			public <U> Optional<PropertyValueProvider<U>> getPropertyProvider(AnnotatedType exactType,
					PropertyLoader loader) {
				if (exactType.getType() instanceof Class<?> && exactType.getType().equals(propertyClass)) {
					return Optional.of((PropertyValueProvider<U>) provider);
				} else {
					return empty();
				}
			}
		};
	}

	static <T> PropertyValueProviderFactory over(Class<T> propertyClass, Function<List<?>, Parser<T>> getValue,
			BiFunction<String, List<?>, T> defaultValue) {
		return over(propertyClass, PropertyValueProvider.over(getValue, defaultValue));
	}

	static <T> PropertyValueProviderFactory over(Class<T> propertyClass, Function<List<?>, Parser<T>> getValue) {
		return over(propertyClass, PropertyValueProvider.over(getValue));
	}

	static <T> PropertyValueProviderFactory over(Class<T> propertyClass, Parser<T> getValue) {
		return over(propertyClass, PropertyValueProvider.over(getValue));
	}
}
