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

import static org.junit.Assert.assertNull;

import java.lang.ref.WeakReference;

import org.junit.Test;

import mockit.FullVerifications;
import mockit.Injectable;
import mockit.VerificationsInOrder;

@SuppressWarnings("javadoc")
public class ReferenceOwnedObserverTest {
  @Injectable
  Observation upstreamObservation;

  @Injectable
  Observer<OwnedMessage<Object, String>> downstreamObserver;

  @Test(timeout = 5000)
  public void weakReferenceTest() {
    WeakReference<?> reference = new WeakReference<>(new Object());

    while (reference.get() != null) {
      new Object();
      System.gc();
      System.runFinalization();
    }

    assertNull(reference.get());
  }

  private Observer<OwnedMessage<Object, String>> wrapDownstreamObserver() {
    return new Observer<OwnedMessage<Object, String>>() {
      @Override
      public void onObserve(Observation observation) {
        downstreamObserver.onObserve(observation);
      }

      @Override
      public void onNext(OwnedMessage<Object, String> message) {
        String messageContent = message.message();
        downstreamObserver.onNext(new OwnedMessage<Object, String>() {
          @Override
          public Object owner() {
            return null;
          }

          @Override
          public String message() {
            return messageContent;
          }
        });
      }

      @Override
      public void onComplete() {
        downstreamObserver.onComplete();
      }

      @Override
      public void onFail(Throwable t) {
        downstreamObserver.onFail(t);
      }
    };
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = 5000)
  public void holdWeakOwnedObserverThenMessageTest() {
    Object owner = new Object();

    Observer<OwnedMessage<Object, String>> downstreamObserverWrapper = wrapDownstreamObserver();
    Observer<String> test = ReferenceOwnedObserver.weak(owner, downstreamObserverWrapper);

    test.onObserve(upstreamObservation);
    weakReferenceTest();
    test.onNext("message");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve(upstreamObservation);
        downstreamObserver.onNext((OwnedMessage<Object, String>) any);
      }
    };
    new FullVerifications() {};
  }

  @SuppressWarnings("unchecked")
  @Test(timeout = 5000)
  public void dropWeakOwnedObserverThenMessageTest() {
    Object owner = new Object();

    Observer<OwnedMessage<Object, String>> downstreamObserverWrapper = wrapDownstreamObserver();
    Observer<String> test = ReferenceOwnedObserver.weak(owner, downstreamObserverWrapper);

    test.onObserve(upstreamObservation);
    test.onNext("message1");
    owner = null;
    weakReferenceTest();
    test.onNext("message2");

    new VerificationsInOrder() {
      {
        downstreamObserver.onObserve(upstreamObservation);
        downstreamObserver.onNext((OwnedMessage<Object, String>) any);
        upstreamObservation.cancel();
      }
    };
    new FullVerifications() {};
  }
}
