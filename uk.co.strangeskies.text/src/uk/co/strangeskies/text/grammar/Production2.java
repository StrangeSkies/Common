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

import java.util.function.BiConsumer;
import java.util.function.Function;

public class Production2 {
  public static Production2 empty() {
    return null;
  }

  public static Production2 produce2(Symbol... symbols) {
    return null;
  }

  public Production2 repeat() {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 not() {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 not(Production2 production) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 repeat(int minimum) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 repeat(int minimum, int maximum) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 then(Symbol symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public Production2 then(Production2 symbol) {
    // TODO Auto-generated method stub
    return null;
  }

  public <T> Production<T> action(Function<SymbolsIn, T> in, BiConsumer<SymbolsOut, T> out) {
    // TODO Auto-generated method stub
    return null;
  }
}
