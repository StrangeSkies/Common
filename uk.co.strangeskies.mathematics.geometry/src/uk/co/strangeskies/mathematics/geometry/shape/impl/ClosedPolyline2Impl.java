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

import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.mathematics.geometry.Bounds2;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.Line2;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.Value;
import uk.co.strangeskies.observable.Observable;

public class ClosedPolyline2Impl<V extends Value<V>> implements ClosedPolyline2<V> {
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
  public Bounds2<V> getBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClosedPolyline2<V> copy() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClosedPolyline2<V> getValue() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Line2<V>> lines() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Vector2<V>> vertices() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Value<?> getLength() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Value<?> getArea() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Value<?> getPerimeter() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean contains(Vector2<?> point) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean touches(Vector2<?> point) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Observable<Expression<? extends ClosedPolyline2<V>>> invalidations() {
    // TODO Auto-generated method stub
    return null;
  }

}
