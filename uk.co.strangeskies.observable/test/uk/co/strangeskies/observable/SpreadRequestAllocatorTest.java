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
import static uk.co.strangeskies.observable.RequestAllocator.spread;

import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.VerificationsInOrder;

@SuppressWarnings("javadoc")
public class SpreadRequestAllocatorTest {
  private final RequestAllocator spread = spread();

  @Injectable
  Observation firstObservation;
  @Injectable
  Observation secondObservation;
  @Injectable
  Observation thirdObservation;

  @Test
  public void allocateUnbounded() {
    long remaining = spread
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
    long remaining = spread
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.request(1);
        secondObservation.request(1);
        thirdObservation.request(1);
      }
    };
  }

  @Test
  public void allocateWhenAllArePendingEvenlyTest() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 5;
        secondObservation.getPendingRequestCount();
        result = 5;
        thirdObservation.getPendingRequestCount();
        result = 5;
      }
    };

    long remaining = spread
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.request(1);
        secondObservation.request(1);
        thirdObservation.request(1);
      }
    };
  }

  @Test
  public void allocateFewerRequestsThanObservations() {
    long remaining = spread
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
    long remaining = spread
        .allocateRequests(4, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.request(1);
        secondObservation.request(1);
        thirdObservation.request(1);
        firstObservation.requestNext();
      }
    };
  }

  @Test
  public void allocateUpToTheGreatestNumberOfPendingRequests() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 2;
        secondObservation.getPendingRequestCount();
        result = 1;
        thirdObservation.getPendingRequestCount();
        result = 0;
      }
    };

    long remaining = spread
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.request(anyInt);
        maxTimes = 0;
        firstObservation.requestNext();
        maxTimes = 0;
        thirdObservation.request(2);
        secondObservation.request(1);
      }
    };
  }

  @Test
  public void allocateUpToBelowTheGreatestNumberOfPendingRequests() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 3;
        secondObservation.getPendingRequestCount();
        result = 1;
        thirdObservation.getPendingRequestCount();
        result = 0;
      }
    };

    long remaining = spread
        .allocateRequests(3, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        firstObservation.request(anyInt);
        maxTimes = 0;
        firstObservation.requestNext();
        maxTimes = 0;
        thirdObservation.request(2);
        secondObservation.request(1);
      }
    };
  }

  @Test
  public void allocateUpToAboveTheGreatestNumberOfPendingRequests() {
    new Expectations() {
      {
        firstObservation.getPendingRequestCount();
        result = 2;
        secondObservation.getPendingRequestCount();
        result = 1;
        thirdObservation.getPendingRequestCount();
        result = 0;
      }
    };

    long remaining = spread
        .allocateRequests(8, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new VerificationsInOrder() {
      {
        thirdObservation.request(3);
        secondObservation.request(2);
        firstObservation.request(1);
        thirdObservation.requestNext();
        secondObservation.requestNext();
      }
    };
  }
}
