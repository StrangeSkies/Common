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
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.expression;

import uk.co.strangeskies.function.TriFunction;
import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.expression.PassiveExpression;
import uk.co.strangeskies.expression.UnaryExpression;

/**
 * As {@link UnaryExpression}, but with three operands.
 *
 * @author Elias N Vasylenko
 * @param <O1>
 *          The type of the first operand.
 * @param <O2>
 *          The type of the second operand.
 * @param <O3>
 *          The type of the third operand.
 * @param <R>
 *          The type of the result.
 */
public abstract class TrinaryExpression<O1, O2, O3, R> extends PassiveExpression<R> {
  private final Expression<? extends O1> firstOperand;
  private final Expression<? extends O2> secondOperand;
  private final Expression<? extends O3> thirdOperand;
  private final TriFunction<? super O1, ? super O2, ? super O3, ? extends R> operation;

  /**
   * @param firstOperand
   *          An expression providing the first operand for the function.
   * @param secondOperand
   *          An expression providing the second operand for the function.
   * @param thirdOperand
   *          An expression providing the third operand for the function.
   * @param operation
   *          A expression providing a function transforming the operands into a
   *          value of this expression's type.
   */
  public TrinaryExpression(
      Expression<? extends O1> firstOperand,
      Expression<? extends O2> secondOperand,
      Expression<? extends O3> thirdOperand,
      TriFunction<? super O1, ? super O2, ? super O3, ? extends R> operation) {
    super(firstOperand, secondOperand, thirdOperand);

    this.firstOperand = firstOperand;
    this.secondOperand = secondOperand;
    this.thirdOperand = thirdOperand;

    this.operation = operation;
  }

  /**
   * @return The first operand expression.
   */
  public Expression<? extends O1> getFirstOperand() {
    return firstOperand;
  }

  /**
   * @return The second operand expression.
   */
  public Expression<? extends O2> getSecondOperand() {
    return secondOperand;
  }

  /**
   * @return The third operand expression.
   */
  public Expression<? extends O3> getThirdOperand() {
    return thirdOperand;
  }

  /**
   * @return the operation
   */
  public TriFunction<? super O1, ? super O2, ? super O3, ? extends R> getOperation() {
    return operation;
  }

  @Override
  protected R evaluate() {
    return operation
        .apply(firstOperand.getValue(), secondOperand.getValue(), thirdOperand.getValue());
  }
}
