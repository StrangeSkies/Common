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
package uk.co.strangeskies.mathematics.geometry.matrix.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.collection.ListTransformOnceView;
import uk.co.strangeskies.collection.SubList;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixH;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixNN;
import uk.co.strangeskies.mathematics.geometry.matrix.MatrixS;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH.Type;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.VectorHNImpl;
import uk.co.strangeskies.mathematics.values.Value;

public abstract class MatrixHImpl<S extends MatrixH<S, V>, V extends Value<V>>
		extends /*  */MatrixImpl<S, V> implements MatrixH<S, V> {
	public MatrixHImpl(int size, Order order, Supplier<V> valueFactory) {
		super(size + 1, size, order, valueFactory);

		for (int i = 0; i < getProjectedDimensions(); i++) {
			getElement(i, i).setValue(1);
		}
	}

	public MatrixHImpl(Order order, List<? extends List<? extends V>> values) {
		super(order, resizeColumnsImplementation(values, order));

		Matrix.assertIsSquare(this);
	}

	protected static <V extends Value<V>> List<List<V>> resizeColumnsImplementation(
			List<? extends List<? extends V>> data,
			Order order) {
		List<List<V>> newData = new ArrayList<>();
		List<V> newElements = null;

		if (order == Order.COLUMN_MAJOR) {
			for (List<? extends V> elements : data) {
				newElements = new ArrayList<>(elements);
				newData.add(newElements);

				V element = newElements.get(0).copy().setValue(0);
				newElements.add(element);
			}
		} else {
			for (List<? extends V> elements : data)
				newData.add(new ArrayList<>(elements));

			newElements = new ArrayList<>();
			for (V element : data.get(0))
				newElements.add(element.copy().setValue(0));

			newData.add(newElements);
		}

		newElements.get(newElements.size() - 1).setValue(0);

		return newData;
	}

	@Override
	public int getDimensions() {
		return getMinorSize();
	}

	@Override
	public int getProjectedDimensions() {
		return getDimensions() - 1;
	}

	@Override
	public MatrixNN<V> getMutableMatrix() {
		List<List<V>> dataView;

		if (getOrder() == Order.COLUMN_MAJOR) {
			dataView = new ListTransformOnceView<>(
					getData2(),
					l -> l.subList(0, getProjectedDimensions()));
		} else {
			dataView = new SubList<>(getData2(), 0, getProjectedDimensions());
		}

		return new MatrixNNImpl<>(getOrder(), dataView);
	}

	protected List<List<V>> getTransformationData2() {
		return getData2()
				.subList(0, getProjectedDimensions())
				.stream()
				.map(l -> l.subList(0, getProjectedDimensions()))
				.collect(Collectors.toList());
	}

	@Override
	public abstract MatrixS<?, V> getTransformationMatrix();

	@Override
	public S translate(Vector<?, ?> translation) {
		getColumnVector(getProjectedDimensions()).translate(translation);

		return getThis();
	}

	@Override
	public S getTranslated(Vector<?, ?> translation) {
		return copy().translate(translation);
	}

	@Override
	public S preTranslate(Vector<?, ?> translation) {
		// TODO implement pre-rotation
		return null;
	}

	@Override
	public S getPreTranslated(Vector<?, ?> translation) {
		return copy().preTranslate(translation);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<? extends VectorH<?, V>> getColumnVectors() {
		return (List<? extends VectorH<?, V>>) super.getColumnVectors();
	}

	@Override
	public VectorH<?, V> getColumnVector(int column) {
		return new VectorHNImpl<>(
				column == getDimensions() - 1 ? Type.Absolute : Type.Relative,
				getOrder(),
				Orientation.COLUMN,
				getColumnVectorData(column).subList(0, getProjectedDimensions()));
	}

	@Override
	public V getDeterminant() {
		return MatrixSImpl.getDeterminant(this);
	}
}
