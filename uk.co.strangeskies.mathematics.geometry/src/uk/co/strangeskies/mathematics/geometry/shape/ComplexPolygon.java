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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import uk.co.strangeskies.mathematics.values.Value;

public interface ComplexPolygon<S extends ComplexPolygon<S, V>, V extends Value<V>>
    extends /* @Immutable */CompoundPolygon<S, V> {
  /*
   * Vertices describing polygon
   *
   * Guaranteed to be continuous/unbroken path describing the polygon, but may
   * include repeated elements where there is self intersection of edges. In the
   * case of a ComplexPolygon class, for example, this will effectively return a
   * keyholed and stitched representation of the component contours.
   *
   * The path described is <em>not</em> guaranteed to not contain degenerate edges
   * or self intersections.
   */
  public/*  */ClosedPolyline2<V> boundary();

  @Override
  public default Set<ClosedPolyline2<V>> boundaryComponents() {
    return new HashSet<>(Arrays.asList(boundary()));
  }

  @Override
  default CompoundPolygon<?, V> nand(CompoundPolygon<?, V> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default CompoundPolygon<?, V> nor(CompoundPolygon<?, V> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default CompoundPolygon<?, V> or(CompoundPolygon<?, V> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default CompoundPolygon<?, V> xnor(CompoundPolygon<?, V> value) {
    throw new UnsupportedOperationException();
  }

  @Override
  default CompoundPolygon<?, V> xor(CompoundPolygon<?, V> value) {
    throw new UnsupportedOperationException();
  }
}
