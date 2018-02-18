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

import org.junit.Test;

import mockit.FullVerificationsInOrder;
import mockit.Injectable;

@SuppressWarnings("javadoc")
public class PassthroughObserverTest {
  @Injectable
  Observation upstreamObservation;

  @Injectable
  Observer<String> downstreamObserver;

  protected PassthroughObserver<String, String> createDefaultObserver(
      Observer<String> downstreamObserver) {
    return new PassthroughObserver<String, String>(downstreamObserver) {
      @Override
      public void onNext(String message) {
        getDownstreamObserver().onNext(message);
      }
    };
  }

  @Test
  public void useObserverOnceTest() {
    Observer<String> test = createDefaultObserver(downstreamObserver);

    test.onObserve(upstreamObservation);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve(upstreamObservation);
      }
    };
  }

  @Test
  public void passSomeMessageEventTest() {
    Observer<String> test = createDefaultObserver(downstreamObserver);

    test.onObserve(upstreamObservation);
    test.onNext("message");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void passSomeMessagesEventTest() {
    Observer<String> test = createDefaultObserver(downstreamObserver);

    test.onObserve(upstreamObservation);
    test.onNext("message1");
    test.onNext("message2");
    test.onNext("message3");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message1");
        downstreamObserver.onNext("message2");
        downstreamObserver.onNext("message3");
      }
    };
  }

  @Test
  public void passCompleteEventTest() {
    Observer<String> test = createDefaultObserver(downstreamObserver);

    test.onObserve(upstreamObservation);
    test.onComplete();

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onComplete();
      }
    };
  }

  @Test
  public void passFailEventTest() {
    Observer<String> test = createDefaultObserver(downstreamObserver);

    Throwable t = new Throwable();

    test.onObserve(upstreamObservation);
    test.onFail(t);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onFail(t);
      }
    };
  }
}
