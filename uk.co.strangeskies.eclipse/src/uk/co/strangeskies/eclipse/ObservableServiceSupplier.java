/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.di.suppliers.ExtendedObjectSupplier;
import org.eclipse.e4.core.di.suppliers.IObjectDescriptor;
import org.eclipse.e4.core.di.suppliers.IRequestor;
import org.eclipse.fx.core.di.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import uk.co.strangeskies.fx.FXUtilities;
import uk.co.strangeskies.osgi.ServiceWiringException;
import uk.co.strangeskies.text.properties.PropertyLoader;

/**
 * Supplier for {@link Service}
 *
 * @since 1.2
 */
@Component(service = ExtendedObjectSupplier.class, property = "dependency.injection.annotation:String=uk.co.strangeskies.eclipse.ObservableService")
public class ObservableServiceSupplier extends ExtendedObjectSupplier {
	private class ServiceUpdateListener<T> implements ServiceListener {
		private final BundleContext context;
		private final ObservableList<ServiceReference<T>> references;
		private final Class<T> elementType;

		@SuppressWarnings("unchecked")
		public ServiceUpdateListener(BundleContext context, Type elementType) throws InvalidSyntaxException {
			this.context = context;
			this.references = FXCollections.observableArrayList();
			this.elementType = elementType instanceof ParameterizedType
					? (Class<T>) ((ParameterizedType) elementType).getRawType() : (Class<T>) elementType;

			synchronized (this) {
				context.addServiceListener(this, "(" + Constants.OBJECTCLASS + "=" + this.elementType.getName() + ")");

				refreshServices();
			}
		}

		@Override
		public void serviceChanged(ServiceEvent event) {
			refreshServices();
		}

		private synchronized void refreshServices() {
			try {
				List<ServiceReference<T>> newReferences = new ArrayList<>(context.getServiceReferences(elementType, null));
				Collections.sort(newReferences);

				references.retainAll(newReferences);

				int index = 0;
				for (ServiceReference<T> newReference : newReferences) {
					if (!references.contains(newReference)) {
						references.add(index, newReference);
					}
					index++;
				}
			} catch (InvalidSyntaxException e) {
				throw new AssertionError();
			}
		}

		public ObservableList<T> getServiceList() {
			return FXUtilities.map(references, context::getService);
		}

		public ObservableSet<T> getServiceSet() {
			return FXUtilities.asSet(getServiceList());
		}

		public ObservableValue<T> getServiceValue() {
			SimpleObjectProperty<T> value = new SimpleObjectProperty<>();

			references.addListener((ListChangeListener<ServiceReference<T>>) c -> {
				value.set(context.getService(references.get(0)));
			});

			return value;
		}
	}

	@Reference
	PropertyLoader generalLocalizer;
	private ObservableServiceSupplierText text;

	@Activate
	void activate() {
		text = generalLocalizer.getProperties(ObservableServiceSupplierText.class);
	}

	@Override
	public Object get(IObjectDescriptor descriptor, IRequestor requestor, boolean track, boolean group) {
		try {
			Type collectionType = descriptor.getDesiredType();
			Bundle bundle = FrameworkUtil.getBundle(requestor.getRequestingObjectClass());

			if (collectionType instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) collectionType;

				ServiceUpdateListener<?> listener = new ServiceUpdateListener<>(bundle.getBundleContext(),
						parameterizedType.getActualTypeArguments()[0]);

				if (parameterizedType.getRawType() == ObservableList.class) {
					return listener.getServiceList();
				}
				if (parameterizedType.getRawType() == ObservableSet.class) {
					return listener.getServiceSet();
				}
				if (parameterizedType.getRawType() == ObservableValue.class) {
					return listener.getServiceValue();
				}
			}

			throw new ServiceWiringException(text.illegalInjectionTarget());
		} catch (ServiceWiringException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceWiringException(text.unexpectedError(), e);
		}
	}
}
