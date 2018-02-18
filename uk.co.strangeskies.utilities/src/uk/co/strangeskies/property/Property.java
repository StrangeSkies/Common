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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.property;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This interface represents a gettable and settable property of a given type.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of the property.
 */
public interface Property<T> {
  /**
   * Set the value of this property to the given value.
   * 
   * @param to
   *          The new value to set for this property.
   * @return The previous value of this property.
   */
  T set(T to);

  /**
   * Set the value of this property to null. This may throw a
   * {@link NullPointerException} if null values are not supported by the
   * underlying implementation.
   * 
   * @return The previous value of this property.
   */
  default T unset() {
    return set(null);
  }

  /**
   * Get the current value of the property.
   * 
   * @return The current value.
   */
  T get();

  default Optional<T> tryGet() {
    return Optional.ofNullable(get());
  }

  default Property<T> setDefault(Supplier<T> defaultValue) {
    T value = get();
    if (value == null)
      set(defaultValue.get());
    return this;
  }

  default <U> Property<U> map(
      Function<? super T, ? extends U> out,
      Function<? super U, ? extends T> in) {
    Property<T> base = this;
    return new Property<U>() {
      @Override
      public U set(U to) {
        U previous = get();
        base.set(in.apply(to));
        return previous;
      }

      @Override
      public U get() {
        T baseValue = base.get();
        return baseValue == null ? null : out.apply(baseValue);
      }

    };
  }

  /**
   * Create a property which defers its implementation to the given callbacks.
   * 
   * @param get
   *          the property retrieval callback
   * @param set
   *          the property assignment callback
   * @return a property over the given callbacks
   */
  static <T> Property<T> over(Supplier<T> get, Consumer<T> set) {
    return new Property<T>() {
      @Override
      public T set(T to) {
        T previous = get();
        set.accept(to);
        return previous;
      }

      @Override
      public T get() {
        return get.get();
      }

      @Override
      public String toString() {
        return Objects.toString(get());
      }
    };
  }
}
