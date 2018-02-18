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
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector3;
import uk.co.strangeskies.mathematics.values.Value;

public class Bounds3<V extends Value<V>> extends Bounds<Bounds3<V>, V> {
  public Bounds3(Bounds<?, V> other) {
    super(other, 3);
  }

  public Bounds3(Bounds3<V> other) {
    super(other);
  }

  public Bounds3(Vector<?, V> from, Vector<?, V> to) {
    super(from, to, 3);
  }

  public Bounds3(Vector3<V> from, Vector3<V> to) {
    super(from, to);
  }

  public Bounds3(@SuppressWarnings("unchecked") Vector3<V>... points) {
    super(points);
  }

  public Bounds3(@SuppressWarnings("unchecked") Vector<?, V>... points) {
    super(3, points);
  }

  public Bounds3(Collection<? extends Vector<?, V>> points) {
    super(3, points);
  }

  public Bounds3(Supplier<V> valueFactory) {
    super(3, valueFactory);
  }

  public final Interval<V> getRangeX() {
    return super.getRange(0);
  }

  public final Interval<V> getRangeY() {
    return super.getRange(1);
  }

  public final Interval<V> getRangeZ() {
    return super.getRange(2);
  }

  @Override
  public final Bounds3<V> copy() {
    return new Bounds3<>(this);
  }
}
