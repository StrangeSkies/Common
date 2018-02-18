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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.hooks.service.ListenerHook.ListenerInfo;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapper;
import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapper.HideServices;
import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapperManager;

/**
 * This implementation will automatically pick up and apply any implementations
 * of {@link ServiceWrapper} which are themselves registered as a service.
 * 
 * @author Elias N Vasylenko
 * 
 */
//@Component(service = { EventListenerHook.class, FindHook.class })
public class ServiceWrapperManagerImpl implements ServiceWrapperManager {
	private final MultiMap<Class<?>, ManagedServiceWrapper<?>, ? extends Set<ManagedServiceWrapper<?>>> wrappedServiceClasses;
	private final Map<ServiceWrapper<?>, ManagedServiceWrapper<?>> managedServiceWrappers;

	private final Map<ServiceReference<?>, WrappingServiceTree> wrappedServices;

	/**
	 * Default constructor.
	 */
	public ServiceWrapperManagerImpl() {
		wrappedServiceClasses = new MultiHashMap<>(HashSet::new);
		managedServiceWrappers = new HashMap<>();

		wrappedServices = new HashMap<>();
	}

	@Override
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties) {
		addManagedServiceWrapper(serviceWrapper, serviceProperties);
	}

	@Override
	public void removeServiceWrapper(ServiceWrapper<?> serviceWrapper) {
		removeManagedServiceWrapper(serviceWrapper);
	}

	@Override
	public void modifyServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties) {
		updateManagedServiceWrapper(serviceWrapper, serviceProperties);
	}

	private <T> void addManagedServiceWrapper(ServiceWrapper<T> serviceWrapper,
			Map<String, Object> serviceProperties) {

		ManagedServiceWrapper<T> managedServiceWrapper = new ManagedServiceWrapper<T>(
				serviceWrapper);
		wrappedServiceClasses.add(serviceWrapper.getServiceClass(),
				managedServiceWrapper);
		managedServiceWrappers.put(serviceWrapper, managedServiceWrapper);

		updateManagedServiceWrapper(serviceWrapper, serviceProperties);
	}

	private void removeManagedServiceWrapper(ServiceWrapper<?> serviceWrapper) {
		wrappedServiceClasses.removeValue(serviceWrapper.getServiceClass(),
				managedServiceWrappers.remove(serviceWrapper));
	}

	private void updateManagedServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties) {
		int serviceRanking;
		try {
			serviceRanking = (Integer) serviceProperties
					.get(Constants.SERVICE_RANKING);
		} catch (ClassCastException | NullPointerException e) {
			serviceRanking = 0;
		}

		HideServices hideServices;
		try {
			hideServices = HideServices.valueOf((String) serviceProperties
					.get(ServiceWrapper.HIDE_SERVICES));
		} catch (ClassCastException | NullPointerException e) {
			hideServices = HideServices.WHEN_WRAPPED;
		}

		managedServiceWrappers.get(serviceWrapper).update(serviceRanking,
				hideServices);
		// TODO update wrappedServices
	}

	@Override
	public void event(ServiceEvent event,
			Map<BundleContext, Collection<ListenerInfo>> listeners) {
		ServiceReference<?> serviceReference = event.getServiceReference();

		if (serviceReference.getProperty(ServiceWrapperManagerImpl.class.getName()) == null) {
			switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				registerWrappingServices(serviceReference);
				break;
			case ServiceEvent.UNREGISTERING:
				unregisterWrappingServices(serviceReference);
				break;
			case ServiceEvent.MODIFIED:
			case ServiceEvent.MODIFIED_ENDMATCH:
				updateWrappingServices(serviceReference);
				break;
			}
			if (wrappedServices.containsKey(serviceReference)) {
				listeners.clear();
			}
		}
	}

	private void registerWrappingServices(ServiceReference<?> serviceReference) {
		WrappingServiceTree wrappedServiceTree = new WrappingServiceTree(
				serviceReference, wrappedServiceClasses);
		wrappedServiceTree.register();

		wrappedServiceTree = wrappedServices.put(serviceReference,
				wrappedServiceTree);

		if (wrappedServiceTree != null)
			wrappedServiceTree.unregister();
	}

	private <T> void unregisterWrappingServices(
			ServiceReference<T> serviceReference) {
		WrappingServiceTree wrappedServiceTree = wrappedServices
				.remove(serviceReference);

		if (wrappedServiceTree != null)
			wrappedServiceTree.unregister();
	}

	private void updateWrappingServices(ServiceReference<?> serviceReference) {
		wrappedServices.get(serviceReference).updateRegistrations();
	}

	@Override
	public void find(BundleContext context, String name, String filter,
			boolean allServices, Collection<ServiceReference<?>> references) {
		Iterator<ServiceReference<?>> iterator = references.iterator();
		while (iterator.hasNext()) {
			if (wrappedServices.containsKey(iterator.next())) {
				iterator.remove();
			}
		}
	}
}
