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

import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.function.Consumer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * A simple service tracker implementation to help keep track of service
 * rankings by wrapping services with {@link RankedService}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the service to track
 */
public class RankedServiceTracker<T> extends ServiceTracker<T, RankedService<T>> {
	static class RankedServiceImpl<T> implements RankedService<T> {
		private final T serviceObject;
		private int ranking;

		public RankedServiceImpl(T serviceObject, int ranking) {
			this.serviceObject = serviceObject;
			this.ranking = ranking;
		}

		@Override
		public int hashCode() {
			return getServiceObject().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof RankedService<?>))
				return false;

			RankedService<?> that = (RankedService<?>) obj;

			return getServiceObject().equals(that.getServiceObject());
		}

		@Override
		public T getServiceObject() {
			return serviceObject;
		}

		@Override
		public int getRanking() {
			return ranking;
		}

		public boolean setRanking(int ranking) {
			if (this.ranking != ranking) {
				this.ranking = ranking;
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * @param context
	 *          the bundle context to listen over
	 * @param serviceClass
	 *          the class of service to track
	 * @param add
	 *          callback for services beginning their lifecycle
	 * @param remove
	 *          callback for services at the end of their lifecycle
	 */
	public RankedServiceTracker(BundleContext context, Class<T> serviceClass, Consumer<RankedService<T>> add,
			Consumer<RankedService<T>> remove) {
		this(context, serviceClass, add, t -> {}, remove);
	}

	/**
	 * @param context
	 *          the bundle context to listen over
	 * @param serviceClass
	 *          the class of service to track
	 * @param add
	 *          callback for services beginning their lifecycle
	 * @param modify
	 *          callback for services whose ranking has changed
	 * @param remove
	 *          callback for services at the end of their lifecycle
	 */
	public RankedServiceTracker(BundleContext context, Class<T> serviceClass, Consumer<RankedService<T>> add,
			Consumer<RankedService<T>> modify, Consumer<RankedService<T>> remove) {
		super(context, serviceClass, new ServiceTrackerCustomizer<T, RankedService<T>>() {
			@Override
			public RankedService<T> addingService(ServiceReference<T> reference) {
				T service = reference.getBundle().getBundleContext().getService(reference);
				RankedService<T> rankedService = new RankedServiceImpl<>(service, getRank(reference));

				add.accept(rankedService);

				return rankedService;
			}

			@Override
			public void modifiedService(ServiceReference<T> reference, RankedService<T> service) {
				if (((RankedServiceImpl<T>) service).setRanking(getRank(reference)))
					modify.accept(service);
			}

			@Override
			public void removedService(ServiceReference<T> reference, RankedService<T> service) {
				remove.accept(service);
			}

			private int getRank(ServiceReference<?> reference) {
				Object property = reference.getProperty(SERVICE_RANKING);
				return (property instanceof Integer) ? ((Integer) property).intValue() : 0;
			}
		});
	}
}
