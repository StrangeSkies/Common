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

public class ImmutableObservableValue<T> implements ObservableValue<T> {
  private final T message;
  private final Throwable failure;

  public ImmutableObservableValue(T message) {
    this(message, null);
  }

  public ImmutableObservableValue(Throwable failure) {
    this(null, failure);
  }

  private ImmutableObservableValue(T message, Throwable failure) {
    this.message = message;
    this.failure = failure;
  }

  @Override
  public Disposable observe(Observer<? super T> observer) {
    Observation observation = new Observation() {
      RequestCount requests = new RequestCount();

      @Override
      public void cancel() {}

      @Override
      public void request(long count) {
        requests.request(count);
      }

      @Override
      public long getPendingRequestCount() {
        return requests.getCount();
      }
    };

    observer = new SafeObserver<>(observer);
    observer.onObserve(observation);
    if (message != null) {
      observer.onNext(message);
      observer.onComplete();
    } else {
      observer.onFail(failure);
    }

    return observation;

  }

  @Override
  public ObservableValue<T> toValue() {
    return this;
  }

  @Override
  public ObservableValue<T> toValue(T initial) {
    return this;
  }

  @Override
  public ObservableValue<T> toValue(Throwable initialProblem) {
    return this;
  }

  @Override
  public T get() {
    if (message != null) {
      return message;
    } else {
      throw new MissingValueException(this, failure);
    }
  }

  @Override
  public Observable<Change<T>> changes() {
    return Observable.empty();
  }
}