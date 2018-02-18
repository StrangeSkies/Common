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
 * This file is part of uk.co.strangeskies.reflection.token.
 *
 * uk.co.strangeskies.reflection.token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.token;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.collection.stream.StreamUtilities.zip;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.Visibility;

public class MethodMatcher<O, T> implements Predicate<ExecutableToken<?, ?>> {
  public static MethodMatcher<Object, Object> anyMethod() {
    return new MethodMatcher<>(empty(), empty(), empty(), empty());
  }

  public static MethodMatcher<Object, Object> anyConstructor() {
    return anyMethod().named("<init>");
  }

  public MethodMatcher<Void, Void> anyStaticInitializer() {
    return anyMethod().named("<cinit>").receiving(Void.class).returning(Void.class).accepting();
  }

  private final Optional<String> name;
  private final Optional<Visibility> visibility;
  private final Optional<TypeToken<?>> returnType;
  private final Optional<List<TypeToken<?>>> argumentTypes;

  protected MethodMatcher(
      Optional<String> name,
      Optional<Visibility> visibility,
      Optional<TypeToken<?>> returnType,
      Optional<List<TypeToken<?>>> argumentTypes) {
    this.name = name;
    this.visibility = visibility;
    this.returnType = returnType;
    this.argumentTypes = argumentTypes;
  }

  @SuppressWarnings("unchecked")
  public Optional<ExecutableToken<O, T>> match(ExecutableToken<?, ?> executable) {
    if (test(executable))
      return of((ExecutableToken<O, T>) executable);
    else
      return empty();
  }

  @Override
  public boolean test(ExecutableToken<?, ?> executable) {
    return testImpl(
        executable.getName(),
        executable.getVisibility(),
        executable.getReturnType().getType(),
        (int) executable.getParameters().count(),
        executable.getParameters().map(ExecutableParameter::getType));
  }

  public boolean test(Executable executable) {
    return testImpl(
        executable.getName(),
        forModifiers(executable.getModifiers()),
        executable.getAnnotatedReturnType().getType(),
        executable.getParameterCount(),
        stream(executable.getGenericParameterTypes()));
  }

  private boolean testImpl(
      String name,
      Visibility visibility,
      Type returnType,
      int argumentCount,
      Stream<Type> argumentTypes) {

    return this.name.map(name::equals).orElse(true)
        && this.visibility.map(visibility::visibilityIsAtLeast).orElse(true)
        && this.returnType.map(type -> type.satisfiesConstraintFrom(SUBTYPE, returnType)).orElse(
            true)
        && this.argumentTypes
            .map(
                arguments -> (arguments.size() == argumentCount) && zip(
                    arguments.stream(),
                    argumentTypes,
                    (a, b) -> a.satisfiesConstraintTo(SUBTYPE, b)).reduce((a, b) -> a && b).orElse(
                        true))
            .orElse(true);
  }

  public MethodMatcher<O, T> named(String name) {
    return new MethodMatcher<>(of(name), visibility, returnType, argumentTypes);
  }

  public MethodMatcher<O, T> visibleTo(Visibility visibility) {
    return new MethodMatcher<>(name, of(visibility), returnType, argumentTypes);
  }

  public <U> MethodMatcher<O, U> returning(TypeToken<U> returnType) {
    return new MethodMatcher<>(name, visibility, of(returnType), argumentTypes);
  }

  public <U> MethodMatcher<O, U> returning(Class<U> type) {
    return returning(forClass(type));
  }

  public <U> MethodMatcher<U, T> receiving(TypeToken<U> type) {
    return null;
  }

  public <U> MethodMatcher<U, T> receiving(Class<U> type) {
    return receiving(forClass(type));
  }

  public MethodMatcher<O, T> accepting() {
    return accepting(emptyList());
  }

  public MethodMatcher<O, T> accepting(Type... argumentTypes) {
    return accepting(stream(argumentTypes).map(TypeToken::forType).collect(toList()));
  }

  public MethodMatcher<O, T> accepting(TypeToken<?>... argumentTypes) {
    return accepting(asList(argumentTypes));
  }

  public MethodMatcher<O, T> accepting(List<TypeToken<?>> argumentTypes) {
    return new MethodMatcher<>(name, visibility, returnType, of(argumentTypes));
  }

  // TODO "accepting(...)" by parameter types and by parameter count
}
