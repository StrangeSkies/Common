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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import mockit.FullVerificationsInOrder;
import mockit.Injectable;

@SuppressWarnings("javadoc")
public class HotObservableTest {
  @Injectable
  Observer<String> downstreamObserver;

  @Test
  public void observeTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
      }
    };
  }

  @Test
  public void isLiveAfterObserveTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.assertLive();
  }

  @Test(expected = IllegalStateException.class)
  public void startWhenLiveTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.start();
  }

  @Test
  public void startWhenDeadTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();
    observable.start();
  }

  @Test
  public void isLiveAfterStartTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();
    observable.start();
    observable.assertLive();
  }

  @Test
  public void isLiveAfterInstantiationTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.assertLive();
  }

  @Test
  public void isDeadAfterCompleteTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.complete();
    observable.assertDead();
  }

  @Test
  public void isDeadAfterFailTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.fail(new Throwable());
    observable.assertDead();
  }

  @Test(expected = IllegalStateException.class)
  public void messageWhenDeadTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();
    observable.next(null);
  }

  @Test(expected = IllegalStateException.class)
  public void completeWhenDeadTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();
    observable.complete();
  }

  @Test(expected = IllegalStateException.class)
  public void failWhenDeadTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();
    observable.fail(null);
  }

  @Test
  public void messageTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.next("message");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void completeTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.complete();

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onComplete();
      }
    };
  }

  @Test
  public void failTest() {
    Throwable t = new Throwable();

    HotObservable<String> observable = new HotObservable<>();
    observable.observe(downstreamObserver);
    observable.fail(t);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onFail(t);
      }
    };
  }

  @Test
  public void hasObserversTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe();
    assertThat(observable.hasObservers(), equalTo(true));
  }

  @Test
  public void hasNoObserversTest() {
    HotObservable<String> observable = new HotObservable<>();
    assertThat(observable.hasObservers(), equalTo(false));
  }

  @Test
  public void hasNoObserversAfterDiscardingOnlyObserverTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe().cancel();
    assertThat(observable.hasObservers(), equalTo(false));
  }

  @Test
  public void hasObserversAfterDiscardingOneOfTwoObserversTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe();
    observable.observe().cancel();
    assertThat(observable.hasObservers(), equalTo(true));
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullThrowableTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe();
    observable.fail(null);
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullMessageTest() {
    HotObservable<String> observable = new HotObservable<>();
    observable.observe();
    observable.next(null);
  }
}
