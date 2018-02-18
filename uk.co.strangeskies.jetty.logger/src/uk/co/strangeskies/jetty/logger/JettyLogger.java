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
 * This file is part of uk.co.strangeskies.jetty.logger.
 *
 * uk.co.strangeskies.jetty.logger is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.jetty.logger is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.jetty.logger;

import org.apache.felix.http.base.internal.logger.SystemLogger;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * The Jetty log is instantiated reflectively from a static context, making it
 * difficult to set the logger early enough to stop rubbish being spat out to
 * the console. This implementation is added as a fragment, configured through
 * the jetty-logging.properties resource in the bundle, and sets up a service
 * tracker to wait for a proper OSGi log implementation to forward to.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("restriction")
public class JettyLogger extends AbstractLogger {
	private static ServiceTracker<LogService, LogService> SERVICE_TRACKER;
	private static LogService LOG;

	private final String name;
	boolean enabled = true;

	public JettyLogger() {
		name = getClass().getName();
		init();
	}

	public JettyLogger(String name) {
		this.name = name;
		init();
	}

	private synchronized void init() {
		try {
			if (SERVICE_TRACKER == null) {
				BundleContext context = FrameworkUtil.getBundle(AbstractLogger.class).getBundleContext();

				SERVICE_TRACKER = new ServiceTracker<>(
						context,
						LogService.class,
						new ServiceTrackerCustomizer<LogService, LogService>() {
							@Override
							public LogService addingService(ServiceReference<LogService> reference) {
								updateLogService();
								return context.getService(reference);
							}

							@Override
							public void modifiedService(ServiceReference<LogService> reference, LogService service) {
								updateLogService();
							}

							@Override
							public void removedService(ServiceReference<LogService> reference, LogService service) {
								updateLogService();
							}
						});
				SERVICE_TRACKER.open();
				updateLogService();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private static void updateLogService() {
		LOG = SERVICE_TRACKER.getService();
		/*
		 * This is similar to what is done internally, but actually tracks service
		 * ranking...
		 */
		SystemLogger.setLogService(LOG);
	}

	@Override
	protected Logger newLogger(String p0) {
		return new JettyLogger(p0);
	}

	private boolean ready() {
		return enabled && LOG != null;
	}

	public void log(int level, String message) {
		if (ready())
			LOG.log(level, message);
	}

	public void log(int level, String message, Throwable exception) {
		if (ready())
			LOG.log(level, message, exception);
	}

	public void log(int level, Throwable exception) {
		if (ready())
			LOG.log(level, exception.getMessage(), exception);
	}

	@Override
	public void debug(Throwable arg0) {
		log(LogService.LOG_DEBUG, arg0);
	}

	@Override
	public void debug(String arg0, Object... arg1) {
		log(LogService.LOG_DEBUG, String.format(arg0, arg1));
	}

	@Override
	public void debug(String arg0, long arg1) {
		debug(arg0, new Object[] { arg1 });
	}

	@Override
	public void debug(String arg0, Throwable arg1) {
		log(LogService.LOG_DEBUG, arg0, arg1);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void ignore(Throwable arg0) {}

	@Override
	public void info(Throwable arg0) {
		log(LogService.LOG_INFO, arg0);
	}

	@Override
	public void info(String arg0, Object... arg1) {
		log(LogService.LOG_INFO, String.format(arg0, arg1));
	}

	@Override
	public void info(String arg0, Throwable arg1) {
		log(LogService.LOG_INFO, arg0, arg1);
	}

	@Override
	public boolean isDebugEnabled() {
		return enabled;
	}

	@Override
	public void setDebugEnabled(boolean arg0) {
		enabled = arg0;
	}

	@Override
	public void warn(Throwable arg0) {
		log(LogService.LOG_WARNING, arg0);
	}

	@Override
	public void warn(String arg0, Object... arg1) {
		log(LogService.LOG_WARNING, String.format(arg0, arg1));
	}

	@Override
	public void warn(String arg0, Throwable arg1) {
		log(LogService.LOG_WARNING, arg0, arg1);
	}
}
