/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *      __   _______  ____           _       __     _      __       __
 *    ,`_ `,L__   __||  _ `.        / \     |  \   | |  ,-`__`]  ,-`__`]
 *   ( (_`-`   | |   | | ) |       / . \    | . \  | | / .`  `  / .`  `
 *    `._ `.   | |   | |<. L      / / \ \   | |\ \ | || |    _ | '--.
 *   _   `. \  | |   | |  `-`.   / /   \ \  | | \ \| || |   | || +--J
 *  \ \__.` /  | |   | |    \ \ / /     \ \ | |  \ ` | \ `._' | \ `.__,-
 *   `.__.-`   L_|   L_|    L_|/_/       \_\L_|   \__|  `-.__.'  `-.__.]
 *                   __    _         _      __      __
 *                 ,`_ `, | |   _   | |  ,-`__`]  ,`_ `,
 *                ( (_`-` | '-.) |  | | / .`  `  ( (_`-`
 *                 `._ `. | +-. <   | || '--.     `._ `.
 *                _   `. \| |  `-`. | || +--J    _   `. \
 *               \ \__.` /| |    \ \| | \ `.__,-\ \__.` /
 *                `.__.-` L_|    L_|L_|  `-.__.] `.__.-`
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

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Factory;

public class Vector2Impl<V extends Value<V>> extends VectorImpl<Vector2<V>, V>
		implements Vector2<V> {
	public Vector2Impl(Order order, Orientation orientation,
			Factory<V> valueFactory) {
		super(2, order, orientation, valueFactory);
	}

	public Vector2Impl(Order order, Orientation orientation,
			List<? extends V> values) {
		super(order, orientation, values);

		assertDimensions(this, 2);
	}

	@Override
	public final Vector2<V> copy() {
		return new Vector2Impl<V>(getOrder(), getOrientation(), getData());
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle) {
		// TODO implement rotation
		return null;
	}

	@Override
	public final Vector2<V> rotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement rotation about point
		return null;
	}
}
