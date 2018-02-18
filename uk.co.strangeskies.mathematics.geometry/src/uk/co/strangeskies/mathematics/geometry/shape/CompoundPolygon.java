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
package uk.co.strangeskies.mathematics.geometry.shape;

import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.logic.BooleanCombinationBehaviour;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

/**
 * Complex polygons, self intersecting with holes and multiple parts.
 *
 * @author Elias N Vasylenko
 *
 * @param <V>
 *            The type of value for the coordinate system of the polygon
 */
public interface CompoundPolygon<S extends CompoundPolygon<S, V>, V extends Value<V>> extends Shape<S>,
		/*  */
		BooleanCombinationBehaviour<CompoundPolygon<?, V>, CompoundPolygon<?, V>> {
	enum WindingRule {
		EVEN_ODD, NON_ZERO
	}

	WindingRule windingRule();

	/**
	 * @return The set of component polygons. These polygons may be self
	 *         intersecting and may intersect each other. There are no TODO
	 */
	Set<ClosedPolyline2<V>> boundaryComponents();

	default Bounds2<?> getBounds() {
		return new Bounds2<V>(
				boundaryComponents().stream().flatMap(p -> p.vertices().stream()).collect(Collectors.toSet()));
	}

	default Value<?> getPerimeter() {
		return boundaryComponents().stream().collect(DoubleValue::new, (v, p) -> v.add(p.getPerimeter()), Value::add);
	}

	@Override
	default boolean contains(Vector2<?> point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	default boolean touches(Vector2<?> point) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	default CompoundPolygon<?, V> and(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getAnd(CompoundPolygon<?, V> value) {
		return copy().and(value);
	}

	@Override
	default CompoundPolygon<?, V> nand(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getNand(CompoundPolygon<?, V> value) {
		return copy().nand(value);
	}

	@Override
	default CompoundPolygon<?, V> nor(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getNor(CompoundPolygon<?, V> value) {
		return copy().nor(value);
	}

	@Override
	default CompoundPolygon<?, V> or(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getOr(CompoundPolygon<?, V> value) {
		return copy().or(value);
	}

	@Override
	default CompoundPolygon<?, V> xnor(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getXnor(CompoundPolygon<?, V> value) {
		return copy().xnor(value);
	}

	@Override
	default CompoundPolygon<?, V> xor(CompoundPolygon<?, V> value) {
		return null; // TODO
	}

	@Override
	default CompoundPolygon<?, V> getXor(CompoundPolygon<?, V> value) {
		return copy().xor(value);
	}
}
