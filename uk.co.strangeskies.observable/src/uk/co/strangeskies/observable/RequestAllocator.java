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

import static java.util.Comparator.comparing;

import java.util.List;

@FunctionalInterface
public interface RequestAllocator {
  long allocateRequests(long requestCount, List<Observation> observations);

  static RequestAllocator balanced() {
    return (count, observations) -> {
      if (count == Long.MAX_VALUE) {
        observations.forEach(o -> o.request(Long.MAX_VALUE));

      } else {
        for (int i = 0; i < observations.size() && count > 0; i++) {
          Observation observation = observations.get(i);
          if (observation.getPendingRequestCount() == 0) {
            observation.requestNext();
            count--;
          }
        }
      }

      return count;
    };
  }

  static RequestAllocator sequential() {
    return (count, observations) -> {
      observations.get(0).request(count);
      return count == Long.MAX_VALUE ? Long.MAX_VALUE : 0;
    };
  }

  static RequestAllocator spread() {
    return (count, observations) -> {
      if (count == Long.MAX_VALUE) {
        observations.forEach(o -> o.request(Long.MAX_VALUE));
        return count;

      } else {
        observations.sort(comparing(Observation::getPendingRequestCount));

        int observationsUnderBaseline = observations.size();
        long pendingUnderBaseline = observations
            .stream()
            .mapToLong(Observation::getPendingRequestCount)
            .sum();

        long newRequestBaseline;
        do {
          newRequestBaseline = (pendingUnderBaseline + count) / observationsUnderBaseline;

          long maximumPendingRequests = observations
              .get(observationsUnderBaseline - 1)
              .getPendingRequestCount();

          if (maximumPendingRequests > newRequestBaseline) {
            pendingUnderBaseline -= maximumPendingRequests;
            observationsUnderBaseline--;
          } else {
            break;
          }
        } while (true);

        for (int i = 0; i < observationsUnderBaseline; i++) {
          Observation observation = observations.get(i);
          long fulfilled = newRequestBaseline - observation.getPendingRequestCount();
          observation.request(fulfilled);
          count -= fulfilled;
        }

        for (int i = 0; i < count; i++) {
          observations.get(i).requestNext();
        }

        return 0;
      }
    };
  }
}
