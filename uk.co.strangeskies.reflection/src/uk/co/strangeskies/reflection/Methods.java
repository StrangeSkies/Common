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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Supplier;

import uk.co.strangeskies.property.IdentityProperty;

/**
 * A collection of general utility methods relating to methods in the Java type
 * system.
 * 
 * @author Elias N Vasylenko
 */
public final class Methods {
	private Methods() {}

	/**
	 * Find a method on an interface type without needing to look it up by its
	 * string name. This method will determine the last method which would be
	 * invoked on an instance of the given class by the given lambda.
	 * 
	 * <p>
	 * For example to get a {@link Method} instance over the
	 * {@link Comparable#compareTo(Object)} method, invoke as
	 * {@code Methods.findMethod(Comparable.class, c -> c.compareTo(null));}. Or
	 * to get a reference to {@link Supplier#get()}, invoke as
	 * {@code Methods.findMethod(Supplier.class, Supplier::get);}.
	 * 
	 * @param <N>
	 *          the interface containing the method
	 * @param type
	 *          the type of the class which declares the method
	 * @param methodLambda
	 *          a consumer, typically given as a lambda or method reference, which
	 *          invokes the requested method
	 * @return the last method which would be invoked by the given
	 *         {@link Consumer} on an instance of the given type
	 */
	public static <N> Method findMethod(Class<N> type, Consumer<? super N> methodLambda) {
		IdentityProperty<Method> lastCalled = new IdentityProperty<>();

		@SuppressWarnings("unchecked")
		N instance = (N) Proxy
				.newProxyInstance(type.getClassLoader(), new Class[] { type }, (proxy, method, args) -> {
					lastCalled.set(method);
					return null;
				});

		methodLambda.accept(instance);

		if (lastCalled.get() == null) {
			throw new ReflectionException(REFLECTION_PROPERTIES.cannotFindMethodOn(type));
		}

		return lastCalled.get();
	}

	/**
	 * Determine the visibility of the method
	 * 
	 * @param method
	 *          the method whose visibility we wish to determine
	 * @return a {@link Visibility} object describing the method
	 */
	public static Visibility getVisibility(Method method) {
		int modifiers = method.getModifiers();

		if (Modifier.isPrivate(modifiers)) {
			return Visibility.PRIVATE;
		} else if (Modifier.isProtected(modifiers)) {
			return Visibility.PROTECTED;
		} else if (Modifier.isPublic(modifiers)) {
			return Visibility.PUBLIC;
		} else {
			return Visibility.PACKAGE_PRIVATE;
		}
	}
}
