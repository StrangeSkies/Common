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
package uk.co.strangeskies.osgi.servicewrapper;

import java.util.Map;

import org.osgi.framework.hooks.service.EventListenerHook;
import org.osgi.framework.hooks.service.FindHook;

/**
 * Adding and removing service wrappers from this manager should see their
 * effects applied to all services within the OSGi service registry this manager
 * is registered with.
 * 
 * @author Elias N Vasylenko
 */
public interface ServiceWrapperManager extends EventListenerHook, FindHook {
	/**
	 * Add a wrapper to the manager. Existing services may be wrapped at this
	 * point according to the configuration of the wrapper.
	 * 
	 * @param serviceWrapper
	 *          The {@link ServiceWrapper} instance to register.
	 * @param serviceProperties
	 *          The service properties of the service wrapper.
	 */
	public void addServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

	/**
	 * Unregister a service wrapper from the manager. The effects of the new
	 * properties will be propagated immediately to any wrapping services as
	 * appropriate.
	 * 
	 * @param serviceWrapper
	 *          The {@link ServiceWrapper} instance to update.
	 * @param serviceProperties
	 *          The updated service properties of the service wrapper.
	 */
	public void modifyServiceWrapper(ServiceWrapper<?> serviceWrapper,
			Map<String, Object> serviceProperties);

	/**
	 * Unregister a service wrapper from the manager. This will have the side
	 * effect of removing all instances of wrapped services from the service
	 * registry, which could mean services in use are removed.
	 * 
	 * @param serviceWrapper
	 *          The {@link ServiceWrapper} instance to unregister.
	 */
	public void removeServiceWrapper(ServiceWrapper<?> serviceWrapper);
}
