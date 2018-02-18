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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.observable.Observable.merge;

import java.util.Collection;

import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.observable.Observable;

/**
 * An expression which is dependent upon the evaluation of a number of other
 * expressions.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          The type of the expression.
 */
public abstract class PassiveExpression<T> implements Expression<T> {
	private final Observable<Expression<? extends T>> dependencies;
	private T value;

	public PassiveExpression(Collection<? extends Expression<?>> dependencies) {
		this.dependencies = merge(
				dependencies.stream().map(Expression::invalidations).collect(toList())).map(e -> {
					invalidate();
					return this;
				});
		this.dependencies.observe();
	}

	public PassiveExpression(Expression<?>... dependencies) {
		this(asList(dependencies));
	}

	@Override
	public Observable<Expression<? extends T>> invalidations() {
		return dependencies;
	}

	private void invalidate() {
		value = null;
	}

	@Override
	public final T getValue() {
		if (value == null) {
			value = evaluate();
		}

		return value;
	}

	/**
	 * @return The value of this {@link Expression} as derived from the dependency
	 *         {@link Expression}s.
	 */
	protected abstract T evaluate();
}
