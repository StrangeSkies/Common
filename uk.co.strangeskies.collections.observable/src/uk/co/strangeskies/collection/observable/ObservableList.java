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

import java.util.Collection;
import java.util.List;

public interface ObservableList<E>
    extends List<E>, ObservableCollection<E, ObservableList.Change<E>> {
  /**
   * A change event for {@link ObservableList}. All elements added or removed
   * during a change operation may be inspected, along with their indices before
   * or after removal or addition respectively.
   * 
   * <p>
   * The original order of sub-events before aggregation is lost. The effective
   * ordering of the aggregated change operation is removal, then addition.
   * 
   * <p>
   * Events
   *
   * @author Elias N Vasylenko
   * @param <E>
   *          the element type, as per {@link Collection}
   */
  interface Change<E> {
    int[] removedIndices();

    List<E> removedItems();

    /*- TODO modification
    int[] modifiedIndices();
    
    List<E> modifiedFromItems();
     */

    /*- TODO permutation
    int[] permutedFromIndices();
    
    int[] permutedToIndices();
     */

    int[] addedIndices();

    List<E> addedItems();
  }

  @Override
  default ObservableList<E> unmodifiableView() {
    return new UnmodifiableObservableList<>(this);
  }

  @Override
  default ObservableList<E> synchronizedView() {
    return new SynchronizedObservableList<>(this);
  }

  @Override
  List<E> silent();
}
