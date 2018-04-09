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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.grammar;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import uk.co.strangeskies.text.grammar.Symbol.Nonterminal;

public class Production<T> {
  public static <T> Production<T> produce(Nonterminal<T> symbol) {
    return null;
  }

  public Production<Stream<T>> repeat() {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<Stream<T>> repeat(int minimum) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<Stream<T>> repeat(int minimum, int maximum) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U> Production<U> map(Function<T, U> in, Function<U, T> out) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> prepend(String terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> append(String terminal) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> prepend(Nonterminal<?> symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> append(Nonterminal<?> symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U, R> Production<R> append(
      Nonterminal<U> symbol,
      BiFunction<? super T, ? super U, ? extends R> in,
      Function<? super R, ? extends T> outT,
      Function<? super R, ? extends U> outU) {
    // TODO Auto-generated method stub
    return null;
  }

  public <U, R> Production<R> append(
      Production<U> symbol,
      BiFunction<? super T, ? super U, ? extends R> in,
      Function<? super R, ? extends T> outT,
      Function<? super R, ? extends U> outU) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production<T> check(Predicate<T> test) {
    // TODO Auto-generated method stub
    return null;
  }
}
