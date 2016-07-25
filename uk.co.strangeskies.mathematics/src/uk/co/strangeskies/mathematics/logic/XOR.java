/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import uk.co.strangeskies.mathematics.expression.BinaryExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class XOR<O extends XORable<?, ? super T>, T>
		extends BinaryExpression<XOR<O, T>, XORable<? extends O, ? super T>, T, O> {
	public XOR(Expression<?, ? extends /*  */XORable<? extends O, ? super T>> firstOperand,
			Expression<?, ? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new XOROperation<O, T>());
	}

	@Override
	public XOR<O, T> copy() {
		return new XOR<>(getFirstOperand(), getSecondOperand());
	}
}
