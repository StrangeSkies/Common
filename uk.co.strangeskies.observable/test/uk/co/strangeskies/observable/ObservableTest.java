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

import static org.hamcrest.core.IsInstanceOf.instanceOf;

import org.junit.Test;

import mockit.Injectable;
import mockit.Verifications;

@SuppressWarnings("javadoc")
public class ObservableTest {
  Observable<String> upstreamObservable = a -> null;

  @Injectable
  Observer<String> downstreamObserver;

  @Test
  public void thenTest() {
    upstreamObservable.then(m -> {});

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(MultiplePassthroughObserver.class)));
      }
    };
  }

  @Test
  public void thenAfterTest() {
    upstreamObservable.thenAfter(m -> {});

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(MultiplePassthroughObserver.class)));
      }
    };
  }

  @Test
  public void retryingTest() {
    upstreamObservable.retrying();

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(RetryingObserver.class)));
      }
    };
  }

  @Test
  public void softReferenceOwnedTest() {
    upstreamObservable.softReference(new Object());

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(ReferenceOwnedObserver.class)));
      }
    };
  }

  @Test
  public void softReferenceTest() {
    upstreamObservable.softReference();

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(ReferenceOwnedObserver.class)));
      }
    };
  }

  @Test
  public void weakReferenceOwnedTest() {
    upstreamObservable.weakReference(new Object());

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(ReferenceOwnedObserver.class)));
      }
    };
  }

  @Test
  public void weakReferenceTest() {
    upstreamObservable.weakReference();

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(ReferenceOwnedObserver.class)));
      }
    };
  }

  @Test
  public void executeOnTest() {
    upstreamObservable.executeOn(r -> {});

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(ExecutorObserver.class)));
      }
    };
  }

  @Test
  public void mapTest() {
    upstreamObservable.map(s -> s);

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(MappingObserver.class)));
      }
    };
  }

  @Test
  public void filterTest() {
    upstreamObservable.filter(s -> true);

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(FilteringObserver.class)));
      }
    };
  }

  @Test
  public void takeWhileTest() {
    upstreamObservable.takeWhile(s -> true);

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(TakeWhileObserver.class)));
      }
    };
  }

  @Test
  public void dropWhileTest() {
    upstreamObservable.dropWhile(s -> true);

    new Verifications() {
      {
        upstreamObservable.observe(withArgThat(instanceOf(DropWhileObserver.class)));
      }
    };
  }
}
