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
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.servicewrapper.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapper.HideServices;

class WrappingServiceTreeNode {
	private final Object service;
	private final Hashtable<String, Object> properties;

	private final Set<WrappingServiceTreeNode> wrappingServices;

	private boolean visible;

	private ServiceRegistration<?> registration;

	public WrappingServiceTreeNode(Object service,
			Hashtable<String, Object> properties) {
		this.service = service;
		this.properties = properties;

		wrappingServices = new HashSet<>();

		visible = false;
	}

	public Object getService() {
		return service;
	}

	public Hashtable<String, Object> getProperties() {
		return properties;
	}

	public Set<WrappingServiceTreeNode> getWrappingServices() {
		return wrappingServices;
	}

	public boolean isVisible() {
		return visible;
	}

	public WrappingServiceTreeNode wrap(ManagedServiceWrapper<?> serviceWrapper,
			Set<Class<?>> classes) {
		if (!classes.contains(serviceWrapper.getServiceClass()))
			throw new IllegalArgumentException();

		Hashtable<String, Object> wrappingProperties = getProperties();
		if (serviceWrapper.wrapServiceProperties(wrappingProperties)) {
			Object wrappingService = wrapService(getService(), serviceWrapper,
					classes);

			if (serviceWrapper.getHideServices() == HideServices.WHEN_WRAPPED)
				visible = false;

			WrappingServiceTreeNode compoundWrappedService = new WrappingServiceTreeNode(
					wrappingService, wrappingProperties);
			wrappingServices.add(compoundWrappedService);
			return compoundWrappedService;
		}

		if (serviceWrapper.getHideServices() == HideServices.ALWAYS)
			visible = false;

		return null;
	}

	public <T> Object wrapService(final Object service,
			ManagedServiceWrapper<T> serviceWrapper, Set<Class<?>> classes) {
		@SuppressWarnings("unchecked")
		final T wrappingService = serviceWrapper.wrapService((T) service);
		final Class<T> wrapperClass = serviceWrapper.getServiceClass();

		List<Class<?>> orderedServiceClasses = new ArrayList<>(classes);
		orderedServiceClasses.remove(wrapperClass);
		orderedServiceClasses.add(0, wrapperClass);

		return Proxy.newProxyInstance(null,
				orderedServiceClasses.toArray(new Class<?>[0]),
				new InvocationHandler() {
					@Override
					public Object invoke(Object object, Method method, Object[] args)
							throws Throwable {
						Class<?> declaringClass = method.getDeclaringClass();
						/*
						 * TODO this ^ isn't good enough. Need to be able to change
						 * preference to 'wrapperClass' when more than one interface
						 * contains a method.
						 * 
						 * WILL REPLACE WITH commons-proxy
						 */

						if (declaringClass == Object.class
								|| declaringClass == wrapperClass) {
							return method.invoke(wrappingService, args);
						} else {
							return method.invoke(service, args);
						}
					}
				});
	}

	public void register(BundleContext context, String[] classNames) {
		if (isVisible())
			registration = context.registerService(classNames, getService(),
					getProperties());

		for (WrappingServiceTreeNode wrappingService : getWrappingServices())
			wrappingService.register(context, classNames);
	}

	public void unregister() {
		if (registration != null) {
			registration.unregister();
			registration = null;
		}

		for (WrappingServiceTreeNode wrappingService : getWrappingServices())
			wrappingService.unregister();
	}
}
