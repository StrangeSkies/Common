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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import uk.co.strangeskies.reflection.token.VariableMatcher;

public class MethodImplementations {
  private MethodImplementations() {}

  public static <T> MethodImplementation<T> returningVariable(
      VariableMatcher<?, ? extends T> variableMatcher) {
    throw new UnsupportedOperationException(); // TODO support variable return method impl.
  }

  public static <T> MethodImplementation<T> throwing(Class<? extends Throwable> throwable) {
    throw new UnsupportedOperationException(); // TODO support throwing method impl.
  }

  public static <T> MethodImplementation<T> empty() {
    return scope -> scope.instructions().visitReturn();
  }

  public static <T> MethodImplementation<T> returningNull() {
    return scope -> {
      scope.instructions().visitNull();
      scope.instructions().visitReturn();
    };
  }

  private static <T> MethodImplementation<T> returningLiteralImpl(T value) {
    return scope -> {
      scope.instructions().visitLiteral(value);
      scope.instructions().visitReturn();
    };
  }

  public static MethodImplementation<String> returningLiteral(String value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Integer> returningLiteral(int value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Float> returningLiteral(float value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Long> returningLiteral(long value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Double> returningLiteral(double value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Byte> returningLiteral(byte value) {
    return returningLiteralImpl(value);
  }

  public static MethodImplementation<Character> returningLiteral(char value) {
    return returningLiteralImpl(value);
  }

  public static <T> MethodImplementation<Class<T>> returningLiteral(Class<T> value) {
    return returningLiteralImpl(value);
  }
}
