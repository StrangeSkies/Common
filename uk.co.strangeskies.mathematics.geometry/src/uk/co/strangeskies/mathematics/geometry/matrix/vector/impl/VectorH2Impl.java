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
package uk.co.strangeskies.mathematics.geometry.matrix.vector.impl;

import java.util.List;
import java.util.function.Supplier;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.mathematics.values.Value;

public class VectorH2Impl<V extends Value<V>> extends VectorHImpl<VectorH2<V>, V>
		implements VectorH2<V> {
	public VectorH2Impl(Type type, Order order, Orientation orientation, Supplier<V> valueFactory) {
		super(type, 2, order, orientation, valueFactory);
	}

	public VectorH2Impl(Type type, Order order, Orientation orientation, List<? extends V> values) {
		super(type, order, orientation, values);

		assertDimensions(this, 3);
	}

	@Override
	public VectorH2<V> copy() {
		return new VectorH2Impl<>(getType(), getOrder(), getOrientation(), getData());
	}

	@Override
	public VectorH2<V> rotate(Value<?> angle) {
		getMutableVector().rotate(angle);

		return getThis();
	}

	@Override
	public VectorH2<V> rotate(Value<?> angle, Vector2<?> centre) {
		getMutableVector().rotate(angle, centre);

		return getThis();
	}

	@Override
	public Vector2<V> getMutableVector() {
		Vector2<V> mutableVector = new Vector2Impl<>(
				getOrder(),
				getOrientation(),
				getData().subList(0, 2));

		return mutableVector;
	}
}
