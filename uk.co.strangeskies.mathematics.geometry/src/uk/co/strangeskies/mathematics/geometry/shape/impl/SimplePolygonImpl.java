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

import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.SimplePolygon;
import uk.co.strangeskies.mathematics.values.DoubleValue;
import uk.co.strangeskies.mathematics.values.Value;

//simple and potentially self touching polygons (relatively simple)
public class SimplePolygonImpl<V extends Value<V>> extends ComplexPolygonImpl<SimplePolygonImpl<V>, V>
		implements SimplePolygon<SimplePolygonImpl<V>, V> {
	public void sanitise(int toleranceLevel) {
		// if (toleranceLevel < 0) {
		// toleranceLevel = 0;
		// }
		// // NEED TO OPTIMISE WITH LINE-SWEEP ALGORITHMS
		//
		// // calibrate tolerance with largest ulp
		// float largestValue = 0;
		// float xAbs, yAbs;
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end(); ++i) {
		// xAbs = fabs((*i).x);
		// if (xAbs > largestValue)
		// largestValue = xAbs;
		//
		// yAbs = fabs((*i).y);
		// if (yAbs > largestValue)
		// largestValue = yAbs;
		// }
		//
		// float tolerance = Ulp(largestValue) * toleranceLevel;
		// // snap together points within tolerence squares and remove zero length
		// segments
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end() - 1; ++i)
		// for (List<Vector2<V>> j : i + 1; j < vertices.end(); ++j)
		// if (ToleranceEqual((*j).x, (*i).x, tolerance) && ToleranceEqual((*j).y,
		// (*i).y, tolerance))
		// if (j == i + 1)
		// vertices.erase(j--);
		// else {
		// (*j).x = (*i).x;
		// (*j).y = (*i).y;
		//
		// // record all touching points found or generated ***********
		// }
		// if (ToleranceEqual(vertices.front().x, vertices.back().x, tolerance) &&
		// ToleranceEqual(vertices.front().y, vertices.back().y, tolerance))
		// vertices.erase(vertices.begin());
		//
		// // snap lines to all points with which they touch the ULP tolerence
		// squares of.
		// tolerance = Ulp(largestValue) * toleranceLevel;
		//
		// // points found touching line, sorted in order
		// std::multimap<float, List<Vector2<V>>::iterator> pointsAlongLine;
		// // iterate through each edge (last, i)
		// List<Vector2<V>>::iterator last = --vertices.end();
		// for (List<Vector2<V>> i : vertices.begin(); i < vertices.end(); ++i) {
		// Line line = Line(*last, *i);
		//
		// // iterate through each point to check if on line.
		// pointsAlongLine.clear();
		// for (List<Vector2<V>> j : vertices.begin(); j < vertices.end(); ++j) {
		// if (j != i && j != last) { // && j !touching i or last
		// Box box = Box(Vector2<V>(*j) -= Vector2<V>(tolerance), Vector2<V>(*j) +=
		// Vector2<V>(tolerance));
		//
		// if (box.CrossesLine(line)) {
		// float distanceAlongLine = Matrix2(!line.GetAB().Normalize(), *j -
		// line.GetA()).Det();
		//
		// if (distanceAlongLine > 0 && distanceAlongLine < line.GetLength())
		// pointsAlongLine.insert(std::make_pair(distanceAlongLine, j));
		// }
		// }
		// }
		//
		// // snap line to intersection points found
		// for (std::multimap<float, List<Vector2<V>>::iterator> j :
		// pointsAlongLine.begin(); j != pointsAlongLine.end(); ++j) {
		// vertices.insert(i, (*j).first);
		// // record touching points generated ( return value of insert() <|>
		// (*insert).first )
		// }
		//
		// // make sure to perform procedure iteratively by returning to first new
		// subline (if any)
		// if (pointsAlongLine.size() > 0)
		// i = last;
		//
		// // Proceed to next line...
		// last = i;
		// }
		//
		// // find lines crossing lines and add vertices at crossing point
		// // SHOULD BE ROBUST IF WE CAN GUARENTEE SOME MAXIMUM ULP IMPRECISION IN
		// INTERSECTION POINTS
		//
		// // find first intersection (cross not touch. when determining which for
		// // intersection strips, we can detect and remove empty slithers)
		// // 'turn' at first point of intersection
		// // (possibly remove turned section if length is below a
		// // certain cutoff point, must consider other half also)
		// int lastIntersection = 0;
		// boolean finished = false;
		// do {
		//
		// } while(!finished);
	}

	@Override
	public DoubleValue getArea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimplePolygonImpl<V> copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WindingDirection getWindingDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClosedPolyline2<V> boundary() {
		// TODO Auto-generated method stub
		return null;
	}
}
