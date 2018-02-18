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

import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.expression.Expression;
import uk.co.strangeskies.observable.Disposable;

public class ExpressionBuffer<F extends Expression<?>, T> extends AbstractFunctionBuffer<F, T> {
  private Disposable backObserver;

  public ExpressionBuffer(
      T front,
      F back,
      BiFunction<? super T, ? super F, ? extends T> operation) {
    super(front, back, operation);
  }

  public ExpressionBuffer(F back, Function<? super F, ? extends T> function) {
    super(back, function);
  }

  public ExpressionBuffer(T front, F back, Function<? super F, ? extends T> function) {
    super(front, back, function);
  }

  public ExpressionBuffer(AbstractFunctionBuffer<F, T> doubleBuffer) {
    super(doubleBuffer);
  }

  @Override
  public F setBack(F next) {
    if (backObserver != null) {
      backObserver.cancel();
    }

    if (next != null) {
      backObserver = next.invalidations().observe(m -> invalidateBack());
    }

    return super.setBack(next);
  }
}
