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

import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import uk.co.strangeskies.reflection.ReflectionException;

/**
 * @author Elias N Vasylenko
 * 
 *         Facilitates the ability to track the exact type of an object in cases
 *         where it would normally be erased and so unavailable through
 *         reflection.
 *
 * @param <T>
 *          The type of the object instance to track.
 */
public class TypedObject<T> implements ReifiedType<TypedObject<T>> {
  private final TypeToken<T> type;
  private final T object;

  /**
   * @param type
   *          The exact type of an object to keep track of.
   * @param object
   *          An object reference of the given type.
   */
  private TypedObject(TypeToken<T> type, T object) {
    Objects.requireNonNull(type);

    this.type = type;
    this.object = object;
  }

  /**
   * Convenience method to return a {@link TypedObject} wrapper around an object
   * instance of this type.
   * 
   * @param object
   *          The object to wrap with a typed container
   * @return A typed container for the given object
   */
  public static <T> TypedObject<T> typedObject(TypeToken<T> type, T object) {
    return new TypedObject<>(type, object);
  }

  /**
   * Convenience method to return a {@link TypedObject} wrapper around an object
   * instance of this type.
   * 
   * @param object
   *          The object to wrap with a typed container
   * @return A typed container for the given object
   */
  public static <T> TypedObject<T> typedObject(Class<T> type, T object) {
    return typedObject(forClass(type), object);
  }

  /**
   * Cast an untyped object into a typed object without consideration for generic
   * type safety.
   * 
   * @param <T>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @param object
   *          The object to cast
   * @return A typed object over the given type and object
   */
  @SuppressWarnings("unchecked")
  public static <T> TypedObject<T> castUnsafe(Object object, TypeToken<T> type) {
    if (!type.getErasedUpperBounds().allMatch(r -> r.isAssignableFrom(object.getClass())))
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invalidCastObject(object, object.getClass(), type.getType()));

    return new TypedObject<>(type, (T) object);
  }

  /**
   * Cast a typed object into a differently typed object without consideration for
   * generic type safety.
   * 
   * @param <U>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @return A typed object over the given type and object
   */
  @SuppressWarnings("unchecked")
  public <U> TypedObject<U> castUnsafe(TypeToken<U> type) {
    return castUnsafe((U) object, type);
  }

  /**
   * Cast a typed object into a differently typed object.
   * 
   * @param <U>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @return If the cast succeeds, an optional containing the typed object over
   *         the given type and object is returned, otherwise an empty optional is
   *         returned.
   */
  public <U> Optional<TypedObject<U>> tryCast(TypeToken<U> type) {
    if (!type.isCastableFrom(this.type) || !type.isCastableFrom(object.getClass()))
      return Optional.empty();
    else
      return Optional.of(castUnsafe(object, type));
  }

  /**
   * Cast a typed object into a differently typed object.
   * 
   * @param <U>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @return A typed object over the given type and object
   */
  public <U> TypedObject<U> cast(TypeToken<U> type) {
    return tryCast(type)
        .orElseThrow(
            () -> new ReflectionException(
                REFLECTION_PROPERTIES
                    .invalidCastObject(this, this.type.getType(), type.getType())));
  }

  /**
   * Cast a typed object into a differently typed object, succeeding only if the
   * types are assignment compatible.
   * 
   * @param <U>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @return If the cast succeeds, an optional containing the typed object over
   *         the given type and object is returned, otherwise an empty optional is
   *         returned.
   */
  public <U> Optional<TypedObject<U>> tryAssign(TypeToken<U> type) {
    if (!type.satisfiesConstraintFrom(LOOSE_COMPATIBILILTY, this.type)
        || !type.isCastableFrom(object.getClass()))
      return Optional.empty();
    else
      return Optional.of(castUnsafe(object, type));
  }

  /**
   * Cast a typed object into a differently typed object, succeeding only if the
   * types are assignment compatible.
   * 
   * @param <U>
   *          The target type of the cast
   * @param type
   *          A type token over the type of the cast
   * @return A typed object over the given type and object
   */
  public <U> TypedObject<U> assign(TypeToken<U> type) {
    return tryAssign(type)
        .orElseThrow(
            () -> new ReflectionException(
                REFLECTION_PROPERTIES
                    .invalidCastObject(this, this.type.getType(), type.getType())));
  }

  /**
   * @return The type of the reference.
   */
  public TypeToken<T> getTypeToken() {
    return type;
  }

  /**
   * @return The type of the reference.
   */
  public Type getType() {
    return getTypeToken().getType();
  }

  /**
   * @return An object reference guaranteed to be of the given type.
   */
  public T getObject() {
    return object;
  }

  @Override
  public String toString() {
    return object + ": " + type;
  }

  @Override
  public TypeToken<TypedObject<T>> getThisTypeToken() {
    return new TypeToken<TypedObject<T>>() {}
        .withTypeArguments(new TypeParameter<T>().asType(type));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof TypedObject<?>))
      return false;

    TypedObject<?> that = (TypedObject<?>) obj;

    return Objects.equals(this.object, that.object) && Objects.equals(this.type, that.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(object, type);
  }
}
