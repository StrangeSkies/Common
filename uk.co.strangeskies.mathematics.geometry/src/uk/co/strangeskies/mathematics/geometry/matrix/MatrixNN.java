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
package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.List;

import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorN;
import uk.co.strangeskies.mathematics.values.Value;

public interface MatrixNN<V extends Value<V>> extends Matrix<MatrixNN<V>, V> {
	@Override
	public MatrixNN<V> getTransposed();

	@Override
	public List<VectorN<V>> getRowVectors();

	@Override
	public List<VectorN<V>> getColumnVectors();

	@Override
	public VectorN<V> getRowVector(int row);

	@Override
	public VectorN<V> getColumnVector(int column);

	@Override
	public List<VectorN<V>> getMajorVectors();

	@Override
	public List<VectorN<V>> getMinorVectors();

	@Override
	public VectorN<V> getMajorVector(int index);

	@Override
	public VectorN<V> getMinorVector(int index);
}