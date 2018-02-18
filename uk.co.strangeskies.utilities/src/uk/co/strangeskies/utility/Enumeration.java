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
package uk.co.strangeskies.utility;

import static java.lang.Character.toUpperCase;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A reimplementation of Java's Enum class, primarily for the purpose of
 * facilitating enumerations with generics. This class should enforce most of
 * the rules governing enum, including limiting instance creation to static
 * initializer blocks. Unfortunately error detection of such problems is
 * inevitably shifted to runtime rather than compile time.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          Self bounding on the type of the enumeration class.
 */
public class Enumeration<S extends Enumeration<S>> implements Self<S> {
  private static class EnumerationType<T extends Enumeration<T>> {
    private final Class<T> type;
    private final List<T> instances;
    private boolean initialised;

    public EnumerationType(Class<T> type) {
      this.type = type;
      instances = new ArrayList<>();
      initialised = false;
    }

    public List<T> getInstances() {
      return Collections.unmodifiableList(instances);
    }

    public int addInstance(T instance) {
      if (initialised)
        throw new IllegalStateException(
            "Cannot instantiate instance of enumeration class '" + type
                + "' as the class has already been initialised");

      int ordinal = instances.size();

      instances.add(instance);

      return ordinal;
    }

    public void initialised() {
      initialised = true;
    }

    public boolean isInitialised() {
      return initialised;
    }
  }

  private static final Map<Class<?>, EnumerationType<?>> ENUM_TYPES = new HashMap<>();

  private final String name;
  private final int ordinal;

  protected Enumeration(String name) {
    this.name = name;
    ordinal = addInstance(getThis());
  }

  /**
   * @return The name of this enumeration item instance.
   */
  public String name() {
    return name;
  }

  @Override
  public final boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  /**
   * @return The ordinal number of this enumeration item instance.
   */
  public int ordinal() {
    return ordinal;
  }

  @Override
  public String toString() {
    return name();
  };

  @SuppressWarnings("unchecked")
  private static <T extends Enumeration<T>> int addInstance(T instance) {
    EnumerationType<T> enumerationType = getEnumerationType(((Class<T>) instance.getClass()));
    List<T> enumerationConstants = enumerationType.getInstances();

    if (enumerationConstants.stream().anyMatch(e -> e.name().equals(instance.name())))
      throw new IllegalArgumentException();

    return enumerationType.addInstance(instance);
  }

  private static <T extends Enumeration<T>> EnumerationType<T> getEnumerationType(Class<T> type) {
    @SuppressWarnings("unchecked")
    EnumerationType<T> enumType = (EnumerationType<T>) ENUM_TYPES.get(type);
    if (enumType == null)
      ENUM_TYPES.put(type, enumType = new EnumerationType<>(type));

    if (!enumType.isInitialised() && !withinStaticInitialiser(type)) {
      forceInitialisation(type);
      enumType.initialised();
    }

    return enumType;
  }

  /**
   * @param <T>
   *          The {@link Enumeration} class.
   * @param enumerationClass
   *          An Enumeration class for which to retrieve all declared constants.
   * @return A list of all enumeration item instances for the given class, in the
   *         order they were declared.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T extends Enumeration> List<T> getConstants(Class<T> enumerationClass) {
    return Collections.unmodifiableList(getEnumerationType(enumerationClass).getInstances());
  }

  /**
   * Effectively a reimplementation of {@link Enumeration#valueOf} for the
   * Enumeration class.
   * 
   * @param <T>
   *          The {@link Enumeration} class.
   * @param enumerationClass
   *          The class of Enumeration we wish to retrieve an instance of.
   * @param name
   *          The name of the instance to retrieve.
   * @return The instance value with the given name.
   */
  public static <T extends Enumeration<?>> T valueOf(Class<T> enumerationClass, String name) {
    return getConstants(enumerationClass)
        .stream()
        .filter(e -> e.name().equals(name))
        .findAny()
        .get();
  }

  /**
   * A reimplementation of {@link Enum#valueOf} with less pointlessly restrictive
   * generic bounds.
   * 
   * @param <T>
   *          The {@link Enum} class.
   * @param enumerationClass
   *          The class of Enum we wish to retrieve an instance of.
   * @param name
   *          The name of the instance to retrieve.
   * @return The instance value with the given name.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static <T extends Enum<?>> T valueOfEnum(Class<T> enumerationClass, String name) {
    return (T) Enum.valueOf((Class) enumerationClass, name);
  }

  private static void forceInitialisation(Class<?> initialiseClass) {
    try {
      Class.forName(initialiseClass.getName(), true, initialiseClass.getClassLoader());
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  private static boolean withinStaticInitialiser(Class<?> initialisingClass) {
    for (StackTraceElement element : Thread.currentThread().getStackTrace())
      if (element.getClassName().equals(initialisingClass.getName())
          && element.getMethodName().equals("<clinit>"))
        return true;

    return false;
  }

  /**
   * @param enumItem
   *          an enumeration item to make readable, by substituting underscores
   *          with spaces, and properly capitalizing
   * @return a readable version of the given enum item's name
   */
  public static String readableName(Enum<?> enumItem) {
    return Arrays
        .stream(enumItem.name().split("\\s|_"))
        .map(s -> toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
        .collect(joining(" "));
  }

  /**
   * @return the next enumeration item in the sequence
   */
  public S next() {
    int ordinal = ordinal() + 1;
    @SuppressWarnings("unchecked")
    List<S> items = (List<S>) getConstants(getClass());
    return items.size() == ordinal ? items.get(0) : items.get(ordinal);
  }

  /**
   * @param <E>
   *          the type of the given enum item
   * @param item
   *          the enum item we wish to find the next in the sequence from
   * @return the next enumeration item in the sequence
   */
  @SuppressWarnings("unchecked")
  public static <E extends Enum<?>> E next(E item) {
    int ordinal = item.ordinal() + 1;
    Enum<?>[] items = item.getDeclaringClass().getEnumConstants();
    return items.length == ordinal ? (E) items[0] : (E) items[ordinal];
  }
}
