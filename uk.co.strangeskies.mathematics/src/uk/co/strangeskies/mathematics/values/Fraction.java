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

public final class Fraction extends Value<Fraction> {
  private static final long serialVersionUID = 1L;

  private int numerator;
  private int denominator;

  public Fraction() {}

  public Fraction(int numerator, int denominator) {
    if (denominator != 0) {
      this.numerator = numerator;
      this.denominator = denominator;
    } else
      throw new IllegalArgumentException();
  }

  public Fraction(int numerator) {
    this(numerator, 1);
  }

  @Override
  public final Fraction reciprocate() {
    int swap = numerator;
    numerator = denominator;
    denominator = swap;

    return this;
  }

  public final int getNumerator() {
    return numerator;
  }

  public final void setNumerator(int numerator) {
    this.numerator = numerator;
  }

  public final int getDenominator() {
    return denominator;
  }

  public final void setDenominator(int denominator) {
    this.denominator = denominator;
  }

  public final static int greatestCommonDivisor(int x, int y) {
    while (x != y)
      if (x > y)
        x -= y;
      else
        y -= x;
    return x;
  }

  public final Fraction reduce() {
    int greatestCommonDivisor = greatestCommonDivisor(numerator, denominator);

    numerator /= greatestCommonDivisor;
    denominator /= greatestCommonDivisor;

    return this;
  }

  public final Fraction getReduced() {
    return copy().reduce();
  }

  @Override
  public final String toString() {
    if (numerator > denominator && denominator > 1)
      return (numerator + "/" + denominator + " or " + (numerator / denominator) + " "
          + (numerator % denominator) + "/" + denominator);
    else
      return (numerator + "/" + denominator);
  }

  @Override
  public final boolean equals(Object that) {
    if (this == that) {
      return true;
    }
    if (that instanceof Fraction) {
      return numerator == ((Fraction) that).getNumerator()
          && denominator == ((Fraction) that).getDenominator();
    }
    if (that instanceof Value<?>) {
      return ((Value<?>) that).equals(this);
    }
    if (that instanceof Number) {
      return numerator == ((Number) that).doubleValue() * denominator
          && numerator == ((Number) that).longValue() * denominator;
    }
    return false;
  }

  @Override
  protected final boolean equals(Value<?> that) {
    return that.getMultiplied(denominator).equals(numerator);
  }

  @Override
  public final boolean equals(double value) {
    return numerator == value * denominator;
  }

  @Override
  public final boolean equals(float value) {
    return numerator == value * denominator;
  }

  @Override
  public final boolean equals(int value) {
    return numerator == value * denominator;
  }

  @Override
  public final boolean equals(long value) {
    return numerator == value * denominator;
  }

  @Override
  public final int compareToAtSupportedPrecision(Value<?> other) {
    return 0;
  }

  @Override
  public final Fraction unitInTheLastPlaceAbove() {
    return new Fraction(1, denominator);
  }

  @Override
  public final Fraction unitInTheLastPlaceBelow() {
    return new Fraction(1, denominator);
  }

  @Override
  public final Fraction copy() {
    return new Fraction(numerator, denominator);
  }

  @Override
  public final Fraction add(Value<?> value) {
    if (value instanceof Fraction) {
      add((Fraction) value);
    } else {
      numerator += value.getMultipliedPrimitive(denominator);
      update();
    }
    return this;
  }

  public final Fraction add(Fraction that) {
    this.numerator = (this.numerator * that.getDenominator())
        + (that.getNumerator() * this.denominator);
    this.denominator *= that.getDenominator();
    update();
    return this;
  }

  @Override
  public final Fraction subtract(Value<?> value) {
    if (value instanceof Fraction) {
      subtract((Fraction) value);
    } else {
      numerator -= value.getMultipliedPrimitive(denominator);
      update();
    }
    return this;
  }

  public final Fraction subtract(Fraction that) {
    this.numerator = (this.numerator * that.getDenominator())
        - (that.getNumerator() * this.denominator);
    this.denominator *= that.getDenominator();
    update();
    return this;
  }

