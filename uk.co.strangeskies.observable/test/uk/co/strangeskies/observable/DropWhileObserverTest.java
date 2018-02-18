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
public class DropWhileObserverTest {
  @Injectable
  Observation upstreamObservation;

  @Injectable
  Observer<String> downstreamObserver;

  @Test
  public void failSkipFilterMessageEventTest() {
    Observer<String> test = new DropWhileObserver<>(downstreamObserver, s -> false);

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
  public void passSkipFilterMessageEventTest() {
    Observer<String> test = new DropWhileObserver<>(downstreamObserver, s -> true);

    test.onObserve(upstreamObservation);
    test.onNext("message");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestNext();
      }
    };
  }

  @Test
  public void passThenFailSkipFilterMessageEventTest() {
    Observer<String> test = new DropWhileObserver<>(downstreamObserver, "pass"::equals);

    test.onObserve(upstreamObservation);
    test.onNext("pass");
    test.onNext("fail");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestNext();
        downstreamObserver.onNext("fail");
      }
    };
  }

  @Test
  public void failThenPassSkipFilterMessageEventTest() {
    Observer<String> test = new DropWhileObserver<>(downstreamObserver, "pass"::equals);

    test.onObserve(upstreamObservation);
    test.onNext("fail");
    test.onNext("pass");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("fail");
        downstreamObserver.onNext("pass");
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void nullSkipFilterTest() {
    new DropWhileObserver<>(downstreamObserver, null);
  }
}
