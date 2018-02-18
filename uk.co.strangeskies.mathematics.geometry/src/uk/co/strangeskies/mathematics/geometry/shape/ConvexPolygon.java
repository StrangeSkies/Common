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

import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

/**
 * 
 * @author Elias N Vasylenko
 * 
 * @param <V>
 *            The type of value for the coordinate system of the polygon
 */
public interface ConvexPolygon<S extends ConvexPolygon<S, V>, V extends Value<V>> extends SimplePolygon<S, V> {
	/**
	 * This method returns a modifiable set containing all vertices which
	 * currently effectively make a contribution to the convex Hull of this
	 * Polygon, i.e. all vertices which form a part of the outside edge and are
	 * returned by the {@link ConvexPolygon#vertexSet()}.
	 * 
	 * The Set is backed by the shape and changes will be reflected in it. If
	 * vertices are moved around such that the convex hull changes any set
	 * previously returned will reflect this change.
	 * 
	 * Adding vertices to the set will succeed if they are not already present,
	 * and fall outside the current perimeter of the shape, and the shape will
	 * be updated to reflect the addition. If the vertices being added lie
	 * within the perimeter the addition to the set will fail and the underlying
	 * model will not be changed. If more than one vertex is added at the same
	 * time, only those which remain a part of this set after they have all been
	 * added will remain a part of the underlying model.
	 * 
	 * @return The vertices describing the convex hull of the polygon
	 */
	public Set<Vector2<V>> hullVertexSet();

	/**
	 * This method returns a modifiable set representing the underlying
	 * collection of vertices from which the convex hull represented by this
	 * polygon is generated. This set may contain vertices which lie within the
	 * convex hull, such that on removal of other vertices, or movement of
	 * vertices, they may become a part of the convex hull.
	 * 
	 * The Set is backed by the shape and changes will be reflected in it. If
	 * vertices are added or removed from the shape, they will be added to or
	 * removed from this set.
	 * 
	 * Adding vertices to the set will always succeed if they are not already
	 * present.
	 * 
	 * @return The vertices contained within this polygon
	 */
	public Set<Vector2<V>> vertexSet();

	@Override
	default CompoundPolygon<?, V> and(CompoundPolygon<?, V> value) {
		return null; // TODO
	}
}
