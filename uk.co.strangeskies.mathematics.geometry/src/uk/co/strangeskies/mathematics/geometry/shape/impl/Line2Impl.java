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
package uk.co.strangeskies.mathematics.geometry.shape.impl;

import java.util.function.Supplier;

import uk.co.strangeskies.expression.CopyDecouplingExpression;
import uk.co.strangeskies.expression.DependentExpression;
import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.Matrix.Order;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.impl.Vector2Impl;
import uk.co.strangeskies.mathematics.geometry.shape.Line2;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;

public class Line2Impl<V extends Value<V>> extends DependentExpression<Line2<V>>
		implements Line2<V>, CopyDecouplingExpression<Line2<V>> {
	private final Vector2<V> a;
	private final Vector2<V> b;

	public Line2Impl(Supplier<V> valueFactory) {
		a = new Vector2Impl<>(Order.COLUMN_MAJOR, Orientation.COLUMN, valueFactory);
		b = new Vector2Impl<>(Order.COLUMN_MAJOR, Orientation.COLUMN, valueFactory);
	}

	public Line2Impl(Vector2<V> a, Vector2<V> b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public IntValue getArea() {
		return new IntValue();
	}

	@Override
	public DoubleValue getPerimeter() {
		return getLength().multiply(2);
	}

	@Override
	public DoubleValue getLength() {
		return a.getSubtracted(b).getSize();
	}

	@Override
	public V getLengthSquared() {
		return a.getSubtracted(b).getSizeSquared();
	}

	@Override
	public Bounds2<V> getBounds() {
		return new Bounds2<>(a, b);
	}

	@Override
	public Line2<V> copy() {
		return new Line2Impl<>(a, b);
	}

	@Override
	public boolean contains(Vector2<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(Shape<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Vector2<V> getA() {
		return a;
	}

	@Override
	public Vector2<V> getB() {
		return b;
	}

	@Override
	public Vector2<V> getAB() {
		return b.getSubtracted(a);
	}

	@Override
	public Vector2<V> getBA() {
		return a.getSubtracted(b);
	}

	@Override
	protected Line2<V> evaluate() {
		return getThis();
	}

	@Override
	public boolean touches(Vector<?, ?> point) {
		// TODO Auto-generated method stub
		return false;
	}
}
