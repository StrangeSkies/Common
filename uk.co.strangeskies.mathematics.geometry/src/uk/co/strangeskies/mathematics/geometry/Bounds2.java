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
package uk.co.strangeskies.mathematics.geometry;

import java.util.Collection;
import java.util.function.Supplier;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.values.Value;

public class Bounds2<V extends Value<V>> extends Bounds<Bounds2<V>, V> {
	public Bounds2(Bounds<?, V> other) throws DimensionalityException {
		super(other, 2);
	}

	public Bounds2(Bounds2<V> other) {
		super(other);
	}

	public Bounds2(Vector2<V> from, Vector2<V> to) {
		super(from, to);
	}

	public Bounds2(Vector<?, V> from, Vector<?, V> to) {
		super(from, to, 2);
	}

	public Bounds2(@SuppressWarnings("unchecked") Vector2<V>... points) {
		super(points);
	}

	public Bounds2(@SuppressWarnings("unchecked") Vector<?, V>... points) {
		super(2, points);
	}

	public Bounds2(Collection<? extends Vector<?, V>> points) {
		super(2, points);
	}

	public Bounds2(Supplier<V> valueFactory) {
		super(2, valueFactory);
	}

	public final Interval<V> getRangeX() {
		return super.getRange(0);
	}

	public final Interval<V> getRangeY() {
		return super.getRange(1);
	}

	@Override
	public final Bounds2<V> copy() {
		return new Bounds2<>(this);
	}
}
