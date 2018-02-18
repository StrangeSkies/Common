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
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix.building.impl;

import java.util.function.Supplier;

import org.osgi.service.component.annotations.Component;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.building.MatrixBuilder;
import uk.co.strangeskies.mathematics.geometry.matrix.building.ValueMatrixBuilder;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.FloatValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.LongValue;
import uk.co.strangeskies.mathematics.values.Value;

@Component
public class MatrixBuilderImpl implements MatrixBuilder {
	private Order defaultOrder = Order.COLUMN_MAJOR;
	private Orientation defaultOrientation = Orientation.COLUMN;

	@Override
	public ValueMatrixBuilder<IntValue> ints() {
		return values(IntValue::new);
	}

	@Override
	public ValueMatrixBuilder<LongValue> longs() {
		return values(LongValue::new);
	}

	@Override
	public ValueMatrixBuilder<FloatValue> floats() {
		return values(FloatValue::new);
	}

	@Override
	public ValueMatrixBuilder<DoubleValue> doubles() {
		return values(DoubleValue::new);
	}

	@Override
	public <V extends Value<V>> ValueMatrixBuilder<V> values(Supplier<V> valueFactory) {
		return new ValueMatrixBuilderImpl<>(valueFactory).order(defaultOrder).orientation(
				defaultOrientation);
	}

	@Override
	public Order getDefaultOrder() {
		return defaultOrder;
	}

	@Override
	public void setDefaultOrder(Order defaultOrder) {
		this.defaultOrder = defaultOrder;
	}

	@Override
	public Orientation getDefaultOrientation() {
		return defaultOrientation;
	}

	@Override
	public void setDefaultOrientation(Orientation defaultOrientation) {
		this.defaultOrientation = defaultOrientation;
	}
}
