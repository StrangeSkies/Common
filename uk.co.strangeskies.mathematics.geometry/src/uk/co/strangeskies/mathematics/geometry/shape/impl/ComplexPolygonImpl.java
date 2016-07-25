/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import java.util.List;

import uk.co.strangeskies.mathematics.expression.CopyDecouplingExpression;
import uk.co.strangeskies.mathematics.expression.DependentExpression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ComplexPolygon;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

public abstract class ComplexPolygonImpl<S extends ComplexPolygonImpl<S, V>, V extends Value<V>>
		extends DependentExpression<S, S> implements ComplexPolygon<S, V>, CopyDecouplingExpression<S, S> {
	@Override
	public Value<?> getPerimeter() {
		List<Vector2<V>> vertices = boundary().vertices();

		double perimeter = 0;

		Vector2<V> previousVertex = null;
		for (Vector2<V> vertex : vertices) {
			if (previousVertex != null)
				perimeter += previousVertex.getSubtracted(vertex).getSize().doubleValue();

			previousVertex = vertex;
		}

		return new DoubleValue(perimeter);
	}

	@Override
	public final S get() {
		return getThis();
	}

	@Override
	protected final S evaluate() {
		return getThis();
	}

	@Override
	public boolean intersects(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touches(Shape<?> shape) {
		// TODO Auto-generated method stub
		return false;
	}
}
