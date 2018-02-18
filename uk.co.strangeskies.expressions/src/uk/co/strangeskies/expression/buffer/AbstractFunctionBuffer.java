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
package uk.co.strangeskies.expression.buffer;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.expression.IdentityExpression;
import uk.co.strangeskies.expression.LockingExpression;

public abstract class AbstractFunctionBuffer<B, F> extends LockingExpression<F> implements DoubleBuffer<B, F> {
	private F frontValue;
	private IdentityExpression<B> back;

	private boolean isFlat;

	private final BiFunction<? super F, ? super B, ? extends F> operation;

	public AbstractFunctionBuffer(F front, B back, BiFunction<? super F, ? super B, ? extends F> operation) {
		setFront(front);
		setBack(back);

		this.operation = operation;
	}

	public AbstractFunctionBuffer(F front, B back, Function<? super B, ? extends F> function) {
		this(front, back, (a, b) -> function.apply(b));
	}

	public AbstractFunctionBuffer(B back, Function<? super B, ? extends F> function) {
		this(function.apply(back), back, function);
	}

	public AbstractFunctionBuffer(AbstractFunctionBuffer<B, F> doubleBuffer) {
		this(doubleBuffer.getFront(), doubleBuffer.getBack(), doubleBuffer.getOperation());
	}

	@Override
	protected final F evaluate() {
		return frontValue;
	}

	@Override
	public Expression<B> getBackExpression() {
		return back;
	}

	public BiFunction<? super F, ? super B, ? extends F> getOperation() {
		return operation;
	}

	@Override
	public F setFront(F front) {
		isFlat = false;

		beginWrite();

		try {
			F previous = this.frontValue;
			this.frontValue = front;
			return previous;
		} finally {
			endWrite();
		}
	}

	@Override
	public B setBack(B back) {
		isFlat = false;

		return this.back.set(back);
	}

	@Override
	public F getFront() {
		return getValue();
	}

	@Override
	public B getBack() {
		return back.get();
	}

	@Override
	public void push() {
		if (!isFlat) {
			setFront(getOperation().apply(getFront(), getBack()));
			isFlat = true;
		}
	}

	@Override
	public boolean equals(Object that) {
		if (this == that) {
			return true;
		}

		if (!(that instanceof AbstractFunctionBuffer)) {
			return false;
		}

		AbstractFunctionBuffer<?, ?> thatDoubleBuffer = (AbstractFunctionBuffer<?, ?>) that;

		F thisFront = this.getFront();
		B thisBack = this.getBack();
		Object thatFront = thatDoubleBuffer.getFront();
		Object thatBack = thatDoubleBuffer.getBack();

		return Objects.equals(thisFront, thatFront) && Objects.equals(thisBack, thatBack);
	}

	@Override
	public int hashCode() {
		int hashCode = getFront().hashCode() + getBack().hashCode() * 29;

		return hashCode;
	}

	@Override
	public boolean isFlat() {
		return isFlat;
	}

	@Override
	public String toString() {
		return "[" + getBack() + "] => [" + getFront() + "]";
	}
}
