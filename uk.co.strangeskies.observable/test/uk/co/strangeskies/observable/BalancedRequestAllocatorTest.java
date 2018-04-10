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

import static java.util.Arrays.asList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.observable.RequestAllocator.balanced;

import org.junit.Test;

import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.VerificationsInOrder;

@SuppressWarnings("javadoc")
public class BalancedRequestAllocatorTest {
  private final RequestAllocator balanced = balanced();

  @Injectable
  Observation firstObservation;
  @Injectable
  Observation secondObservation;
  @Injectable
  Observation thirdObservation;

  @Test
  public void allocateUnbounded() {
    long remaining = balanced
        .allocateRequests(
            Long.MAX_VALUE,
            asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(Long.MAX_VALUE));

    new VerificationsInOrder() {
      {
        firstObservation.request(Long.MAX_VALUE);
        secondObservation.request(Long.MAX_VALUE);
        thirdObservation.request(Long.MAX_VALUE);
      }
    };
  }

  @Test
  public void allocateWhenNoneArePendingTest() {
    long remaining = balanced
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.requestNext();
        secondObservation.requestNext();
        thirdObservation.requestNext();
      }
    };
  }

  @Test
  public void allocateWhenAllArePendingTest() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 1;
        secondObservation.getPendingRequestCount();
        result = 1;
        thirdObservation.getPendingRequestCount();
        result = 1;
      }
    };

    long remaining = balanced
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(3l));

    new VerificationsInOrder() {
      {
        firstObservation.requestNext();
        maxTimes = 0;
        secondObservation.requestNext();
        maxTimes = 0;
        thirdObservation.requestNext();
        maxTimes = 0;
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void allocateWhenSomeArePendingTest() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 1;
        secondObservation.getPendingRequestCount();
        result = 0;
      }
    };

    long remaining = balanced.allocateRequests(2, asList(firstObservation, secondObservation));

    assertThat(remaining, equalTo(1l));

    new VerificationsInOrder() {
      {
        firstObservation.requestNext();
        maxTimes = 0;
        secondObservation.requestNext();
      }
    };
  }

  @Test
  public void allocateFewerRequestsThanObservations() {
    long remaining = balanced
        .allocateRequests(2, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.requestNext();
        secondObservation.requestNext();
        thirdObservation.requestNext();
        maxTimes = 0;
      }
    };
  }

  @Test
  public void allocateMoreRequestsThanObservations() {
    long remaining = balanced
        .allocateRequests(4, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(1l));

    new VerificationsInOrder() {
      {
        firstObservation.requestNext();
        secondObservation.requestNext();
        thirdObservation.requestNext();
      }
    };
  }
}
