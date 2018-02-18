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

public class SynchronizedObserver<M> extends PassthroughObserver<M, M> {
  private Object mutex;

  public SynchronizedObserver(Observer<? super M> downstreamObserver, Object mutex) {
    super(downstreamObserver);

    this.mutex = requireNonNull(mutex);
  }

  @Override
  public void onNext(M message) {
    synchronized (mutex) {
      getDownstreamObserver().onNext(message);
    }
  }

  @Override
  public void onComplete() {
    synchronized (mutex) {
      super.onComplete();
    }
  }

  @Override
  public void onFail(Throwable t) {
    synchronized (mutex) {
      super.onFail(t);
    }
  }

  @Override
  public void onObserve(Observation observation) {
    synchronized (mutex) {
      super.onObserve(new Observation() {
        @Override
        public void cancel() {
          synchronized (mutex) {
            observation.cancel();
          }
        }

        @Override
        public void request(long count) {
          synchronized (mutex) {
            observation.request(count);
          }
        }

        @Override
        public long getPendingRequestCount() {
          synchronized (mutex) {
            return observation.getPendingRequestCount();
          }
        }
      });
    }
  }
}
