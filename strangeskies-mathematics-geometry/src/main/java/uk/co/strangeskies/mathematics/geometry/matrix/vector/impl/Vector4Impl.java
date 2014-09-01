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
