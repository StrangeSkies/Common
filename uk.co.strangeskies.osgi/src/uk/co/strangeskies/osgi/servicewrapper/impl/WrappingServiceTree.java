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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import uk.co.strangeskies.collection.multimap.MultiMap;

class WrappingServiceTree {
	private final ServiceReference<?> serviceReference;
	private final WrappingServiceTreeNode root;

	WrappingServiceTree(ServiceReference<?> serviceReference,
			MultiMap<Class<?>, ManagedServiceWrapper<?>, ? extends Set<ManagedServiceWrapper<?>>> wrappedServiceClasses) {
		this.serviceReference = serviceReference;
		root = new WrappingServiceTreeNode(serviceReference.getBundle().getBundleContext().getService(serviceReference),
				getProperties(serviceReference));

		Set<Class<?>> classes = getClasses(serviceReference);

		SortedSet<ManagedServiceWrapper<?>> orderedServiceWrappers = new TreeSet<>(new ManagedServiceWrapperComparator());
		orderedServiceWrappers.addAll(wrappedServiceClasses.getAll(classes));

		Set<WrappingServiceTreeNode> workingSet = new HashSet<>();
		workingSet.add(root);

		WrappingServiceTreeNode wrappingService;
		for (ManagedServiceWrapper<?> serviceWrapper : orderedServiceWrappers)
			for (WrappingServiceTreeNode service : new HashSet<>(workingSet))
				if (service.isVisible() && (wrappingService = service.wrap(serviceWrapper, classes)) != null)
					workingSet.add(wrappingService);
	}

	public void register() {
		BundleContext context = serviceReference.getBundle().getBundleContext();
		String[] classNames = getClassNames(serviceReference);

		root.register(context, classNames);
	}

	public void unregister() {
		root.unregister();
	}

	public void updateRegistrations() {
		// TODO Auto-generated method stub

	}

	private static Hashtable<String, Object> getProperties(ServiceReference<?> serviceReference) {
		Hashtable<String, Object> properties = new Hashtable<>();
		for (String propertyKey : serviceReference.getPropertyKeys()) {
			properties.put(propertyKey, serviceReference.getProperty(propertyKey));
		}
		properties.put(ServiceWrapperManagerImpl.class.getName(), true);

		return properties;
	}

	private static Set<Class<?>> getClasses(ServiceReference<?> serviceReference) {
		Set<Class<?>> serviceClasses = new HashSet<>();
		try {
			for (String className : getClassNames(serviceReference)) {
				serviceClasses.add(serviceReference.getBundle().loadClass(className));
			}
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return serviceClasses;
	}

	private static String[] getClassNames(ServiceReference<?> serviceReference) {
		return (String[]) serviceReference.getProperty(Constants.OBJECTCLASS);
	}
}
