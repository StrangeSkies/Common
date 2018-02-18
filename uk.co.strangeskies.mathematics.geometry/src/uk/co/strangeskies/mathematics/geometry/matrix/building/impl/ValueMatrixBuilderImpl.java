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

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix2;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix3;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix4;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH2;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH3;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixHN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixNN;
import uk.co.strangeskies.mathematics.geometry.matrix.building.ValueMatrixBuilder;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.Matrix2Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.Matrix3Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.Matrix4Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixH2Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixH3Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixHNImpl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixNImpl;
import uk.co.strangeskies.mathematics.geometry.matrix.impl.MatrixNNImpl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorHN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector4Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorH2Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorH3Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorHNImpl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorNImpl;
import uk.co.strangeskies.mathematics.values.Value;

public class ValueMatrixBuilderImpl<V extends Value<V>> implements ValueMatrixBuilder<V> {
	protected class HomogeneousValueMatrixBuilderImpl implements HomogeneousValueMatrixBuilder<V> {
		private final Type type;

		public HomogeneousValueMatrixBuilderImpl(Type type) {
			this.type = type;
		}

		@Override
		public final HomogeneousValueMatrixBuilder<V> order(Order order) {
			ValueMatrixBuilderImpl.this.order(order);
			return this;
		}

		@Override
		public final HomogeneousValueMatrixBuilder<V> orientation(Orientation orientation) {
			ValueMatrixBuilderImpl.this.orientation(orientation);
			return this;
		}

		@Override
		public VectorH2<V> vectorH2() {
			return new VectorH2Impl<>(type, order, orientation, getValueFactory());
		}

		@Override
		public VectorH3<V> vectorH3() {
			return new VectorH3Impl<>(type, order, orientation, getValueFactory());
		}

		@Override
		public VectorHN<V> vectorHN(int size) {
			return new VectorHNImpl<>(type, size, order, orientation, getValueFactory());
		}

		@Override
		public MatrixH2<V> matrixH2() {
			return new MatrixH2Impl<>(order, getValueFactory());
		}

		@Override
		public MatrixH3<V> matrixH3() {
			return new MatrixH3Impl<>(order, getValueFactory());
		}

		@Override
		public MatrixHN<V> matrixHN(int size) {
			return new MatrixHNImpl<>(size, order, getValueFactory());
		}
	}

	private final Supplier<V> valueFactory;
	private Order order;
	private Orientation orientation;

	public ValueMatrixBuilderImpl(Supplier<V> valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public final ValueMatrixBuilder<V> order(Order order) {
		this.order = order;
		return this;
	}

	@Override
	public final ValueMatrixBuilder<V> orientation(Orientation orientation) {
		this.orientation = orientation;
		return this;
	}

	protected final Supplier<V> getValueFactory() {
		return valueFactory;
	}

	@Override
	public final Vector2<V> vector2() {
		return new Vector2Impl<>(order, orientation, getValueFactory());
	}

	@Override
	public final Vector3<V> vector3() {
		return new Vector3Impl<>(order, orientation, getValueFactory());
	}

	@Override
	public final Vector4<V> vector4() {
		return new Vector4Impl<>(order, orientation, getValueFactory());
	}

	@Override
	public VectorN<V> vectorN(int size) {
		return new VectorNImpl<>(size, order, orientation, getValueFactory());
	}

	@Override
	public Matrix2<V> matrix2() {
		return new Matrix2Impl<>(order, getValueFactory());
	}

	@Override
	public Matrix3<V> matrix3() {
		return new Matrix3Impl<>(order, getValueFactory());
	}

	@Override
	public Matrix4<V> matrix4() {
		return new Matrix4Impl<>(order, getValueFactory());
	}

	@Override
	public MatrixN<V> matrixN(int size) {
		return new MatrixNImpl<>(size, order, getValueFactory());
	}

	@Override
	public MatrixNN<V> matrixNN(int width, int height) {
		return new MatrixNNImpl<>(width, height, order, getValueFactory());
	}

	@Override
	public HomogeneousValueMatrixBuilder<V> homogeneous(Type type) {
		return new HomogeneousValueMatrixBuilderImpl(type);
	}
}
