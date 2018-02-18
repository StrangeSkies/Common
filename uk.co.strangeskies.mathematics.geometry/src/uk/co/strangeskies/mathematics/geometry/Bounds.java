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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import uk.co.strangeskies.mathematics.Interval;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.utility.Copyable;

public abstract class Bounds<S extends Bounds<S, V>, V extends Value<V>> implements Copyable<S> {
  ArrayList<Interval<V>> ranges;

  public Bounds(int dimensions, Supplier<V> valueFactory) {
    try {
      DimensionalityException.checkValid(dimensions);
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }

    ranges = new ArrayList<>();
    for (int i = 0; i < dimensions; i++) {
      ranges.add(Interval.bounded(valueFactory.get(), valueFactory.get()));
    }
  }

  public Bounds(Vector<?, V> from, Vector<?, V> to) {
    this(from, to, from.getDimensions());
  }

  public Bounds(Vector<?, V> from, Vector<?, V> to, int dimensions) {
    try {
      DimensionalityException.checkEquivalence(from.getDimensions(), dimensions);
      DimensionalityException.checkEquivalence(to.getDimensions(), dimensions);
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }
    ranges = new ArrayList<>();

    Iterator<? extends V> fromIterator = from.getData().iterator();
    Iterator<? extends V> toIterator = to.getData().iterator();
    while (fromIterator.hasNext()) {
      ranges.add(Interval.bounded(fromIterator.next(), toIterator.next()));
    }
  }

  public Bounds(@SuppressWarnings("unchecked") Vector<?, V>... points) {
    this(Arrays.asList(points));
  }

  public Bounds(int dimensions, @SuppressWarnings("unchecked") Vector<?, V>... points) {
    this(dimensions, Arrays.asList(points));
  }

  public Bounds(Collection<? extends Vector<?, V>> points) {
    this(points.iterator().next().getDimensions(), points);
  }

  public Bounds(int dimensions, Collection<? extends Vector<?, V>> points) {
    try {
      for (Vector<?, V> point : points) {
        DimensionalityException.checkEquivalence(dimensions, point.getDimensions());
      }
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }
    ranges = new ArrayList<>();

    Iterator<? extends Vector<?, V>> pointIterator = points.iterator();
    Vector<?, V> firstPoint = pointIterator.next();
    for (V value : firstPoint.getData()) {
      ranges.add(Interval.bounded(value, value));
    }
    while (pointIterator.hasNext()) {
      int i = 0;
      for (V value : pointIterator.next().getData()) {
        ranges.set(i, ranges.get(i).getExtendedThrough(value, true));
      }
    }
  }

  public Bounds(Bounds<?, V> other) {
    ranges = new ArrayList<>();
    for (Interval<V> range : other.getData()) {
      ranges.add(range);
    }
  }

  public Bounds(Bounds<?, V> other, int dimensions) {
    try {
      DimensionalityException.checkEquivalence(other.getDimension(), dimensions);
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }
    ranges = new ArrayList<>();
    for (Interval<V> range : other.getData()) {
      ranges.add(range);
    }
  }

  public final List<Interval<V>> getData() {
    return Collections.unmodifiableList(ranges);
  }

  public final Interval<V> getRange(int index) {
    return getData().get(index);
  }

  public int getDimension() {
    return ranges.size();
  }
}
