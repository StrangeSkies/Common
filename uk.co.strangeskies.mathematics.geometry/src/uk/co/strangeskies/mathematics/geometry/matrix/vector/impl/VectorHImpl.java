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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import uk.co.strangeskies.mathematics.geometry.matrix.Matrix;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.VectorH;
import uk.co.strangeskies.mathematics.values.Value;

public abstract class VectorHImpl<S extends VectorH<S, V>, V extends Value<V>>
		extends /*  */VectorImpl<S, V> implements VectorH<S, V> {
	private Type type;

	public VectorHImpl(
			Type type,
			int size,
			Order order,
			Orientation orientation,
			Supplier<V> valueFactory) {
		super(size + 1, order, orientation, valueFactory);

		getElement(size).setValue(type == Type.Relative ? 0 : 1);

		this.type = type;
	}

	public VectorHImpl(Type type, Order order, Orientation orientation, List<? extends V> values) {
		super(order, orientation, resizeColumnImplementation(type, values));

		this.type = type;
	}

	protected static <V extends Value<V>> List<V> resizeColumnImplementation(
			Type type,
			List<? extends V> data) {
		List<V> newData = new ArrayList<>(data);

		newData.add(data.get(0).copy().setValue(type == Type.Relative ? 0 : 1));

		return newData;
	}

	@Override
	public final Type getType() {
		return type;
	}

	@Override
	public final void setType(Type type) {
		this.type = type;
	}

	@Override
	public final int getProjectedDimensions() {
		return super.getDimensions() - 1;
	}

	@Override
	public final V getElement(int major, int minor) {
		return super.getElement(major, minor);
	}

	@Override
	public final S add(Matrix<?, ?> other) {
		if (!other.getData().get(getProjectedDimensions()).equals(0)) {
			throw new IllegalArgumentException();
		}

		return super.add(other);
	}

	@Override
	public final S subtract(Matrix<?, ?> other) {
		if (!other.getData().get(getProjectedDimensions()).equals(0)) {
			throw new IllegalArgumentException();
		}

		return super.subtract(other);
	}

	@Override
	public final S multiply(double scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(float scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(long scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(int scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S multiply(Value<?> scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return super.multiply(scalar);
	}

	@Override
	public final S divide(double scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(float scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public S divide(long scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(int scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S divide(Value<?> scalar) {
		if (getType() == Type.Absolute) {
			throw new IllegalStateException();
		}

		return divide(scalar);
	}

	@Override
	public final S multiply(Matrix<?, ?> other) {
		List<List<Value<V>>> multiplied = Matrix.multiplyData(getData2(), other.getData2());

		List<Value<V>> lastElements = multiplied.get(multiplied.size() - 1);
		Value<V> lastElement = lastElements.get(lastElements.size() - 1);
		if (!((lastElement.equals(0) && getType() == Type.Relative)
				|| (lastElement.equals(1) && getType() == Type.Absolute)))
			throw new IllegalArgumentException();

		return setData2(multiplied);
	}

	@Override
	public final S preMultiply(Matrix<?, ?> other) {
		List<List<Value<V>>> multiplied = Matrix.preMultiplyData(getData2(), other.getData2());

		List<Value<V>> lastElements = multiplied.get(multiplied.size() - 1);
		Value<V> lastElement = lastElements.get(lastElements.size() - 1);
		if (!((lastElement.equals(0) && getType() == Type.Relative)
				|| (lastElement.equals(1) && getType() == Type.Absolute)))
			throw new IllegalArgumentException();

		return setData2(multiplied);
	}

	@Override
	public final S translate(Vector<?, ?> translation) {
		if (translation.getDimensions() == getProjectedDimensions()) {
			getMutableVector().translate(translation);
		} else if (translation.getDimensions() == getDimensions()) {
			add(translation);
		}
		return getThis();
	}
}
