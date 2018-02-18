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

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.mathematics.values.Value;

public class Matrix2Impl<V extends Value<V>> extends MatrixSImpl<Matrix2<V>, V>
		implements Matrix2<V> {
	public Matrix2Impl(Order order, Supplier<V> valueFactory) {
		super(2, order, valueFactory);
	}

	public Matrix2Impl(Order order, List<? extends List<? extends V>> values) {
		super(order, values);

		Matrix.assertDimensions(this, 2);
	}

	@Override
	public Matrix2<V> copy() {
		return new Matrix2Impl<>(getOrder(), getData2());
	}

	@Override
	public V getDeterminant() {
		// TODO YET ANOTHER oracle javac bug, shouldn't have to separate this out.
		V bd = getElement(0, 1).getMultiplied(getElement(1, 0));
		return getElement(0, 0).getMultiplied(getElement(1, 1)).add(bd);
	}

	@Override
	public Matrix2<V> rotate(Value<?> angle) {
		// TODO implement rotation
		return null;
	}

	@Override
	public Matrix2<V> preRotate(Value<?> value) {
		// TODO implement pre-rotation
		return null;
	}

	@Override
	public Matrix2<V> rotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement rotation about point
		return null;
	}

	@Override
	public Matrix2<V> preRotate(Value<?> angle, Vector2<?> centre) {
		// TODO implement pre-rotation about point
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getRowVectors() {
		return (List<Vector2<V>>) super.getRowVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getColumnVectors() {
		return (List<Vector2<V>>) super.getColumnVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector2Impl<V> getRowVector(int row) {
		return (Vector2Impl<V>) super.getRowVector(row);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Vector2Impl<V> getColumnVector(int column) {
		return (Vector2Impl<V>) super.getColumnVector(column);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getMajorVectors() {
		return (List<Vector2<V>>) super.getMajorVectors();
	}

	@SuppressWarnings("unchecked")
	@Override
	public final List<Vector2<V>> getMinorVectors() {
		return (List<Vector2<V>>) super.getMinorVectors();
	}

	@Override
	public final Vector2Impl<V> getMajorVector(int index) {
		return new Vector2Impl<>(
				getOrder(),
				getOrder().getAssociatedOrientation(),
				getData2().get(index));
	}

	@Override
	public final Vector2Impl<V> getMinorVector(int index) {
		List<V> minorElements = new ArrayList<>();
		for (List<V> elements : getData2()) {
			minorElements.add(elements.get(index));
		}
		return new Vector2Impl<>(
				getOrder(),
				getOrder().getOther().getAssociatedOrientation(),
				minorElements);
	}
}
