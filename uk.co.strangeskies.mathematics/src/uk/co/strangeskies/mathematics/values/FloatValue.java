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

public final class FloatValue extends ContinuousValue<FloatValue> {
  private static final long serialVersionUID = 5916143538083079342L;

  private float value;

  public FloatValue() {}

  public FloatValue(Value<?> value) {
    super(value);
  }

  public FloatValue(Number value) {
    super(value);
  }

  @Override
  public synchronized final FloatValue reciprocate() {
    value = 1 / value;

    return this;
  }

  @Override
  public synchronized final FloatValue add(Value<?> value) {
    this.value += value.floatValue();
    return this;
  }

  @Override
  public synchronized final FloatValue negate() {
    value = -value;
    return this;
  }

  @Override
  public synchronized final FloatValue multiply(int value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final FloatValue multiply(long value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final FloatValue multiply(float value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final FloatValue multiply(double value) {
    this.value *= value;
    return this;
  }

  @Override
  public synchronized final FloatValue multiply(Value<?> value) {
    this.value = value.getMultipliedPrimitive(this.value);
    return this;
  }

  @Override
  public synchronized final FloatValue divide(int value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final FloatValue divide(long value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final FloatValue divide(float value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final FloatValue divide(double value) {
    this.value /= value;
    return this;
  }

  @Override
  public synchronized final FloatValue divide(Value<?> value) {
    this.value = value.getDividedPrimitive(this.value);
    return this;
  }

  @Override
  public synchronized final FloatValue subtract(Value<?> value) {
    this.value -= value.floatValue();
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
    return (int) value;
  }

  @Override
  public final long longValue() {
    return (long) value;
  }

  @Override
  public final String toString() {
    return new Float(value).toString();
  }

  @Override
  public synchronized final FloatValue setValue(Number value) {
    this.value = value.floatValue();
    return this;
  }

  @Override
  public final int compareToAtSupportedPrecision(Value<?> other) {
    return new Float(this.value).compareTo(new Float(other.floatValue()));
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
    return new Float(value).hashCode();
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
  public synchronized final FloatValue increment() {
    value++;
    return this;
  }

  @Override
  public synchronized final FloatValue decrement() {
    value--;
    return this;
  }

  @Override
  public final FloatValue copy() {
    return new FloatValue(this);
  }

  @Override
  public final FloatValue unitInTheLastPlaceAbove() {
    return new FloatValue(1);
  }

  @Override
  public final FloatValue unitInTheLastPlaceBelow() {
    return new FloatValue(1);
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
    return this.value * value;
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
    return this.value * value;
  }

  @Override
  public final double getDividedPrimitive(double value) {
    return this.value * value;
  }

  @Override
  public synchronized FloatValue square() {
    value *= value;
    return this;
  }

  @Override
  public synchronized FloatValue squareRoot() {
    value = (int) Math.sqrt(value);
    return this;
  }

  @Override
  public synchronized FloatValue exponentiate(Value<?> exponential) {
    value = (int) Math.pow(value, exponential.doubleValue());
    return this;
  }

  @Override
  public FloatValue root(Value<?> root) {
    return exponentiate(root.getReciprocal());
  }

  @Override
  public synchronized FloatValue modulus() {
    value = Math.abs(value);
    return this;
  }
}
