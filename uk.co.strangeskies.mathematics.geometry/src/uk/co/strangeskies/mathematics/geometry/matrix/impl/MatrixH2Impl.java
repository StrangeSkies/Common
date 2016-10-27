/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix2;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector3Impl;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorH2Impl;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utilities.Factory;

public class MatrixH2Impl<V extends Value<V>> extends
		MatrixHImpl<MatrixH2<V>, V> implements MatrixH2<V> {
	public MatrixH2Impl(Order order, Factory<V> valueFactory) {
		super(2, order, valueFactory);
	}

	public MatrixH2Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);
	}

	@Override
	public Matrix2<V> getTransformationMatrix() {
		return new Matrix2Impl<V>(getOrder(), getTransformationData2());
	}

	@Override
	public MatrixH2<V> copy() {
		return new MatrixH2Impl<V>(getOrder(), getData2());
	}

	@Override
	public MatrixH2<V> rotate(Value<?> angle) {
		getTransformationMatrix().rotate(angle);

		return getThis();
	}

	@Override
	public MatrixH2<V> rotate(Value<?> angle, Vector2<?> centre) {
		getTransformationMatrix().rotate(angle, centre);

		return getThis();
	}

	@Override
	public MatrixH2<V> preRotate(Value<?> value) {
		getTransformationMatrix().preRotate(value);

		return getThis();
	}

	@Override
	public MatrixH2<V> preRotate(Value<?> angle, Vector2<?> centre) {
		getTransformationMatrix().preRotate(angle, centre);

		return getThis();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector3<V>> getRowVectors() {
		return (List<Vector3<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<VectorH2<V>> getColumnVectors() {
		return (List<VectorH2<V>>) super.getColumnVectors();
	}

	@Override
	public final Vector3<V> getRowVector(int row) {
		return new Vector3Impl<V>(getOrder(), Orientation.ROW,
				getRowVectorData(row));
	}

	@Override
	public final VectorH2<V> getColumnVector(int column) {
		return new VectorH2Impl<V>(column == getDimensions() - 1 ? Type.Absolute
				: Type.Relative, getOrder(), Orientation.COLUMN, getColumnVectorData(
				getProjectedDimensions()).subList(0, getProjectedDimensions()));
	}

	@Override
	public final Vector<?, V> getMajorVector(int index) {
		List<V> majorElements = getData2().get(index);

		if (getOrder() == Order.COLUMN_MAJOR) {
			majorElements = majorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorH2Impl<V>(newType, Order.COLUMN_MAJOR,
					Orientation.COLUMN, majorElements);
		} else {
			return new Vector3Impl<V>(Order.ROW_MAJOR, Orientation.ROW, getData2()
					.get(index));
		}
	}

	@Override
	public final Vector<?, V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<V>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}

		if (getOrder() == Order.ROW_MAJOR) {
			minorElements = minorElements.subList(0, getProjectedDimensions());

			Type newType;
			if (index == getProjectedDimensions()) {
				newType = Type.Absolute;
			} else {
				newType = Type.Relative;
			}

			return new VectorH2Impl<V>(newType, Order.ROW_MAJOR, Orientation.COLUMN,
					minorElements);
		} else {
			return new Vector3Impl<V>(Order.COLUMN_MAJOR, Orientation.ROW, getData2()
					.get(index));
		}
	}
}
