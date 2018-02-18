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
package uk.co.strangeskies.mathematics.geometry.matrix.vector;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

import uk.co.strangeskies.mathematics.geometry.Rotatable2;
import uk.co.strangeskies.mathematics.values.Value;

public interface Vector2<V extends Value<V>> extends Vector<Vector2<V>, V>,
		Rotatable2<Vector2<V>> {
	public default V getX() {
		return getElement(0);
	}

	public default V getY() {
		return getElement(1);
	}

	public default Vector2<V> setData(Dimension dimension) {
		return setData(dimension.getWidth(), dimension.getHeight());
	}

	public default Vector2<V> setData(Point point) {
		return setData(point.getX(), point.getY());
	}

	public default Vector2<V> setData(Point2D point) {
		return setData(point.getX(), point.getY());
	}

	public default Dimension getDimension() {
		return new Dimension(getX().intValue(), getY().intValue());
	}

	public default Point getPoint() {
		return new Point(getX().intValue(), getY().intValue());
	}

	public default Point2D getPoint2D() {
		return new Point2D.Double(getX().doubleValue(), getY().doubleValue());
	}
}
