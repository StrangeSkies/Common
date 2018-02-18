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
package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.expression.LockingExpression;
import uk.co.strangeskies.utility.Copyable;

public class BooleanValue extends LockingExpression<BooleanValue>
    implements BooleanCombinationBehaviour<BooleanValue, BooleanValue>,
    NOTable<BooleanValue, BooleanValue>, Copyable<BooleanValue> {
  private boolean value;

  public BooleanValue() {}

  public BooleanValue(Boolean value) {
    this.value = value;
  }

  public BooleanValue(BooleanValue expression) {
    value = expression.getValue().getBooleanValue();
  }

  @Override
  public final BooleanValue evaluate() {
    return this;
  }

  public final BooleanValue getDecoupledValue() {
    return copy();
  }

  @Override
  public final BooleanValue copy() {
    return new BooleanValue(this);
  }

  public final BooleanValue getConst() {
    return this;
  }

  public final boolean getBooleanValue() {
    return value;
  }

  public final void setValue(boolean value) {
    this.value = value;
  }

  public final void setValue(Boolean value) {
    this.value = value;
  }

  public final void setValue(BooleanValue value) {
    this.value = value.getValue().getBooleanValue();
  }

  public final void setValue(Condition condition) {
    value = condition.isFulfilled();
  }

  @Override
  public final BooleanValue and(BooleanValue expression) {
    value = value && expression.getValue().getBooleanValue();

    return this;
  }

  @Override
  public final BooleanValue getAnd(BooleanValue expression) {
    return copy().and(expression);
  }

  @Override
  public final BooleanValue getOr(BooleanValue expression) {
    return copy().or(expression);
  }

  @Override
  public final BooleanValue getXor(BooleanValue expression) {
    return copy().xor(expression);
  }

  @Override
  public final BooleanValue getNot() {
    return copy().not();
  }

  @Override
  public final BooleanValue getNand(BooleanValue expression) {
    return copy().nand(expression);
  }

  @Override
  public final BooleanValue getNor(BooleanValue expression) {
    return copy().nor(expression);
  }

  @Override
  public final BooleanValue getXnor(BooleanValue expression) {
    return copy().xnor(expression);
  }

  @Override
  public final BooleanValue or(BooleanValue expression) {
    value = value || expression.getValue().getBooleanValue();

    return this;
  }

  @Override
  public final BooleanValue xor(BooleanValue expression) {
    value = value ^ expression.getValue().getBooleanValue();

    return this;
  }

  @Override
  public final BooleanValue not() {
    value = !value;

    return this;
  }

  @Override
  public final BooleanValue nand(BooleanValue expression) {
    value = !(value && expression.getValue().getBooleanValue());

    return this;
  }

  @Override
  public final BooleanValue nor(BooleanValue expression) {
    value = !(value || expression.getValue().getBooleanValue());

    return this;
  }

  @Override
  public final BooleanValue xnor(BooleanValue expression) {
    value = value == expression.getValue().getBooleanValue();

    return this;
  }
}
