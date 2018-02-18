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
import java.util.function.Consumer;

import uk.co.strangeskies.observable.Observable;

/**
 * A collections which can be observed for changes. The collection is
 * {@link Observable} over itself, i.e. events will be triggered with the event
 * object being the collection itself. There is also an observable interface
 * {@link #changes()} over a type {@code C} which is parameterisable by
 * subtypes, and which may contain further information about the nature of the
 * change.
 * <p>
 * Observable events should be triggered each time the collection is changed.
 * Many small internal sub-changes may be grouped into a single conceptual
 * change, and so only trigger once upon completion. The ordering of sub-changes
 * is generally considered an implementation detail in this case, and may not be
 * made available via the change event message type {@code C}.
 * <p>
 * Event messages may only be valid during the invocation of the
 * {@link Consumer#accept(Object)} method on observers, so references should
 * typically not be held beyond this point.
 *
 * @author Elias N Vasylenko
 * @param <E>
 *          the element type, as per {@link Collection}
 * @param <C>
 *          the change event message type
 */
public interface ObservableCollection<E, C> extends Collection<E> {
  /**
   * @return a view of the collections which does not allow modification
   */
  ObservableCollection<E, ?> unmodifiableView();

  /**
   * @return a view of the collections which is safe in concurrent contexts
   */
  ObservableCollection<E, ?> synchronizedView();

  Observable<? extends ObservableCollection<E, C>> invalidations();

  /**
   * @return an observable instance over changes
   */
  Observable<C> changes();

  /**
   * Get a view of the collection which can be mutated without triggering events.
   * Operations performed on the returned list may need to be synchronized
   * manually with the backing list.
   * 
   * @return a view transparent to listeners
   */
  Collection<E> silent();
}
