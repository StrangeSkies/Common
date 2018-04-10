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
import static uk.co.strangeskies.observable.RequestAllocator.sequential;

import org.junit.Test;

import mockit.FullVerifications;
import mockit.Injectable;

@SuppressWarnings("javadoc")
public class SequentialRequestAllocatorTest {
  private final RequestAllocator sequential = sequential();

  @Injectable
  Observation firstObservation;
  @Injectable
  Observation secondObservation;
  @Injectable
  Observation thirdObservation;

  @Test
  public void allocateUnbounded() {
    long remaining = sequential
        .allocateRequests(
            Long.MAX_VALUE,
            asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(Long.MAX_VALUE));

    new FullVerifications() {
      {
        firstObservation.request(Long.MAX_VALUE);
      }
    };
  }

  @Test
  public void allocateRequests() {
    long remaining = sequential
        .allocateRequests(10l, asList(firstObservation, secondObservation, thirdObservation));

    assertThat(remaining, equalTo(0l));

    new FullVerifications() {
      {
        firstObservation.request(10l);
      }
    };
  }
}
