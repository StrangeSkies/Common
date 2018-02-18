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
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * A value which can be {@link #get() fetched} and observed for updates and
 * {@link #changes() changes}.
 * <p>
 * Observable value adheres to the contract of {@link Observable}, with a few
 * extra restrictions on its behavior to conform to the semantics of a mutable
 * value.
 * <p>
 * An instance may send a {@link Observer#onComplete() completion} event to
 * indicate that it will not mutate beyond that point, but it should only send
 * this to an observer if it has already sent them a message event containing
 * the current value.
 * <p>
 * The value may enter an error state, in which case observers will receive a
 * {@link Observer#onFail(Throwable) failure event} and subsequent calls to
 * {@link #get()} should throw a {@link MissingValueException} as per
 * {@link Observable#get()} until a new valid value is available.
 * <p>
 * A common example of an error state may be {@link NullPointerException} when
 * there is no value available.
 * <p>
 * The observable should always be primed with either a message event or a
 * failure event immediately upon instantiation, and observers which subscribe
 * to the observable should receive the last such event directly subsequent to
 * observation. This means that the {@link #get()} method inherited from
 * observable should never have to block.
 * <p>
 * Observable values should never support backpressure.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value
 */
public interface ObservableValue<T> extends Observable<T> {
  /**
   * A value change event.
   * 
   * @author Elias N Vasylenko
   *
   * @param <T>
   *          the type of the value
   */
  interface Change<T> {
    ObservableValue<T> previousValue();

    ObservableValue<T> newValue();
  }

  /**
   * Immediately resolve the current value if one exists, otherwise throw a
   * {@link MissingValueException} with a cause representing the current failure
   * state.
   * 
   * @return the current value
   */
  @Override
  T get();

  /**
   * Immediately resolve the current value, if one exists.
   * 
   * @return an optional containing the current value, or an empty option if no
   *         value is available
   */
  @Override
  default Optional<T> tryGet() {
    return Observable.super.tryGet();
  }

  default boolean isValid() {
    return tryGet().isPresent();
  }

  default boolean isEqual(T value) {
    return isMatching(value::equals);
  }

  default boolean isMatching(Predicate<? super T> value) {
    return tryGet().filter(value::test).isPresent();
  }

  default Throwable getProblem() {
    try {
      get();
      throw new IllegalStateException();
    } catch (MissingValueException e) {
      return e.getCause();
    }
  }

  default Optional<Throwable> tryGetProblem() {
    try {
      return Optional.of(getProblem());
    } catch (IllegalStateException e) {
      return Optional.empty();
    }
  }

  /**
   * @return an observable over changes to the value
   */
  Observable<Change<T>> changes();
}
