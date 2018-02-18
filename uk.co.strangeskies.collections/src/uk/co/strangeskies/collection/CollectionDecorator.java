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
 * This file is part of uk.co.strangeskies.collections.
 *
 * uk.co.strangeskies.collections is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection;

import java.util.Collection;
import java.util.Iterator;

public interface CollectionDecorator<T extends Collection<E>, E> extends Collection<E> {
  Collection<E> getComponent();

  @Override
  default boolean add(E e) {
    return getComponent().add(e);
  }

  @Override
  default boolean addAll(Collection<? extends E> c) {
    return getComponent().addAll(c);
  }

  @Override
  default void clear() {
    getComponent().clear();
  }

  @Override
  default boolean contains(Object o) {
    return getComponent().contains(o);
  }

  @Override
  default boolean containsAll(Collection<?> c) {
    return getComponent().containsAll(c);
  }

  @Override
  default boolean isEmpty() {
    return getComponent().isEmpty();
  }

  @Override
  default Iterator<E> iterator() {
    return getComponent().iterator();
  }

  @Override
  default boolean remove(Object o) {
    return getComponent().remove(o);
  }

  @Override
  default boolean removeAll(Collection<?> c) {
    return getComponent().removeAll(c);
  }

  @Override
  default boolean retainAll(Collection<?> c) {
    return getComponent().retainAll(c);
  }

  @Override
  default int size() {
    return getComponent().size();
  }

  @Override
  default Object[] toArray() {
    return getComponent().toArray();
  }

  @Override
  default <A> A[] toArray(A[] a) {
    return getComponent().toArray(a);
  }
}
