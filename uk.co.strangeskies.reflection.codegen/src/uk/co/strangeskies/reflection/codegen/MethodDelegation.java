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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public class MethodDelegation<T> {
  /**
   * An intercepter which delegates invocation of every method which is present on
   * the given object, or which overrides a method which is present on the given
   * object, using the given object as the receiver.
   * <p>
   * Classes defined by way of such an intercepter may not be serialized and must
   * be instantiated from within the defining JVM instance, as they are dependent
   * on the specific instance given.
   * 
   * @param intercepter
   *          the object to which invocation should be delegated
   * @return the receiver type
   */
  public static <T> MethodDelegation<T> instanceDelegation(Object intercepter) {
    throw new UnsupportedOperationException();
  }

  public static <T> MethodDelegation<T> invocationDelegation(InvocationHandler handler) {
    throw new UnsupportedOperationException();
  }

  /*
   * TODO This filter can be applied at class compilation time when defining the
   * method behavior so doesn't restrict class writing like instanceDelegation
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   * TODO this class should be redesigned pretty heavily ... problems include:
   * 
   * - not parameterized for return value
   * 
   * - parameterization is not inferred from target type because of method
   * chaining
   * 
   * - needs? forMethod declaratively for a method signature rather than just a
   * filter?
   * 
   * - do we filter for the overridden Method or the generated Method? If the
   * former, we can filter on methods which have a default implementation
   * 
   * 
   * 
   * 
   * 
   * 
   * 
   */
  public MethodDelegation<T> filterOverriddenMethod(Predicate<Method> receiver) {
    throw new UnsupportedOperationException();
  }
}
