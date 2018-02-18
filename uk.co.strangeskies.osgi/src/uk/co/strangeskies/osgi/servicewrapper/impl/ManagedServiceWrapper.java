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

import java.util.Map;

import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapper;
import uk.co.strangeskies.osgi.servicewrapper.ServiceWrapper.HideServices;

class ManagedServiceWrapper<T> {
	private final ServiceWrapper<T> serviceWrapper;

	private Integer serviceRanking;
	private HideServices hideServices;

	public ManagedServiceWrapper(ServiceWrapper<T> serviceWrapper) {
		this.serviceWrapper = serviceWrapper;
	}

	public Integer getServiceRanking() {
		return serviceRanking;
	}

	public HideServices getHideServices() {
		return hideServices;
	}

	public T wrapService(T service) {
		return serviceWrapper.wrapService(service);
	}

	public void unwrapService(T service) {
		serviceWrapper.unwrapService(service);
	}

	public boolean wrapServiceProperties(Map<String, Object> serviceProperties) {
		return serviceWrapper.wrapServiceProperties(serviceProperties);
	}

	public Class<T> getServiceClass() {
		return serviceWrapper.getServiceClass();
	}

	public void update(int serviceRanking, HideServices hideServices) {
		this.serviceRanking = serviceRanking;
		this.hideServices = hideServices;
	}
}
