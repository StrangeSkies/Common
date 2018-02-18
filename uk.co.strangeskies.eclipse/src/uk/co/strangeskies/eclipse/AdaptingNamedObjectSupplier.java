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

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.hash;
import static uk.co.strangeskies.reflection.Types.getErasedType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.e4.core.internal.contexts.ContextObjectSupplier;
import org.eclipse.e4.core.internal.di.Requestor;
import org.eclipse.e4.core.services.adapter.Adapter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import uk.co.strangeskies.text.properties.PropertyLoader;

/**
 * Supplier for Eclipse DI contexts, to provide localization implementations of
 * a requested type via a {@link PropertyLoader}.
 *
 * @since 1.2
 */
@Component(
		service = ExtendedObjectSupplier.class,
		property = "dependency.injection.annotation:String=uk.co.strangeskies.eclipse.AdaptNamed",
		immediate = true)
public class AdaptingNamedObjectSupplier extends ExtendedObjectSupplier {
	class Request {
		private final String name;
		private final Class<?> adapterType;

		private final IRequestor requestor;
		private final IEclipseContext context;

		public Request(IObjectDescriptor descriptor, IRequestor requestor) {
			this.name = descriptor.getQualifier(AdaptNamed.class).value();
			this.adapterType = getErasedType(descriptor.getDesiredType());

			this.requestor = requestor;
			this.context = ((ContextObjectSupplier) ((Requestor<?>) requestor).getPrimarySupplier()).getContext();
		}

		public String getName() {
			return name;
		}

		public IEclipseContext getContext() {
			return context;
		}

		public Tracker getTracker() {
			return trackers.computeIfAbsent(this, Tracker::new);
		}

		public void disposeTracker() {
			Tracker tracker = trackers.remove(this);
			tracker.dispose();
		}

		public Object get() {
			return get(context);
		}

		private Object get(IEclipseContext context) {
			return context.get(Adapter.class).adapt(context.get(name), adapterType);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Request))
				return false;

			Request that = (Request) obj;

			return Objects.equals(this.name, that.name) && Objects.equals(this.adapterType, that.adapterType)
					&& Objects.equals(this.requestor, that.requestor);
		}

		@Override
		public int hashCode() {
			return hash(name, requestor);
		}

		@Override
		public String toString() {
			return name + " : " + adapterType + " @ " + requestor.getRequestingObject();
		}
	}

	class Tracker {
		private final Request request;

		private boolean disposed;
		private Object namedObject;

		public Tracker(Request request) {
			request.getContext().runAndTrack(new RunAndTrack() {
				@Override
				public boolean changed(IEclipseContext context) {
					if (disposed) {
						return false;
					}

					Object namedObject = request.get(context);
					if (!Objects.equals(Tracker.this.namedObject, namedObject)) {
						Tracker.this.namedObject = namedObject;

						// if this is not the first time ...
						if (Tracker.this.request != null) {
							request.requestor.resolveArguments(false);
							request.requestor.execute();
						}
					}

					return true;
				}
			});
			this.request = request;

			this.disposed = false;
			namedObject = null;
		}

		public void dispose() {
			disposed = true;
		}

		public Object get() {
			return namedObject;
		}
	}

	private final Map<Request, Tracker> trackers = synchronizedMap(new HashMap<>());

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		Request request = new Request(descriptor, requestor);

		if (!requestor.isValid()) {
			request.disposeTracker();
			return null;
		}

		if (track) {
			return request.getTracker().get();
		}

		return request.get();
	}

	@Deactivate
	public void dispose() {
		trackers.values().forEach(Tracker::dispose);
	}
}
