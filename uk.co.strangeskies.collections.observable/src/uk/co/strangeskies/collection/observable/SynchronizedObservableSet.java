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

import java.util.Collections;
import java.util.Set;

import uk.co.strangeskies.collection.SetDecorator;
import uk.co.strangeskies.observable.Observable;

public class SynchronizedObservableSet<E> implements SetDecorator<E>, ObservableSet<E> {
  private final ObservableSet<E> component;
  private final Set<E> synchronizedComponent;

  public SynchronizedObservableSet(ObservableSet<E> component) {
    this.component = component;
    this.synchronizedComponent = Collections.synchronizedSet(component);
  }

  public static <E> SynchronizedObservableSet<E> over(ObservableSet<E> component) {
    return new SynchronizedObservableSet<>(component);
  }

  public Object getMutex() {
    return synchronizedComponent;
  }

  @Override
  public Set<E> getComponent() {
    return synchronizedComponent;
  }

  @Override
  public Observable<Change<E>> changes() {
    return component.changes().synchronize(getMutex());
  }

  @Override
  public Observable<? extends ObservableCollection<E, Change<E>>> invalidations() {
    return component.invalidations().synchronize(getMutex()).map(a -> this);
  }

  @Override
  public Set<E> silent() {
    return component.silent();
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
}
