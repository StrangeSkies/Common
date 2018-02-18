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

public final class DoubleValue extends ContinuousValue<DoubleValue> {
  private static final long serialVersionUID = 5916143538083079342L;

  private double value;

  public DoubleValue() {}

  public DoubleValue(Value<?> value) {
    super((Number) value);
  }

  public DoubleValue(Number value) {
    super(value);
  }

  @Override
  public final DoubleValue reciprocate() {
    value = 1 / value;

    return this;
  }

  @Override
  public final DoubleValue add(Value<?> value) {
    this.value += value.doubleValue();
    update();
    return this;
  }

  @Override
  public final DoubleValue subtract(Value<?> value) {
    this.value -= value.doubleValue();
    update();
    return this;
  }

  @Override
  public final DoubleValue negate() {
    value = -value;
    update();
    return this;
  }

  @Override
  public final DoubleValue multiply(int value) {
    this.value *= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue multiply(long value) {
    this.value *= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue multiply(float value) {
    this.value *= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue multiply(double scalar) {
    this.value *= scalar;
    update();
    return this;
  }

  @Override
  public final DoubleValue divide(int value) {
    this.value /= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue divide(long value) {
    this.value /= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue divide(float value) {
    this.value /= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue divide(double value) {
    this.value /= value;
    update();
    return this;
  }

  @Override
  public final DoubleValue divide(Value<?> value) {
    this.value = value.getDividedPrimitive(this.value);
    update();
    return this;
  }

  @Override
  public final double doubleValue() {
    return value;
  }

  @Override
  public final float floatValue() {
    return (float) value;
  }

  @Override
  public final int intValue() {
    return (int) value;
  }

  @Override
  public final long longValue() {
    return (long) value;
  }

  @Override
  public final String toString() {
    return new Double(value).toString();
  }

  @Override
  public final DoubleValue setValue(Number value) {
    this.value = value.doubleValue();
    update();
    return this;
  }

  @Override
  public final DoubleValue multiply(Value<?> value) {
    this.value = value.getMultipliedPrimitive(this.value);
    update();
    return this;
  }

  @Override
  public final int compareToAtSupportedPrecision(Value<?> other) {
    return new Double(this.value).compareTo(new Double(other.doubleValue()));
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
    return new Double(value).hashCode();
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
  public final DoubleValue increment() {
    value++;
    update();
    return this;
  }

  @Override
  public final DoubleValue decrement() {
    value--;
    update();
    return this;
  }

  @Override
  public final DoubleValue copy() {
    return new DoubleValue(this);
  }

  @Override
  public final DoubleValue unitInTheLastPlaceAbove() {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return this;
    }
    double absoluteValue = Math.abs(value);

    long nextValueLong = Double.doubleToLongBits(absoluteValue) + 1;
    double nextValue = Double.longBitsToDouble(nextValueLong);

    // if ended on bad number go down instead
    if (Double.isNaN(nextValue) || Double.isInfinite(nextValue)) {
      nextValueLong = nextValueLong - 2;
      nextValue = absoluteValue;
      absoluteValue = Double.longBitsToDouble(nextValueLong);
    }

    return new DoubleValue(Math.abs(nextValue - absoluteValue));
  }

  @Override
  public final DoubleValue unitInTheLastPlaceBelow() {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return this;
    }
    double absoluteValue = Math.abs(value);

    long nextValueLong = Double.doubleToLongBits(absoluteValue) - 1;
    double nextValue = Double.longBitsToDouble(nextValueLong);

    // if ended on bad number go up instead
    if (Double.isNaN(nextValue) || Double.isInfinite(nextValue)) {
      nextValueLong = nextValueLong + 2;
      nextValue = absoluteValue;
      absoluteValue = Double.longBitsToDouble(nextValueLong);
    }

    return new DoubleValue(Math.abs(nextValue - absoluteValue));
  }

  @Override
  public final int getMultipliedPrimitive(int value) {
    return (int) (this.value * value);
  }

  @Override
  public final long getMultipliedPrimitive(long value) {
    return (long) (this.value * value);
  }

  @Override
  public final float getMultipliedPrimitive(float value) {
    return (float) (this.value * value);
  }

  @Override
  public final double getMultipliedPrimitive(double value) {
    return this.value * value;
  }

  @Override
  public final int getDividedPrimitive(int value) {
    return (int) (this.value * value);
  }

  @Override
  public final long getDividedPrimitive(long value) {
    return (long) (this.value * value);
  }

  @Override
  public final float getDividedPrimitive(float value) {
    return (float) (this.value * value);
  }

  @Override
  public final double getDividedPrimitive(double value) {
    return this.value * value;
  }

  @Override
  public DoubleValue square() {
    value *= value;
    update();
    return this;
  }

  @Override
  public DoubleValue squareRoot() {
    value = (int) Math.sqrt(value);
    update();
    return this;
  }

  @Override
  public DoubleValue exponentiate(Value<?> exponential) {
    value = (int) Math.pow(value, exponential.doubleValue());
    update();
    return this;
  }

  @Override
  public DoubleValue root(Value<?> root) {
    value = (int) Math.pow(value, root.reciprocate().doubleValue());
    update();
    return this;
  }

  @Override
  public DoubleValue modulus() {
    value = Math.abs(value);
    return this;
  }
}
