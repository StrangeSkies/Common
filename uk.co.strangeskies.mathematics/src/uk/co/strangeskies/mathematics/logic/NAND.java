/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.logic;

import uk.co.strangeskies.mathematics.expression.BinaryExpression;
import uk.co.strangeskies.mathematics.expression.Expression;

public class NAND<O extends NANDable<?, ? super T>, T>
		extends BinaryExpression<NAND<O, T>, /*  */NANDable<? extends O, ? super T>, /*
																																									 * @
																																									 * ReadOnly
																																									 */T, /*
																																											 * @
																																											 * ReadOnly
																																											 */O> {
	public NAND(Expression<?, ? extends /*  */NANDable<? extends O, ? super T>> firstOperand,
			Expression<?, ? extends /*  */T> secondOperand) {
		super(firstOperand, secondOperand, new NANDOperation<O, T>());
	}

	@Override
	public NAND<O, T> copy() {
		return new NAND<>(getFirstOperand(), getSecondOperand());
	}
}
