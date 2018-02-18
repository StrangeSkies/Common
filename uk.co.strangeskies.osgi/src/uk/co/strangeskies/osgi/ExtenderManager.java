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
package uk.co.strangeskies.osgi;

import static uk.co.strangeskies.log.Log.forwardingLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import uk.co.strangeskies.log.Log;
import uk.co.strangeskies.log.Log.Level;

/**
 * An abstract class intended to facilitate implementation of OSGi extenders.
 * <p>
 * The extender capability relating to an implementation of
 * {@link ExtenderManager} should be the only one provided by the containing
 * bundle. The capability will typically be provided via the
 * {@link ProvideExtender} annotation on the implementing class.
 * 
 * @author Elias N Vasylenko
 */
public abstract class ExtenderManager implements BundleListener {
  /**
   * The OSGi capability namespace for an extender.
   */
  public static final String OSGI_EXTENDER = "osgi.extender";

  private BundleContext context;
  private BundleCapability capability;
  private Log log;
  private final Map<Bundle, Lock> added = new HashMap<>();

  @Activate
  protected void activate(ComponentContext cc) {
    this.context = cc.getBundleContext();
    context.addBundleListener(this);

    List<BundleCapability> extenderCapabilities = context
        .getBundle()
        .adapt(BundleWiring.class)
        .getCapabilities(OSGI_EXTENDER);

    if (extenderCapabilities.isEmpty()) {
      throw new IllegalStateException(
          "Cannot initiate extender, no capability is present on the implementing bundle");
    }
    if (extenderCapabilities.size() > 1) {
      throw new IllegalStateException(
          "Cannot initiate extender, capability on the implementing bundle is ambiguous between: "
              + extenderCapabilities);
    }

    capability = extenderCapabilities.get(0);

    for (Bundle bundle : context.getBundles()) {
      if ((bundle.getState() & (Bundle.STARTING | Bundle.ACTIVE)) != 0) {
        tryRegister(bundle);
      }
    }
  }

  protected Log getLog() {
    return forwardingLog(() -> log);
  }

  @Deactivate
  protected void deactivate(ComponentContext context) throws Exception {
    this.context.removeBundleListener(this);
  }

  @Override
  public void bundleChanged(BundleEvent event) {
    switch (event.getType()) {
    case BundleEvent.STARTED:
      tryRegister(event.getBundle());
      break;

    case BundleEvent.STOPPED:
      tryUnregister(event.getBundle());
      break;
    }
  }

  private void tryRegister(Bundle bundle) {
    Lock lock;
    synchronized (added) {
      if (added.containsKey(bundle)) {
        return;
      }
      lock = new ReentrantLock();
      lock.lock();
      added.put(bundle, lock);
    }

    try {
      boolean registerable = bundle
          .adapt(BundleWiring.class)
          .getRequirements(OSGI_EXTENDER)
          .stream()
          .anyMatch(r -> r.matches(capability));

      if (!registerable || !register(bundle)) {
        synchronized (added) {
          added.remove(bundle);
        }
      }
    } catch (Exception e) {
      synchronized (added) {
        added.remove(bundle);
      }
      getLog().log(
          Level.ERROR,
          "Cannot register bundle '" + bundle.getSymbolicName() + "' with extension manager '"
              + this + "'",
          e);
    } finally {
      lock.unlock();
    }
  }

  private void tryUnregister(Bundle bundle) {
    Lock lock;
    synchronized (added) {
      lock = added.get(bundle);
      if (lock == null) {
        return;
      }
    }
    lock.lock();
    try {
      boolean removed;
      synchronized (added) {
        removed = added.remove(bundle) != null;
      }
      if (removed) {
        unregister(bundle);
      }
    } finally {
      lock.unlock();
    }
  }

  @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
  protected void setLog(Log log) {
    this.log = log;
  }

  protected void unsetLog(Log log) {
    if (this.log == log)
      this.log = null;
  }

  protected abstract boolean register(Bundle bundle);

  protected abstract void unregister(Bundle bundle);
}
