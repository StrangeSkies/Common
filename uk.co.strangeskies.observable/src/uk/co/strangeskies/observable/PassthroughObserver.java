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

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

/**
 * This is a helper class for implementing {@link Observable passive
 * observables}.
 * <p>
 * A passive observable is one which does not maintain a set of observations or
 * manage its own events, instead deferring to one or more upstream observables.
 * When an observer subscribes to a passive observable, typically the observer
 * is decorated, and the decorator is then subscribed to the parents. This way
 * the decorator can modify, inspect, or filter events as appropriate before
 * passing them back through to the original observer.
 * <p>
 * This class is a partial implementation of such a decorator.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The message type of the upstream observable
 * @param <U>
 *          The message type of the downstream observer
 */
public abstract class PassthroughObserver<T, U> implements Observer<T> {
  private Observation observation;
  private final Supplier<Observer<? super U>> downstreamObserver;

  public PassthroughObserver(Observer<? super U> downstreamObserver) {
    this(() -> requireNonNull(downstreamObserver));
  }

  public PassthroughObserver(Supplier<Observer<? super U>> downstreamObserver) {
    this.downstreamObserver = downstreamObserver;
  }

  public Observer<? super U> getDownstreamObserver() {
    return downstreamObserver.get();
  }

  public Observation getObservation() {
    return observation;
  }

  @Override
  public abstract void onNext(T message);

  protected void initializeObservation(Observation observation) {
    this.observation = observation;
  }

  @Override
  public void onObserve(Observation observation) {
    initializeObservation(observation);
    getDownstreamObserver().onObserve(observation);
  }

  @Override
  public void onComplete() {
    getDownstreamObserver().onComplete();
  }

  @Override
  public void onFail(Throwable t) {
    getDownstreamObserver().onFail(t);
  }
}
