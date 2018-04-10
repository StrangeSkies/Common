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
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values;

public final class IntValue extends IntegralValue<IntValue> {
  private static final long serialVersionUID = 5916143538083079342L;

  private int value;

  public IntValue() {}

  public IntValue(Value<?> value) {
    super(value);
  }

  public IntValue(Number value) {
    super(value);
  }

  @Override
  public synchronized final IntValue reciprocate() {
    value = 1 / value;
    return this;
  }

  @Override
  public synchronized final IntValue add(Value<?> value) {
    this.value += value.intValue();
    return this;
  }

  @Override
  public synchronized final IntValue negate() {
    value = -value;
    return this;
  }

  @Override
  public synchronized final IntValue multiply(int value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final IntValue multiply(long value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final IntValue multiply(float value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final IntValue multiply(double value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final IntValue multiply(Value<?> value) {
    this.value = value.getMultipliedPrimitive(this.value);
    return this;
  }

  @Override
  public synchronized final IntValue divide(int value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final IntValue divide(long value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final IntValue divide(float value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final IntValue divide(double value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final IntValue divide(Value<?> value) {
    this.value = value.getDividedPrimitive(this.value);
    return this;
  }

  @Override
  public synchronized final IntValue subtract(Value<?> value) {
    this.value -= value.intValue();
    return this;
  }

  @Override
  public final double doubleValue() {
    return value;
  }

  @Override
  public final float floatValue() {
    return value;
  }

  @Override
  public final int intValue() {
    return value;
  }

  @Override
  public final long longValue() {
    return value;
  }

  @Override
  public final String toString() {
    return Integer.toString(value);
  }

  @Override
  public synchronized final IntValue setValue(Number value) {
    this.value = value.intValue();
    return this;
  }

  @Override
  public final int compareToAtSupportedPrecision(Value<?> other) {
    return Integer.compare(this.value, other.intValue());
  }

  @Override
  public final boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that instanceof Value<?>) {
      return equals((Value<?>) that);
    }
    if (that instanceof Number) {
      return ((Number) that).equals(this.value);
    }
    return false;
  }

  @Override
  protected final boolean equals(Value<?> that) {
    return ((Value<?>) that).equals(this.value);
  }

  @Override
  public final int hashCode() {
    return Integer.hashCode(value);
  }

  @Override
  public final boolean equals(double value) {
    return this.value == value;
  }

  @Override
  public final boolean equals(float value) {
    return this.value == value;
  }

  @Override
  public final boolean equals(int value) {
    return this.value == value;
  }

  @Override
  public final boolean equals(long value) {
    return this.value == value;
  }

  @Override
  public synchronized final IntValue increment() {
    value++;
    return this;
  }

  @Override
  public synchronized final IntValue decrement() {
    value--;
    return this;
  }

  @Override
  public final IntValue copy() {
    return new IntValue(this);
  }

  @Override
  public final IntValue unitInTheLastPlaceAbove() {
    return new IntValue(1);
  }

  @Override
  public final IntValue unitInTheLastPlaceBelow() {
    return new IntValue(1);
  }

  @Override
  public final int getMultipliedPrimitive(int value) {
    return this.value * value;
  }

  @Override
  public final long getMultipliedPrimitive(long value) {
    return this.value * value;
  }

  @Override
  public final float getMultipliedPrimitive(float value) {
    return this.value * value;
  }

  @Override
  public final double getMultipliedPrimitive(double value) {
    return this.value * value;
  }

  @Override
  public final int getDividedPrimitive(int value) {
    return this.value * value;
  }

  @Override
  public final long getDividedPrimitive(long value) {
    return this.value * value;
  }

  @Override
  public final float getDividedPrimitive(float value) {
    return this.value * value;
  }

  @Override
  public final double getDividedPrimitive(double value) {
    return this.value * value;
  }

  @Override
  public synchronized IntValue square() {
    value *= value;
    return this;
  }

  @Override
  public synchronized IntValue squareRoot() {
    value = (int) Math.sqrt(value);
    return this;
  }

  @Override
  public synchronized IntValue exponentiate(Value<?> exponential) {
    value = (int) Math.pow(value, exponential.doubleValue());
    return this;
  }

  @Override
  public IntValue root(Value<?> root) {
    return exponentiate(root.getReciprocal());
  }

  @Override
  public synchronized IntValue modulus() {
    value = Math.abs(value);
    return this;
  }
}
