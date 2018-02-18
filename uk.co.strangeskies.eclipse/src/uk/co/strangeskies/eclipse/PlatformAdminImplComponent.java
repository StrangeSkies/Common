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
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import java.lang.reflect.Method;

import org.eclipse.osgi.compatibility.state.PlatformAdminImpl;
import org.eclipse.osgi.service.resolver.DisabledInfo;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.service.resolver.StateHelper;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Wrapper for {@link PlatformAdminImpl}, which is not properly exported.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("restriction")
@Component(service = PlatformAdmin.class)
public class PlatformAdminImplComponent implements PlatformAdmin {
	PlatformAdminImpl platformAdmin = new PlatformAdminImpl();

	/**
	 * Activate the {@link PlatformAdmin} instance.
	 * 
	 * @param context
	 *          {@link BundleContext} instance from the framework
	 * @throws Exception
	 *           Exception from reflective {@code start(BundleContext)} invocation
	 */
	@Activate
	public void activate(BundleContext context) throws Exception {
		Method start = platformAdmin.getClass().getDeclaredMethod("start", BundleContext.class);
		start.setAccessible(true);
		start.invoke(platformAdmin, context);
	}

	/**
	 * Deactivate the {@link PlatformAdmin} instance.
	 * 
	 * @param context
	 *          {@link BundleContext} instance from the framework
	 * @throws Exception
	 *           Exception from reflective {@code start(BundleContext)} invocation
	 */
	@Deactivate
	public void deactivate(BundleContext context) throws Exception {
		Method stop = platformAdmin.getClass().getDeclaredMethod("stop", BundleContext.class);
		stop.setAccessible(true);
		stop.invoke(platformAdmin, context);
	}

	@Override
	public void addDisabledInfo(DisabledInfo disabledInfo) {
		platformAdmin.addDisabledInfo(disabledInfo);
	}

	@Override
	public void commit(State state) throws BundleException {
		platformAdmin.commit(state);
	}

	@Override
	public Resolver createResolver() {
		return platformAdmin.createResolver();
	}

	@Override
	public StateObjectFactory getFactory() {
		return platformAdmin.getFactory();
	}

	@Override
	@Deprecated
	public Resolver getResolver() {
		return platformAdmin.getResolver();
	}

	@Override
	public State getState() {
		return platformAdmin.getState();
	}

	@Override
	public State getState(boolean mutable) {
		return platformAdmin.getState(mutable);
	}

	@Override
	public StateHelper getStateHelper() {
		return platformAdmin.getStateHelper();
	}

	@Override
	public void removeDisabledInfo(DisabledInfo disabledInfo) {
		platformAdmin.removeDisabledInfo(disabledInfo);
	}
}
