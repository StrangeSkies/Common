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

import static java.util.Collections.emptyListIterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import uk.co.strangeskies.utility.Scoped;

/**
 * 
 * 
 * @author Elias N Vasylenko
 * @param <E>
 *          the element type, as per {@link Collection}
 */
public class ScopedObservableSet<E> extends ObservableSetDecorator<E>
    implements Scoped<ScopedObservableSet<E>> {
  private final Supplier<? extends Set<E>> componentFactory;
  private final ScopedObservableSet<E> parent;

  public ScopedObservableSet(Supplier<? extends Set<E>> componentFactory) {
    this(null, componentFactory);
  }

  protected ScopedObservableSet(
      ScopedObservableSet<E> parent,
      Supplier<? extends Set<E>> componentFactory) {
    super(componentFactory.get());

    this.parent = parent;
    this.componentFactory = componentFactory;

    forwardEvents();
  }

  private void forwardEvents() {
    if (getParentScope().isPresent()) {
      ScopedObservableSet<E> parent = getParentScope().get();
      Set<E> silent = silent();

      parent.changes().observe(change -> {
        /*
         * If we add items to the parent which are currently in the child, we must
         * silently remove them, and modify the change event so that those additions are
         * not seen from the child scope when we forward it...
         */
        Set<E> effectivelyAdded = null;
        for (E item : change.added()) {
          if (silent.remove(item)) {
            if (effectivelyAdded == null) {
              effectivelyAdded = new HashSet<>(change.added());
            }
            effectivelyAdded.remove(item);
          }
        }

        Change<E> effectiveChange;
        if (effectivelyAdded == null) {
          effectiveChange = change;
        } else {
          if (effectivelyAdded.isEmpty() && change.removed().isEmpty()) {
            /*
             * No items were *effectively* added, and none were removed, so we can drop the
             * event.
             */
            return;
          } else {
            effectiveChange = wrapChange(change, effectivelyAdded);
          }
        }

        /*
         * Forward change events
         */
        fireChange(effectiveChange);
      });
    }
  }

  private static <T> Change<T> wrapChange(Change<T> change, Set<T> effectivelyAdded) {
    return new Change<T>() {
      @Override
      public Set<T> added() {
        return effectivelyAdded;
      }

      @Override
      public Set<T> removed() {
        return change.removed();
      }
    };
  }

  @Override
  public boolean add(E e) {
    if (getParentScope().map(p -> p.contains(e)).orElse(false))
      return false;

    return super.add(e);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean changed = false;

    for (E e : c) {
      changed = add(e) || changed;
    }

    return changed;
  }

  @Override
  public boolean contains(Object o) {
    return ScopedObservableSet.super.contains(o)
        || getParentScope().map(p -> p.contains(o)).orElse(false);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return c.stream().allMatch(this::contains);
  }

  /**
   * @return an iterator over only those items which are local to this scope
   */
  public Iterator<E> localIterator() {
    return super.iterator();
  }

  @Override
  public Iterator<E> iterator() {
    Iterator<E> iterator = localIterator();
    Iterator<E> parentIterator = getParentScope().map(Collection::iterator).orElse(
        emptyListIterator());

    return new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext() || parentIterator.hasNext();
      }

      @Override
      public E next() {
        return iterator.hasNext() ? iterator.next() : parentIterator.next();
      }
    };
  }

  @Override
  public int size() {
    return super.size() + getParentScope().map(Set::size).orElse(0);
  }

  @Override
  public boolean isEmpty() {
    return super.isEmpty() && getParentScope().map(Collection::isEmpty).orElse(true);
  }

  @Override
  public Optional<ScopedObservableSet<E>> getParentScope() {
    return Optional.ofNullable(parent);
  }

  @Override
  public void collapseIntoParentScope() {
    getParentScope().get().silent().addAll(this);
    silent().clear();
  }

  @Override
  public ScopedObservableSet<E> nestChildScope() {
    return new ScopedObservableSet<>(this, componentFactory);
  }
}
