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

public class RepeatingObserver<T> extends PassthroughObserver<T, T> {
  static class RetryingObservation implements Observation {
    private Observation upstreamObservation;
    private long pendingRequests;

    public RetryingObservation(Observation observation) {
      pendingRequests = observation.getPendingRequestCount();
    }

    public RetryingObservation setUpstreamObservation(Observation upstreamObservation) {
      this.upstreamObservation = upstreamObservation;
      upstreamObservation.request(pendingRequests - upstreamObservation.getPendingRequestCount());
      pendingRequests = upstreamObservation.getPendingRequestCount();
      return this;
    }

    @Override
    public void cancel() {
      upstreamObservation.cancel();
    }

    @Override
    public void request(long count) {
      pendingRequests += count;
      upstreamObservation.request(count);
    }

    public void fulfilRequest() {
      pendingRequests--;
    }

    @Override
    public long getPendingRequestCount() {
      return pendingRequests;
    }
  }

  private final Observable<? extends T> retryOn;

  public RepeatingObserver(
      Observer<? super T> downstreamObserver,
      Observable<? extends T> retryOn) {
    super(downstreamObserver);
    this.retryOn = requireNonNull(retryOn);
  }

  @Override
  public RetryingObservation getObservation() {
    return (RetryingObservation) super.getObservation();
  }

  @Override
  public void onObserve(Observation observation) {
    boolean firstObservation = getObservation() == null;

    if (firstObservation) {
      super.onObserve(new RetryingObservation(observation));
    } else {
      getObservation().setUpstreamObservation(observation);
    }
  }

  @Override
  public void onComplete() {
    retryOn.observe(this);
  }

  @Override
  public void onNext(T message) {
    getObservation().fulfilRequest();
    getDownstreamObserver().onNext(message);
  }
}
