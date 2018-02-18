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

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple implementation of {@link Observable} which implements backpressure
 * over the elements of an iterable.
 * <p>
 * The implementation is intentionally basic, with messages being pushed to the
 * observer on the same thread which makes the {@link Observation#request(long)
 * request} by default. An executor can be added downstream.
 * 
 * @author Elias N Vasylenko
 * @param <M>
 *          The type of event message to produce
 */
public class ColdObservable<M> implements Observable<M> {
  private final Iterable<? extends M> iterable;

  public ColdObservable(Iterable<? extends M> iterable) {
    this.iterable = iterable;
  }

  @Override
  public synchronized Disposable observe(Observer<? super M> observer) {
    return new ColdObservation<>(iterable, observer);
  }

  static class ColdObservation<M> extends ObservationImpl<M> {
    private final Iterator<? extends M> iterator;
    private final AtomicLong totalCount = new AtomicLong();

    ColdObservation(Iterable<? extends M> iterable, Observer<? super M> observer) {
      super(observer);
      this.iterator = iterable.iterator();
      onObserve();
      tryContinue();
    }

    @Override
    public void request(long count) {
      if (count < 0) {
        throw new IllegalArgumentException();
      }

      totalCount.addAndGet(count);

      if (count == Long.MAX_VALUE) {
        requestUnbounded();
        return;
      }

      while (count-- > 0 && tryNext()) {}
    }

    @Override
    public void requestUnbounded() {
      totalCount.set(Long.MAX_VALUE);
      while (tryNext()) {}
    }

    private synchronized boolean tryNext() {
      if (tryContinue()) {
        if (totalCount.get() < Long.MAX_VALUE)
          totalCount.decrementAndGet();
        onNext(iterator.next());
        tryContinue();
        return true;
      }
      return false;
    }

    private boolean tryContinue() {
      if (!isDisposed() && !iterator.hasNext()) {
        onComplete();
        cancel();
        return false;
      }
      return true;
    }

    @Override
    public long getPendingRequestCount() {
      return totalCount.get();
    }

    @Override
    protected void cancelImpl() {}
  }
}
