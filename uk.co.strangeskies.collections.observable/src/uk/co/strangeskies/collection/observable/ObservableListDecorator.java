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
 * This file is part of uk.co.strangeskies.collections.observable.
 *
 * uk.co.strangeskies.collections.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.observable;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.collection.ListDecorator;
import uk.co.strangeskies.observable.HotObservable;
import uk.co.strangeskies.observable.Observable;

public class ObservableListDecorator<E> implements ListDecorator<E>, ObservableList<E> {
  final class ChangeImpl implements Change<E> {
    @Override
    public int[] removedIndices() {
      return removedIndices;
    }

    @Override
    public List<E> removedItems() {
      return removedItems;
    }

    @Override
    public int[] addedIndices() {
      return addedIndices;
    }

    @Override
    public List<E> addedItems() {
      return new AbstractList<E>() {
        @Override
        public E get(int index) {
          return ObservableListDecorator.this.get(addedIndices[index]);
        }

        @Override
        public int size() {
          return ObservableListDecorator.this.size();
        }
      };
    }
  }

  private final List<E> component;

  private final HotObservable<ObservableList<E>> invalidationObservable = new HotObservable<>();
  private final HotObservable<Change<E>> changeObservable = new HotObservable<>();

  private boolean firing;
  private int[] addedIndices;
  private int[] removedIndices;
  private List<E> removedItems;
  private int changeDepth = 0;
  private final ChangeImpl change = new ChangeImpl();

  public ObservableListDecorator(List<E> component) {
    this.component = component;
  }

  @Override
  public List<E> getComponent() {
    return component;
  }

  protected boolean beginChange() {
    if (changeDepth++ == 0) {
      firing = changeObservable.hasObservers();

      return true;
    } else {
      return false;
    }
  }

  protected boolean endChange() {
    if (--changeDepth == 0) {
      fireChange(change);

      // TODO make change empty

      return true;
    } else {
      return false;
    }
  }

  protected void fireChange(Change<E> change) {
    changeObservable.next(change);
    fireEvent();
  }

  protected void fireEvent() {
    invalidationObservable.next(this);
  }

  @Override
  public Observable<? extends ObservableCollection<E, Change<E>>> invalidations() {
    return invalidationObservable;
  }

  @Override
  public boolean add(E e) {
    try {
      beginChange();

      ListDecorator.super.add(e);

      if (change != null) {
        // TODO do change
      }

      return true;
    } finally {
      endChange();
    }
  }

  @Override
  public void add(int index, E element) {
    try {
      beginChange();

      ListDecorator.super.add(index, element);

      if (change != null) {
        // TODO do change
      }
    } finally {
      endChange();
    }
  }

  @Override
  public boolean remove(Object o) {
    try {
      beginChange();

      // int index = indexOf(o);
      boolean changed = ListDecorator.super.remove(o);

      if (changed && change != null) {
        // TODO do change
      }

      return changed;
    } finally {
      endChange();
    }
  }

  @Override
  public E remove(int index) {
    try {
      beginChange();

      E previous = ListDecorator.super.remove(index);

      if (change != null) {
        // TODO do change
      }

      return previous;
    } finally {
      endChange();
    }
  }

  @Override
  public E set(int index, E element) {
    try {
      beginChange();

      E previous = ListDecorator.super.set(index, element);

      if (change != null) {
        // TODO do change
      }

      return previous;
    } finally {
      endChange();
    }
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    try {
      beginChange();

      boolean changed = ListDecorator.super.addAll(c);

      if (changed && change != null) {
        // TODO do change
      }

      return changed;
    } finally {
      endChange();
    }
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    try {
      beginChange();

      boolean changed = ListDecorator.super.addAll(index, c);

      if (changed && change != null) {
        // TODO do change
      }

      return changed;
    } finally {
      endChange();
    }
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    try {
      beginChange();

      boolean changed = ListDecorator.super.removeAll(c);

      if (changed && change != null) {
        // TODO do change
      }

      return changed;
    } finally {
      endChange();
    }
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    try {
      beginChange();

      boolean changed = ListDecorator.super.retainAll(c);

      if (changed && change != null) {
        // TODO do change
      }

      return changed;
    } finally {
      endChange();
    }
  }

  @Override
  public HotObservable<Change<E>> changes() {
    return changeObservable;
  }

  @Override
  public String toString() {
    return getComponent().toString();
  }

  @Override
  public int hashCode() {
    return getComponent().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return getComponent().equals(obj);
  }

  @Override
  public List<E> silent() {
    return component;
  }
}
