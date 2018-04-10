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

// TODO put all the update() read() lock protection stuff in the other Value implementations
public class LongValue extends IntegralValue<LongValue> {
  private static final long serialVersionUID = 5916143538083079342L;

  private long value;

  public LongValue() {}

  public LongValue(Value<?> value) {
    super((Number) value);
  }

  public LongValue(Number value) {
    super(value);
  }

  @Override
  public final LongValue reciprocate() {
    value = 1 / value;
    return this;
  }

  @Override
  public final LongValue add(Value<?> value) {
    this.value += value.longValue();
    return this;
  }

  @Override
  public final LongValue negate() {
    value = -value;
    return this;
  }

  @Override
  public final LongValue multiply(int value) {
    this.value *= value;
    return this;
  }

  @Override
  public final LongValue multiply(long value) {
    this.value *= value;
    return this;
  }

  @Override
  public final LongValue multiply(float value) {
    this.value *= value;
    return this;
  }

  @Override
  public final LongValue multiply(double value) {
    this.value *= value;
    return this;
  }

  @Override
  public final LongValue multiply(Value<?> value) {
    this.value = value.getMultipliedPrimitive(this.value);
    return this;
  }

  @Override
  public final LongValue divide(int value) {
    this.value /= value;
    return this;
  }

  @Override
  public final LongValue divide(long value) {
    this.value /= value;
    return this;
  }

  @Override
  public final LongValue divide(float value) {
    this.value /= value;
    return this;
  }

  @Override
  public final LongValue divide(double value) {
    this.value /= value;
    return this;
  }

  @Override
  public final LongValue divide(Value<?> value) {
    this.value = value.getDividedPrimitive(this.value);
    return this;
  }

  @Override
  public final LongValue subtract(Value<?> value) {
    this.value -= value.longValue();
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
    return value;
  }

  @Override
  public final String toString() {
    return Long.toString(value);
  }

  @Override
  public final LongValue setValue(Number value) {
    this.value = value.longValue();
    return this;
  }

  @Override
  public final int compareToAtSupportedPrecision(Value<?> other) {
    return Long.compare(this.value, other.longValue());
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
    return that.equals(this.value);
  }

  @Override
  public final int hashCode() {
    return Long.hashCode(value);
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
  public final LongValue increment() {
    value++;
    return this;
  }

  @Override
  public final LongValue decrement() {
    value--;
    return this;
  }

  @Override
  public final LongValue copy() {
    return new LongValue(this);
  }

  @Override
  public final LongValue unitInTheLastPlaceAbove() {
    return new LongValue(1);
  }

  @Override
  public final LongValue unitInTheLastPlaceBelow() {
    return new LongValue(1);
  }

  @Override
  public final int getMultipliedPrimitive(int value) {
    return (int) (this.value * value);
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
    return (int) (this.value * value);
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
  public LongValue square() {
    value *= value;
    return this;
  }

  @Override
  public LongValue squareRoot() {
    value = (int) Math.sqrt(value);
    return this;
  }

  @Override
  public LongValue exponentiate(Value<?> exponential) {
    value = (int) Math.pow(value, exponential.doubleValue());
    return this;
  }

  @Override
  public LongValue root(Value<?> root) {
    value = (int) Math.pow(value, root.reciprocate().doubleValue());
    return this;
  }

  @Override
  public LongValue modulus() {
    value = Math.abs(value);
    return this;
  }
}
