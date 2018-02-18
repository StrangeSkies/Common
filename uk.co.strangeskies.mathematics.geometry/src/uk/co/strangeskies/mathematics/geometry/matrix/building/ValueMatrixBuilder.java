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
package uk.co.strangeskies.mathematics.geometry.matrix.building;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix2;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix3;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix4;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH2;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH3;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixHN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixNN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector4;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorHN;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.mathematics.values.Value;

public interface ValueMatrixBuilder<V extends Value<V>> {
	public interface HomogeneousValueMatrixBuilder<V extends Value<V>> {
		public HomogeneousValueMatrixBuilder<V> order(Order order);

		public HomogeneousValueMatrixBuilder<V> orientation(Orientation orientation);

		public VectorH2<V> vectorH2();

		public VectorH3<V> vectorH3();

		public VectorHN<V> vectorHN(int size);

		public MatrixH2<V> matrixH2();

		public MatrixH3<V> matrixH3();

		public MatrixHN<V> matrixHN(int size);
	}

	public ValueMatrixBuilder<V> order(Order order);

	public ValueMatrixBuilder<V> orientation(Orientation orientation);

	public HomogeneousValueMatrixBuilder<V> homogeneous(Type type);

	public Vector2<V> vector2();

	public Vector3<V> vector3();

	public Vector4<V> vector4();

	public VectorN<V> vectorN(int size);

	public Matrix2<V> matrix2();

	public Matrix3<V> matrix3();

	public Matrix4<V> matrix4();

	public MatrixN<V> matrixN(int size);

	public MatrixNN<V> matrixNN(int width, int height);
}
