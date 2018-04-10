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

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.VerificationsInOrder;

@SuppressWarnings("javadoc")
public class BackpressureReducingObserverTest {
  @Injectable
  Observation upstreamObservation;

  @Injectable
  Observer<String> downstreamObserver;

  @Injectable
  Supplier<String> identity;

  @Injectable
  Function<String, String> initial;

  @Injectable
  BiFunction<String, String, String> accumulator;

  @Test
  public void accumulateFromIdentityAndSingleMessage() {
    new Expectations() {
      {
        identity.get();
        result = "identity";
      }
    };

    Observer<String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        identity,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onNext("message");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        identity.get();
        accumulator.apply("identity", "message");
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void accumulateFromIdentityAndTwoMessages() {
    new Expectations() {
      {
        identity.get();
        result = "identity";
      }
    };

    Observer<String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        identity,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onNext("message1");
    test.onNext("message2");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        identity.get();
        accumulator.apply("identity", "message1");
        accumulator.apply("identity", "message2");
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void accumulateFromIdentityAndSingleMessageThenComplete() {
    new Expectations() {
      {
        identity.get();
        result = "identity";
        accumulator.apply(anyString, anyString);
        result = "accumulation";
      }
    };

    Observer<String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        identity,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onNext("message");
    test.onComplete();

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        identity.get();
        accumulator.apply("identity", "message");
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void accumulateFromInitialMessageAndSingleMessage() {
    new Expectations() {
      {
        initial.apply(anyString);
        result = "initial";
        accumulator.apply(anyString, anyString);
        result = "accumulation";
      }
    };

    Observer<String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        initial,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onNext("message1");
    test.onNext("message2");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        initial.apply("message1");
        accumulator.apply("initial", "message2");
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void immediatelyComplete() {
    Observer<String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        identity,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onComplete();

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        downstreamObserver.onComplete();
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void requestNextFromIdentity() {
    PassthroughObserver<String, String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        identity,
        accumulator);

    test.onObserve(upstreamObservation);
    test.getObservation().requestNext();

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void requestNextFromMissingInitialMessage() {
    PassthroughObserver<String, String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        initial,
        accumulator);

    test.onObserve(upstreamObservation);
    test.getObservation().requestNext();

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
      }
    };
    new FullVerifications() {};
  }

  @Test
  public void requestNextFromInitialMessage() {
    new Expectations() {
      {
        initial.apply(anyString);
        result = "initial";
      }
    };

    PassthroughObserver<String, String> test = new BackpressureReducingObserver<>(
        downstreamObserver,
        initial,
        accumulator);

    test.onObserve(upstreamObservation);
    test.onNext("message");
    test.getObservation().requestNext();

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        upstreamObservation.requestUnbounded();
        initial.apply("message");
        downstreamObserver.onNext("initial");
      }
    };
    new FullVerifications() {};
  }

  @Test(expected = NullPointerException.class)
  public void nullAccumulaterWithIdentityTest() {
    new BackpressureReducingObserver<>(downstreamObserver, () -> null, null);
  }

  @Test(expected = NullPointerException.class)
  public void nullAccumulaterWithInitialTest() {
    new BackpressureReducingObserver<>(downstreamObserver, a -> null, null);
  }

  @Test(expected = NullPointerException.class)
  public void nullIdentityTest() {
    new BackpressureReducingObserver<>(downstreamObserver, (Supplier<String>) null, (a, b) -> null);
  }

  @Test(expected = NullPointerException.class)
  public void nullInitialTest() {
    new BackpressureReducingObserver<>(
        downstreamObserver,
        (Function<String, String>) null,
        (a, b) -> null);
  }
}
