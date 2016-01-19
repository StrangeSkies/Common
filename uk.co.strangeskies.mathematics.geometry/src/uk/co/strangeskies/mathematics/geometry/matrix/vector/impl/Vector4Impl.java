/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.geometry.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.factory.Factory;

public class Vector4Impl<V extends Value<V>> extends VectorImpl<Vector4<V>, V>
		implements Vector4<V> {
	public Vector4Impl(Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(4, order, orientation, valueFactory);
	}

	public Vector4Impl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);

		assertDimensions(this, 4);
	}

	@Override
	public Vector4<V> copy() {
		return new Vector4Impl<V>(getOrder(), getOrientation(), getData());
	}
}
