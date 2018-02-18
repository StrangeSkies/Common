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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utility;

import static uk.co.strangeskies.utility.Enumeration.getConstants;
import static uk.co.strangeskies.utility.Enumeration.readableName;
import static uk.co.strangeskies.utility.Enumeration.valueOf;
import static uk.co.strangeskies.utility.Enumeration.valueOfEnum;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

/**
 * Careful design is necessary here, as in some cases we are testing what
 * happens during class initialization, so extra thought must be put into test
 * isolation
 * 
 * @author Elias N Vasylenko
 *
 */
@SuppressWarnings("javadoc")
public class EnumerationTest {
  /**
   * Confirm that {@link Enumeration}s are properly initialized when accessed
   * first by literal, rather than via {@link Enumeration#getConstants(Class)}.
   * This is significant because of the odd initialization logic to enforce
   * instantiation only inside static initializers.
   */
  @Test
  public void testEnumLiteralAccess() {
    Assert.assertEquals("first", NamedEnum.FIRST.name());
    Assert.assertEquals("second", NamedEnum.SECOND.name());
    Assert.assertEquals("third", NamedEnum.THIRD.name());
  }

  /**
   * Confirm that an {@link Enumeration} works as an inner class (implementation
   * may perform stack trace examination).
   */
  @Test
  public void testInnerEnum() {
    Assert.assertEquals(1, Enumeration.getConstants(InnerEnum.class).size());
  }

  /**
   * Confirm that an {@link Enumeration} contains the expected number of members.
   */
  @Test
  public void testEnumMemberCount() {
    Assert.assertEquals(4, Enumeration.getConstants(PopulatedEnum.class).size());
  }

  /**
   * Confirm that the members of {@link Enumeration} are available in instantiated
   * order.
   */
  @Test
  public void testEnumMemberOrder() {
    for (int i = 0; i < Enumeration.getConstants(NumberedEnum.class).size(); i++) {
      Assert.assertEquals(i, Enumeration.getConstants(NumberedEnum.class).get(i).getInstance());
    }
  }

  /**
   * Confirm that attempts to instantiate instances of an Enumeration after the
   * static initializer has completed throw the appropriate exception.
   */
  @Test(expected = IllegalStateException.class)
  public void testPostStaticInitialiserFailure() {
    class EmptyEnum extends Enumeration<EmptyEnum> {
      public EmptyEnum() {
        super("");
      }
    }

    new EmptyEnum();
  }

  /**
   * Confirm that attempts to instantiate instances of an Enumeration after the
   * static initializer has completed do not add a constant in an incomplete
   * state.
   */
  @Test
  public void testPostStaticInitialiserImmutability() {
    class EmptyEnum extends Enumeration<EmptyEnum> {
      public EmptyEnum() {
        super("");
      }
    }

    try {
      new EmptyEnum();
    } catch (IllegalStateException e) {
      // we test this failure in #testPostStaticInitialiserFailure
    }
    Assert.assertEquals(0, getConstants(EmptyEnum.class).size());
  }

  @Test
  public void testEquality() {
    for (PopulatedEnum first : getConstants(PopulatedEnum.class)) {
      for (PopulatedEnum second : getConstants(PopulatedEnum.class)) {
        Assert.assertEquals(first == second, first.equals(second));
      }
    }
  }

  @Test
  public void testHashCode() {
    for (PopulatedEnum first : getConstants(PopulatedEnum.class)) {
      for (PopulatedEnum second : getConstants(PopulatedEnum.class)) {
        Assert.assertEquals(first == second, first.hashCode() == second.hashCode());
      }
    }
  }

  @Test
  public void testOrdinal() {
    Assert.assertEquals(0, PopulatedEnum.INSTANCE0.ordinal());
    Assert.assertEquals(1, PopulatedEnum.INSTANCE1.ordinal());
    Assert.assertEquals(2, PopulatedEnum.INSTANCE2.ordinal());
    Assert.assertEquals(3, PopulatedEnum.INSTANCE3.ordinal());
  }

  @Test
  public void testToString() {
    Assert.assertEquals("Instance0", PopulatedEnum.INSTANCE0.toString());
    Assert.assertEquals("Instance1", PopulatedEnum.INSTANCE1.toString());
    Assert.assertEquals("Instance2", PopulatedEnum.INSTANCE2.toString());
    Assert.assertEquals("Instance3", PopulatedEnum.INSTANCE3.toString());
  }

  @Test
  public void testValueOf() {
    Assert.assertEquals(PopulatedEnum.INSTANCE0, valueOf(PopulatedEnum.class, "Instance0"));
    Assert.assertEquals(PopulatedEnum.INSTANCE1, valueOf(PopulatedEnum.class, "Instance1"));
    Assert.assertEquals(PopulatedEnum.INSTANCE2, valueOf(PopulatedEnum.class, "Instance2"));
    Assert.assertEquals(PopulatedEnum.INSTANCE3, valueOf(PopulatedEnum.class, "Instance3"));
  }