  @Override
  public final Fraction multiply(Value<?> value) {
    if (value instanceof Fraction) {
      multiply((Fraction) value);
    } else {
      numerator = value.getMultipliedPrimitive(numerator);
      update();
    }
    return this;
  }

  public final Fraction multiply(Fraction that) {
    this.numerator *= that.getNumerator();
    this.denominator *= that.getDenominator();
    update();
    return this;
  }

  @Override
  public final Fraction multiply(int value) {
    numerator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction multiply(long value) {
    numerator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction multiply(float value) {
    numerator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction multiply(double value) {
    numerator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction divide(Value<?> value) {
    if (value instanceof Fraction) {
      divide((Fraction) value);
    } else {
      denominator = value.getMultipliedPrimitive(denominator);
      update();
    }
    return this;
  }

  public final Fraction divide(Fraction that) {
    this.numerator *= that.getDenominator();
    this.denominator *= that.getNumerator();
    update();
    return this;
  }

  @Override
  public final Fraction divide(int value) {
    denominator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction divide(long value) {
    denominator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction divide(float value) {
    denominator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction divide(double value) {
    denominator *= value;
    update();
    return this;
  }

  @Override
  public final Fraction increment() {
    numerator += denominator;
    update();
    return this;
  }

  @Override
  public final Fraction decrement() {
    numerator -= denominator;
    update();
    return this;
  }

  @Override
  public final Fraction negate() {
    numerator = -numerator;
    update();
    return this;
  }

  @Override
  public final Fraction setValue(Number value) {
    numerator = value.intValue();
    denominator = 1;
    update();
    return this;
  }

  public final Fraction setValue(Number numerator, Number denominator) {
    this.numerator = numerator.intValue();
    this.denominator = denominator.intValue();
    update();
    return this;
  }

  @Override
  public final double doubleValue() {
    return (double) numerator / denominator;
  }

  @Override
  public final float floatValue() {
    return (float) numerator / denominator;
  }

  @Override
  public final int intValue() {
    return numerator / denominator;
  }

  @Override
  public final long longValue() {
    return (long) numerator / denominator;
  }

  @Override
  public final int getMultipliedPrimitive(int value) {
    return value * numerator / denominator;
  }

  @Override
  public final long getMultipliedPrimitive(long value) {
    return value * numerator / denominator;
  }

  @Override
  public final float getMultipliedPrimitive(float value) {
    return value * numerator / denominator;
  }

  @Override
  public final double getMultipliedPrimitive(double value) {
    return value * numerator / denominator;
  }

  @Override
  public final int getDividedPrimitive(int value) {
    return value * denominator / numerator;
  }

  @Override
  public final long getDividedPrimitive(long value) {
    return value * denominator / numerator;
  }

  @Override
  public final float getDividedPrimitive(float value) {
    return value * denominator / numerator;
  }

  @Override
  public final double getDividedPrimitive(double value) {
    return value * denominator / numerator;
  }

  @Override
  public Fraction square() {
    denominator *= denominator;
    numerator *= numerator;
    update();
    return this;
  }

  @Override
  public Fraction squareRoot() {
    denominator = (int) Math.sqrt(denominator);
    numerator = (int) Math.sqrt(numerator);
    update();
    return this;
  }

  @Override
  public Fraction exponentiate(Value<?> exponential) {
    denominator = (int) Math.pow(denominator, exponential.doubleValue());
    numerator = (int) Math.pow(numerator, exponential.doubleValue());
    update();
    return this;
  }

  @Override
  public Fraction root(Value<?> root) {
    denominator = (int) Math.pow(denominator, root.reciprocate().doubleValue());
    numerator = (int) Math.pow(numerator, root.reciprocate().doubleValue());
    update();
    return this;
  }

  @Override
  public Fraction modulus() {
    denominator = Math.abs(denominator);
    numerator = Math.abs(numerator);
    return this;
  }
}