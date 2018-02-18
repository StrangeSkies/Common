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
 * This file is part of uk.co.strangeskies.configadmin.persistence.
 *
 * uk.co.strangeskies.configadmin.persistence is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.configadmin.persistence is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.configadmin.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;

import org.apache.felix.cm.PersistenceManager;
import org.apache.felix.cm.file.FilePersistenceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Simple persistence manager implementation since the default doesn't support
 * configuration with the users home directory.
 * 
 * <p>
 * This service <em>must</em> be started <em>before</em> any configurable
 * components, and must be configured statically via the enRoute configurer
 * provider as it obviously can't bootstrap-configure its own file location.
 *
 * @author Elias N Vasylenko
 */
@Component(immediate = true, property = Constants.SERVICE_RANKING + ":Integer=" + 100)
public class PersistenceManagerImpl implements PersistenceManager {
	public static final String PERSISTENCE_CONFIGURATION = "uk.co.strangeskies.configadmin.persistence";
	public static final String DEFAULT_PERSISTENCE_CONFIGURATION = "~/.strangeskies/config";
	private static final String USER_HOME = "user.home";

	private FilePersistenceManager filePersistenceManager;

	@Activate
	void activate(BundleContext context) {
		String persistenceLocation = context.getProperty(PERSISTENCE_CONFIGURATION);

		if (persistenceLocation==null) {
			persistenceLocation = DEFAULT_PERSISTENCE_CONFIGURATION;
		}

		Path persistencePath = Paths.get(persistenceLocation);
		Path homeContraction = Paths.get("~");

		if (persistencePath.startsWith(homeContraction)) {
			persistencePath = Paths.get(System.getProperty(USER_HOME)).resolve(homeContraction.relativize(persistencePath));
		}

		filePersistenceManager = new FilePersistenceManager(persistencePath.toString());
	}

	@Override
	public void delete(String pid) throws IOException {
		filePersistenceManager.delete(pid);
	}

	@Override
	public boolean exists(String pid) {
		return filePersistenceManager.exists(pid);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getDictionaries() throws IOException {
		return filePersistenceManager.getDictionaries();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Dictionary load(String pid) throws IOException {
		return filePersistenceManager.load(pid);
	}

	@Override
	public void store(String pid, @SuppressWarnings("rawtypes") Dictionary props) throws IOException {
		filePersistenceManager.store(pid, props);
	}
}