  @Test(expected = NoSuchElementException.class)
  public void testValueOfMissing() {
    valueOf(PopulatedEnum.class, "Instance4");
  }

  @Test
  public void testValueOfEnum() {
    Assert.assertEquals(TraditionalEnum.INSTANCE0, valueOfEnum(TraditionalEnum.class, "INSTANCE0"));
    Assert.assertEquals(TraditionalEnum.INSTANCE1, valueOfEnum(TraditionalEnum.class, "INSTANCE1"));
    Assert.assertEquals(TraditionalEnum.INSTANCE2, valueOfEnum(TraditionalEnum.class, "INSTANCE2"));
    Assert.assertEquals(TraditionalEnum.INSTANCE3, valueOfEnum(TraditionalEnum.class, "INSTANCE3"));
  }

  @Test
  public void testReadableEnumNames() {
    Assert.assertEquals("Instance One", readableName(TraditionalEnumNames.INSTANCE_ONE));
    Assert.assertEquals("A Second Instance", readableName(TraditionalEnumNames.A_SECOND_INSTANCE));
    Assert.assertEquals("Instance Number 3", readableName(TraditionalEnumNames.INSTANCE_NUMBER_3));
    Assert.assertEquals("This Instance Too", readableName(TraditionalEnumNames.this_instance_too));
  }

  @Test
  public void testNext() {
    Assert.assertEquals(PopulatedEnum.INSTANCE1, PopulatedEnum.INSTANCE0.next());
    Assert.assertEquals(PopulatedEnum.INSTANCE2, PopulatedEnum.INSTANCE1.next());
    Assert.assertEquals(PopulatedEnum.INSTANCE3, PopulatedEnum.INSTANCE2.next());
    Assert.assertEquals(PopulatedEnum.INSTANCE0, PopulatedEnum.INSTANCE3.next());
  }

  @Test
  public void testEnumNext() {
    Assert.assertEquals(TraditionalEnum.INSTANCE1, Enumeration.next(TraditionalEnum.INSTANCE0));
    Assert.assertEquals(TraditionalEnum.INSTANCE2, Enumeration.next(TraditionalEnum.INSTANCE1));
    Assert.assertEquals(TraditionalEnum.INSTANCE3, Enumeration.next(TraditionalEnum.INSTANCE2));
    Assert.assertEquals(TraditionalEnum.INSTANCE0, Enumeration.next(TraditionalEnum.INSTANCE3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueOfEnumMissing() {
    TraditionalEnum.valueOf("INSTANCE4");
  }

  /**
   * Confirm that attempts to instantiate multiple instances of an Enumeration
   * with the same name will fail.
   */
  @Test(expected = ExceptionInInitializerError.class)
  public void testUniqueNames() {
    getConstants(UniqueEnum.class);
  }

  static class InnerEnum extends Enumeration<InnerEnum> {
    public static final InnerEnum ELEMENT = new InnerEnum("test");

    public InnerEnum(String name) {
      super(name);
    }
  }
}

enum TraditionalEnumNames {
  INSTANCE_ONE, A_SECOND_INSTANCE, INSTANCE_NUMBER_3, this_instance_too
}

enum TraditionalEnum {
  INSTANCE0, INSTANCE1, INSTANCE2, INSTANCE3
}

class UniqueEnum extends Enumeration<UniqueEnum> {
  public static final UniqueEnum FIRST = new UniqueEnum();
  public static final UniqueEnum SECOND = new UniqueEnum();

  public UniqueEnum() {
    super("Unique");
  }
}

class PopulatedEnum extends Enumeration<PopulatedEnum> {
  private static int counter = 0;

  public static final PopulatedEnum INSTANCE0 = new PopulatedEnum();
  public static final PopulatedEnum INSTANCE1 = new PopulatedEnum();
  public static final PopulatedEnum INSTANCE2 = new PopulatedEnum();
  public static final PopulatedEnum INSTANCE3 = new PopulatedEnum();

  private PopulatedEnum() {
    super("Instance" + counter++);
  }
}

class NumberedEnum extends Enumeration<NumberedEnum> {
  private static int counter = 0;

  public static final NumberedEnum INSTANCE0 = new NumberedEnum();
  public static final NumberedEnum INSTANCE1 = new NumberedEnum();
  public static final NumberedEnum INSTANCE2 = new NumberedEnum();

  private final int instance;

  private NumberedEnum() {
    super(Objects.toString(counter));

    this.instance = counter++;
  }

  public int getInstance() {
    return instance;
  }
}

class NamedEnum extends Enumeration<NamedEnum> {
  public static final NamedEnum FIRST = new NamedEnum("first");
  public static final NamedEnum SECOND = new NamedEnum("second");
  public static final NamedEnum THIRD = new NamedEnum("third");

  private NamedEnum(String name) {
    super(name);
  }
}
