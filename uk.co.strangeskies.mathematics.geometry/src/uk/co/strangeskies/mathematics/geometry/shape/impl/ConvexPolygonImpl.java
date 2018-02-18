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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.expression.DependentExpression;
import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.expression.collection.ExpressionSet;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.geometry.shape.ClosedPolyline2;
import uk.co.strangeskies.mathematics.geometry.shape.ConvexPolygon;
import uk.co.strangeskies.mathematics.geometry.shape.Shape;
import uk.co.strangeskies.mathematics.values.Value;

//convex only polygon
public class ConvexPolygonImpl<V extends Value<V>> extends DependentExpression<ConvexPolygonImpl<V>>
    implements ConvexPolygon<ConvexPolygonImpl<V>, V> {
  public class ConvexHull<T> extends AbstractSet<Vector2<V>> {
    private final ArrayList<Vector2<V>> backingList;

    private ConvexHull(Expression<?> hullVertexSet) {
      backingList = new ArrayList<>();
      hullVertexSet.invalidations().weakReference(this).observe(
          message -> message.owner().invalidate());
    }

    public void invalidate() {

    }

    protected boolean tryAdd(Vector2<V> point) {
      if (backingList.contains(point)) {
        return false;
      }

      List<Vector2<V>> vertices = boundary().vertices();

      // fail if point is already part of shape
      if (touches(point))
        return false;

      // special case for current size < 3
      if (vertices.size() < 3) {
        vertices.add(point);

        // make sure wound in correct direction
        /*-
        if (vertices.size() == 3
        		&& ConvexPolygonImpl.super.getWindingDirection() != getWindingDirection()) {
        	vertices.add(vertices.remove(0));
        }*/
        return true;
      }

      // find the end points of the edge sequence which has the point on the
      // right.

      // was the point on the right of the last edge
      /*-
      boolean right = false;
      
      for (int i = begin; i < end; i++) {
      	// is the point on the right of the edge from this vertex to the next
      	if (new Matrix2Impl<V>(i - (i + 1), i - point).Det() > 0.01) {
      		// was it on the other side last time
      		if (right == false) {
      			right = true;
      			first = i;
      			if (last >= begin)
      				i = end;
      		}
      	} else {
      		// was it on the other side last time
      		if (right == true) {
      			right = false;
      			last = i;
      			if (first > begin)
      				i = end;
      		}
      	}
      }
      if (last == begin - 1)
      	last = begin;
      
      // construct new convex polygon
      List<Vector2Impl<V>> newVertices;
      
      if (first < last) {
      	for (i = first; i <= last; i++)
      		newVertices.push_back(i);
      	newVertices.push_back(point);
      } else {
      	for (i = first; i < end; i++)
      		newVertices.push_back(i);
      	for (i = begin; i <= last; i++)
      		newVertices.push_back(i);
      	newVertices.push_back(point);
      }
      
      vertices = newVertices;
       */

      return true;
    }

    @Override
    public Iterator<Vector2<V>> iterator() {
      return backingList.iterator();
    }

    @Override
    public int size() {
      return backingList.size();
    }
  }

  private final ExpressionSet<?, Vector2<V>> vertexSet;
  private final ConvexHull<V> convexHull;

  private WindingDirection windingDirection;

  public ConvexPolygonImpl() {
    vertexSet = null; // TODO new ExpressionSetDecorator<Vector2<V>>();
    convexHull = new ConvexHull<>(vertexSet);
    // getDependencies().add(vertexSet);

    windingDirection = WindingDirection.CLOCKWISE;
  }

  public void setWindingDirection(WindingDirection windingDirection) {
    this.windingDirection = windingDirection;
  }

  @Override
  public WindingDirection getWindingDirection() {
    return windingDirection;
  }

  @Override
  public boolean contains(Vector2<?> point) {
    // TODO test on right of left side and on left or right side.
    return false;
  }

  @Override
  public boolean touches(Vector2<?> point) {
    // TODO test on right of left side and on left or right side.
    return false;
  }

  @Override
  public ConvexPolygonImpl<V> copy() {
    ConvexPolygonImpl<V> copy = new ConvexPolygonImpl<>();

    Set<Vector2<V>> copyVertexSet = copy.vertexSet();
    for (Vector2<V> vertex : vertexSet()) {
      copyVertexSet.add(vertex.copy());
    }

    return copy;
  }

  @Override
  public Set<Vector2<V>> hullVertexSet() {
    return convexHull;
  }

  @Override
  public Set<Vector2<V>> vertexSet() {
    return vertexSet;
  }

  @Override
  public ClosedPolyline2<V> boundary() {
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
  protected ConvexPolygonImpl<V> evaluate() {
    // TODO Auto-generated method stub
    return null;
  }
}
