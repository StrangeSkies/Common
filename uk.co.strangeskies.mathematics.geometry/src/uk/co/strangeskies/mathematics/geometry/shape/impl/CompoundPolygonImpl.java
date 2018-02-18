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

import java.util.List;
import java.util.Set;

import uk.co.strangeskies.expression.DependentExpression;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.CompoundPolygon;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.mathematics.values.ValueFactory;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 *
 * @author Elias N Vasylenko
 *
 * @param <V>
 *          The type of value for the coordinate system of the polygon
 */
public class CompoundPolygonImpl<V extends Value<V>> extends DependentExpression<CompoundPolygonImpl<V>>
		implements CompoundPolygon<CompoundPolygonImpl<V>, V> {
	public CompoundPolygonImpl(CompoundPolygon<?, ?> polygon, ValueFactory<? extends V> valueFactory) {}

	public CompoundPolygonImpl(CompoundPolygon<?, ? extends V> polygon) {}

	public CompoundPolygonImpl(List<? extends Vector2<?>> polygon, ValueFactory<? extends V> valueFactory) {}

	public CompoundPolygonImpl(List<? extends Vector2<V>> polygon) {}

	@Override
	public Value<?> getArea() {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public CompoundPolygonImpl<V> copy() {
		return new CompoundPolygonImpl<>(this);
	}

	@Override
	public uk.co.strangeskies.mathematics.geometry.shape.CompoundPolygon.WindingRule windingRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClosedPolyline2<V>> boundaryComponents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CompoundPolygonImpl<V> evaluate() {
		// TODO Auto-generated method stub
		return null;
	}
}
