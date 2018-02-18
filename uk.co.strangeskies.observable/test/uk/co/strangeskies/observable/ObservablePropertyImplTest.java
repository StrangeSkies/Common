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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import mockit.FullVerificationsInOrder;
import mockit.Injectable;
import uk.co.strangeskies.observable.ObservableValue.Change;

@SuppressWarnings("javadoc")
public class ObservablePropertyImplTest {
  @Injectable
  Observer<String> downstreamObserver;
  @Injectable
  Observer<Change<String>> changeObserver;

  @Test
  public void getInitialValueTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    assertThat(property.get(), equalTo("initial"));
  }

  @Test
  public void getInitialValueMultipleTimesTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    assertThat(property.get(), equalTo("initial"));
    assertThat(property.get(), equalTo("initial"));
  }

  @Test
  public void initialValueMessageOnSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
      }
    };
  }

  @Test
  public void initialValueMessageOnSubscribeMultipleTimesTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver).cancel();
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
      }
    };
  }

  @Test
  public void setValueMessageAfterSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.observe(downstreamObserver);
    property.set("message");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void setValueMessageBeforeSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");

    property.set("message");
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("message");
      }
    };
  }

  @Test
  public void setProblemEventAfterSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.observe(downstreamObserver);
    property.setProblem(problem);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onFail(problem);
      }
    };
  }

  @Test
  public void setProblemEventBeforeSubscribeTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.observe(downstreamObserver);

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onFail(problem);
      }
    };
  }

  @Test
  public void setValueEventAfterProblemEventTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.observe(downstreamObserver);
    property.setProblem(problem);
    property.set("ignore");

    new FullVerificationsInOrder() {
      {
        downstreamObserver.onObserve((Observation) any);
        downstreamObserver.onNext("initial");
        downstreamObserver.onFail(problem);
      }
    };
  }

  @Test(expected = MissingValueException.class)
  public void setProblemEventThenGetTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.get();
  }

  @Test
  public void clearProblemEventThenGetTest() {
    ObservableProperty<String> property = new ObservablePropertyImpl<>("initial");
    Throwable problem = new Throwable();

    property.setProblem(problem);
    property.set("message");
    assertThat(property.get(), equalTo("message"));
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullThrowableTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.observe();
    observable.setProblem(null);
  }

  @Test(expected = NullPointerException.class)
  public void failWithNullMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.observe();
    observable.set(null);
  }

  @Test
  public void noChangesOnObserveTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);

    new FullVerificationsInOrder() {
      {
        changeObserver.onObserve((Observation) any);
      }
    };
  }

  @Test
  public void changeInitialToNextMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.set("message");

    new FullVerificationsInOrder() {
      {
        changeObserver.onObserve((Observation) any);
        Change<String> change;
        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue().get(), equalTo("initial"));
        assertThat(change.newValue().get(), equalTo("message"));
      }
    };
  }

  @Test
  public void changeInitialToProblemTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());

    new FullVerificationsInOrder() {
      {
        changeObserver.onObserve((Observation) any);
        Change<String> change;
        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue().get(), equalTo("initial"));
        assertFalse(change.newValue().isValid());
      }
    };
  }

  @Test
  public void changeInitialToEqualValueTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.set("initial");

    new FullVerificationsInOrder() {
      {
        changeObserver.onObserve((Observation) any);
      }
    };
  }

  @Test
  public void changeProblemToNextMessageTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());
    observable.set("message");

    new FullVerificationsInOrder() {
      {
        Change<String> change;
        changeObserver.onObserve((Observation) any);

        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue().get(), equalTo("initial"));
        assertFalse(change.newValue().isValid());

        changeObserver.onNext(change = withCapture());
        assertFalse(change.previousValue().isValid());
        assertThat(change.newValue().get(), equalTo("message"));
      }
    };
  }

  @Test
  public void changeProblemToProblemTest() {
    ObservablePropertyImpl<String> observable = new ObservablePropertyImpl<>("initial");
    observable.changes().observe(changeObserver);
    observable.setProblem(new Throwable());
    observable.setProblem(new Throwable());

    new FullVerificationsInOrder() {
      {
        Change<String> change;
        changeObserver.onObserve((Observation) any);

        changeObserver.onNext(change = withCapture());
        assertThat(change.previousValue().get(), equalTo("initial"));
        assertFalse(change.newValue().isValid());

        changeObserver.onNext(change = withCapture());
        assertFalse(change.previousValue().isValid());
        assertFalse(change.newValue().isValid());
      }
    };
  }
}
