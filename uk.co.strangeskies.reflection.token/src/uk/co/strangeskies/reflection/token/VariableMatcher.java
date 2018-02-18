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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.Visibility.forModifiers;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Predicate;

import uk.co.strangeskies.reflection.Visibility;

public class VariableMatcher<O, T> implements Predicate<FieldToken<?, ?>> {
  public static VariableMatcher<Object, Object> anyVariable() {
    return new VariableMatcher<>(empty(), empty(), empty(), empty());
  }

  private final Optional<String> name;
  private final Optional<Visibility> visibility;
  private final Optional<TypeToken<?>> assignableTo;
  private final Optional<TypeToken<?>> assignableFrom;

  protected VariableMatcher(
      Optional<String> name,
      Optional<Visibility> visibility,
      Optional<TypeToken<?>> assignableTo,
      Optional<TypeToken<?>> assignableFrom) {
    this.name = name;
    this.visibility = visibility;
    this.assignableTo = assignableTo;
    this.assignableFrom = assignableFrom;
  }

  @SuppressWarnings("unchecked")
  public Optional<FieldToken<O, T>> match(FieldToken<?, ?> field) {
    if (test(field))
      return of((FieldToken<O, T>) field);
    else
      return empty();
  }

  @Override
  public boolean test(FieldToken<?, ?> field) {
    return testImpl(field.getName(), field.getVisibility(), field.getFieldType().getType());
  }

  public boolean test(Field field) {
    return testImpl(field.getName(), forModifiers(field.getModifiers()), field.getGenericType());
  }

  private boolean testImpl(String name, Visibility visibility, Type type) {
    return this.name.map(name::equals).orElse(true)
        && this.visibility.map(v -> visibility.visibilityIsAtLeast(v)).orElse(true)
        && this.assignableTo.map(t -> t.satisfiesConstraintFrom(SUBTYPE, type)).orElse(true)
        && this.assignableFrom.map(t -> t.satisfiesConstraintTo(SUBTYPE, type)).orElse(true);
  }

  public VariableMatcher<O, T> named(String name) {
    return new VariableMatcher<>(of(name), visibility, assignableTo, assignableFrom);
  }

  public VariableMatcher<O, T> visibleTo(Visibility visibility) {
    return new VariableMatcher<>(name, of(visibility), assignableTo, assignableFrom);
  }

  public <U> VariableMatcher<O, U> typed(TypeToken<U> type) {
    return new VariableMatcher<>(name, visibility, of(type), of(type));
  }

  public <U> VariableMatcher<O, U> typed(Class<U> type) {
    return typed(forClass(type));
  }

  public <U> VariableMatcher<O, U> assignableTo(TypeToken<U> type) {
    return new VariableMatcher<>(name, visibility, of(type), assignableFrom);
  }

  public <U> VariableMatcher<O, U> assignableTo(Class<U> type) {
    return assignableTo(forClass(type));
  }

  public <U> VariableMatcher<O, U> assignableFrom(TypeToken<U> type) {
    return new VariableMatcher<>(name, visibility, assignableTo, of(type));
  }

  public <U> VariableMatcher<O, U> assignableFrom(Class<U> type) {
    return assignableFrom(forClass(type));
  }

  public <U> MethodMatcher<U, T> receiving(TypeToken<U> type) {
    return null;
  }

  public <U> MethodMatcher<U, T> receiving(Class<U> type) {
    return receiving(forClass(type));
  }
}
