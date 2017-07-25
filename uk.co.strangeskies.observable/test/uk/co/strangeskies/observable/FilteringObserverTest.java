/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
import mockit.Mocked;

@SuppressWarnings("javadoc")
public class FilteringObserverTest {
  @Mocked
  Observation upstreamObservation;

  @Mocked
  Observer<String> downstreamObserver;

  @Test
  public void filterFailMessageEventTest() {
    Observer<String> test = new FilteringObserver<>(downstreamObserver, s -> false);

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
  public void filterPassMessageEventTest() {
    Observer<String> test = new FilteringObserver<>(downstreamObserver, s -> true);

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
  public void filterMultipleMessageEventsTest() {
    Observer<String> test = new FilteringObserver<>(downstreamObserver, s -> s.startsWith("t"));

    test.onObserve(upstreamObservation);
    test.onNext("one");
    test.onNext("two");
    test.onNext("three");
    test.onNext("four");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestNext();
        downstreamObserver.onNext("two");
        downstreamObserver.onNext("three");
        upstreamObservation.requestNext();
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void nullFilterTest() {
    new FilteringObserver<>(downstreamObserver, null);
  }
}
