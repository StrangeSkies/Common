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
 * This file is part of uk.co.strangeskies.log.provider.
 *
 * uk.co.strangeskies.log.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.log.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.log.provider;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;

import uk.co.strangeskies.log.Log;

/**
 * {@link LogListener} implementation dumping all logs to console
 * 
 * @author Elias N Vasylenko
 */
@Component(scope = ServiceScope.PROTOTYPE)
@SuppressWarnings("javadoc")
public class LogProvider implements Log {
  private Bundle usingBundle;

  private ServiceReference<LogService> logServiceReference;
  private LogService logService;

  @Activate
  public void activate(ComponentContext context) {
    usingBundle = context.getUsingBundle();
    logServiceReference = usingBundle.getBundleContext().getServiceReference(LogService.class);
    logService = usingBundle.getBundleContext().getService(logServiceReference);
  }

  @Deactivate
  public void deactivate() {
    usingBundle.getBundleContext().ungetService(logServiceReference);
  }

  private int getLogServiceLevel(Level level) {
    switch (level) {
    case TRACE:
      return LogService.LOG_DEBUG;
    case DEBUG:
      return LogService.LOG_DEBUG;
    case INFO:
      return LogService.LOG_INFO;
    case WARN:
      return LogService.LOG_WARNING;
    case ERROR:
      return LogService.LOG_ERROR;
    default:
      throw new AssertionError();
    }
  }

  @Override
  public void log(Level level, String message) {
    logService.log(getLogServiceLevel(level), message);
  }

  @Override
  public void log(Level level, Throwable exception) {
    logService.log(getLogServiceLevel(level), exception.getMessage(), exception);
  }

  @Override
  public void log(Level level, String message, Throwable exception) {
    logService.log(getLogServiceLevel(level), message, exception);
  }
}
