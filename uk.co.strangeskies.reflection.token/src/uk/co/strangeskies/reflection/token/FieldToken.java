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
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;

/**
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the owner type which the field belongs to
 * @param <T>
 *          the type of the field
 */
public class FieldToken<O, T> implements MemberToken<O, FieldToken<O, T>> {
  private final BoundSet bounds;

  private final Field field;

  private final TypeToken<? super O> receiverType;
  private final TypeToken<T> fieldType;

  protected FieldToken(Class<?> instance, Field field) {
    this(emptyBoundSet(), instance, field.getType(), field);
  }

  @SuppressWarnings("unchecked")
  private FieldToken(BoundSet bounds, Class<?> receiverType, Class<?> fieldType, Field field) {
    this.bounds = bounds;

    this.field = field;

    this.receiverType = (TypeToken<? super O>) forClass(receiverType);
    this.fieldType = (TypeToken<T>) forClass(fieldType);
  }

  private FieldToken(
      BoundSet bounds,
      TypeToken<? super O> receiverType,
      TypeToken<T> fieldType,
      Field field) {
    this.bounds = bounds;
    this.field = field;
    this.receiverType = receiverType;
    this.fieldType = fieldType;
  }

  /**
   * Create a new {@link FieldToken} instance from a reference to a {@link Field}
   * of an outer or static class.
   * 
   * <p>
   * If the method is generic it will be parameterized with its own type
   * variables.
   * 
   * @param field
   *          the field to wrap
   * @return an field member wrapping the given constructor
   */
  public static FieldToken<Void, ?> forField(Field field) {
    return new FieldToken<>(field.getDeclaringClass(), field);
  }

  /**
   * Create a new {@link FieldToken} instance from a reference to a {@link Field}.
   * 
   * @param field
   *          the field to wrap
   * @return a field member wrapping the given field
   */
  public static FieldToken<Void, ?> forStaticField(Field field) {
    if (!Modifier.isStatic(field.getModifiers())) {
      throw new ReflectionException(REFLECTION_PROPERTIES.memberMustBeStatic(field));
    }
    return new FieldToken<>(void.class, field);
  }

  @Override
  public String getName() {
    return getMember().getName();
  }

  @Override
  public Field getMember() {
    return field;
  }

  @Override
  public BoundSet getBounds() {
    return receiverType.getBounds();
  }

  /**
   * @return true if the wrapped field is volatile, false otherwise
   */
  public boolean isVolatile() {
    return Modifier.isVolatile(getMember().getModifiers());
  }

  /**
   * @return true if the wrapped field is transient, false otherwise
   */
  public boolean isTransient() {
    return Modifier.isTransient(getMember().getModifiers());
  }

  @Override
  public TypeToken<? super O> getReceiverType() {
    return receiverType;
  }

  /**
   * @return the exact generic type of the field according to the type of its
   *         owner
   */
  public TypeToken<T> getFieldType() {
    return fieldType;
  }

  /**
   * If the declaration is raw, parameterize it with its own type parameters,
   * otherwise return the declaration itself.
   * 
   * @return the parameterized version of the declaration where applicable, else
   *         the unmodified declaration
   */
  @SuppressWarnings("unchecked")
  public FieldToken<? extends O, ? extends T> parameterize() {
    if (isRaw()) {
      return (FieldToken<? extends O, ? extends T>) getParameterizedFromRaw();
    } else {
      return this;
    }
  }

  private FieldToken<?, ? extends T> getParameterizedFromRaw() {
    @SuppressWarnings("unchecked")
    TypeToken<Object> receiverType = (TypeToken<Object>) getReceiverType()
        .getErasedTypeToken()
        .parameterize();

    @SuppressWarnings("unchecked")
    TypeToken<? extends T> returnType = (TypeToken<? extends T>) forType(
        getMember().getGenericType());

    return new FieldToken<>(bounds, receiverType, returnType, getMember());
  }

  /**
   * Derive a new {@link FieldToken} instance, with the given bounds incorporated
   * into the bounds of the underlying resolver. The original {@link FieldToken}
   * will remain unmodified.
   * 
   * @param bounds
   *          The new bounds to incorporate.
   * @return The newly derived {@link ExecutableToken}.
   */
  @Override
  public FieldToken<O, T> withBounds(BoundSet bounds) {
    if (isRaw() || bounds.isEmpty()) {
      return this;
    } else {
      return withTypeSubstitution(getBounds().withBounds(bounds), new TypeSubstitution());
    }
  }

  protected FieldToken<O, T> withTypeSubstitution(
      BoundSet bounds,
      TypeSubstitution typeSubstitution) {
    return new FieldToken<>(
        bounds,
        determineReceiverType(bounds, typeSubstitution),
        determineFieldType(bounds, typeSubstitution),
        field);
  }

  private TypeToken<? super O> determineReceiverType(
      BoundSet bounds,
      TypeSubstitution typeArguments) {
    if (getReceiverType().getType() instanceof Class<?>) {
      return getReceiverType();
    } else {
      return new TypeToken<>(bounds, typeArguments.resolve(getReceiverType().getType()));
    }
  }

  private TypeToken<T> determineFieldType(BoundSet bounds, TypeSubstitution typeArguments) {
    if (getFieldType().getType() instanceof Class<?>) {
      return getFieldType();
    } else {
      return new TypeToken<>(bounds, typeArguments.resolve(getFieldType().getType()));
    }
  }

  @Override
  public FieldToken<?, ? extends T> withReceiverType(Type type) {
    return withReceiverType(forType(type));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <U> FieldToken<U, ? extends T> withReceiverType(TypeToken<U> type) {
    if (!receiverType.isGeneric()) {
      if (!receiverType.satisfiesConstraintFrom(SUBTYPE, type)) {
        throw new ReflectionException(
            REFLECTION_PROPERTIES.cannotResolveReceiver(getMember(), type.getType()));
      }

      return (FieldToken<U, T>) this;
    }

    if (isRaw()) {
      Class<?> rawType = getReceiverType().getErasedType();
      TypeToken<? super U> receiverType = type.resolveSupertype(rawType);

      return new FieldToken<>(
          type.getBounds(),
          receiverType,
          (TypeToken<? extends T>) forType(field.getGenericType())
              .withTypeArguments(receiverType.getAllTypeArguments().collect(toList())),
          field);

    } else {
      return (FieldToken<U, T>) withBounds(
          new ConstraintFormula(Kind.SUBTYPE, type.getType(), receiverType.getType())
              .reduce(getBounds().withBounds(type.getBounds())));
    }
  }

  /**
   * As @see {@link #withType(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public <S> FieldToken<O, S> withType(Class<S> target) {
    return withType(TypeToken.forClass(target));
  }

  /**
   * As @see {@link #withType(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public FieldToken<O, ?> withType(Type target) {
    if (target == null)
      return this;

    return withType(forType(target));
  }

  /**
   * Derive a new instance of {@link FieldToken} with the exact given type.
   * 
   * TODO document
   */
  public <S> FieldToken<O, S> withType(TypeToken<S> target) {
    throw new UnsupportedOperationException(); // TODO
  }

  /**
   * As @see {@link #withAssignmentTo(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public <S> FieldToken<O, ? extends S> withAssignmentTo(Class<S> target) {
    return withAssignmentTo(TypeToken.forClass(target));
  }

  /**
   * As @see {@link #withAssignmentTo(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public FieldToken<O, ?> withAssignmentTo(Type target) {
    if (target == null)
      return this;

    return withAssignmentTo(forType(target));
  }

  /**
   * Derive a new instance of {@link FieldToken} with the given assignment target
   * type.
   * 
   * TODO document
   */
  public <S> FieldToken<O, ? extends S> withAssignmentTo(TypeToken<S> target) {
    throw new UnsupportedOperationException(); // TODO
  }

  /**
   * As @see {@link #withAssignmentFrom(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public <S> FieldToken<O, ? super S> withAssignmentFrom(Class<S> target) {
    return withAssignmentFrom(TypeToken.forClass(target));
  }

  /**
   * As @see {@link #withAssignmentFrom(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public FieldToken<O, ?> withAssignmentFrom(Type target) {
    if (target == null)
      return this;

    return withAssignmentFrom(forType(target));
  }

  /**
   * Derive a new instance of {@link FieldToken} which is assignment compatible
   * from the given type.
   * 
   * TODO document
   */
  public <S> FieldToken<O, ? super S> withAssignmentFrom(TypeToken<S> target) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public FieldToken<O, T> resolve() {
    TypeResolver resolver = new TypeResolver(getBounds());
    resolver.resolve();
    return withBounds(resolver.getBounds());
  }

  /**
   * @param target
   *          the instance to access the field of
   * @return the value of the field
   */
  @SuppressWarnings("unchecked")
  public T get(O target) {
    try {
      return (T) getMember().get(target);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotGetField(target, this.getMember()),
          e);
    }
  }

  /**
   * @param target
   *          the instance to assign to the field of
   * @param value
   *          the value to assign
   */
  public void set(O target, T value) {
    try {
      getMember().set(target, value);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotSetField(target, value, this.getMember()),
          e);
    }
  }

  @Override
  public Optional<? extends DeclarationToken<?>> getOwningDeclaration() {
    return isStatic() ? Optional.empty() : Optional.of(getReceiverType());
  }

  @Override
  public int getTypeParameterCount() {
    return getDeclaringClass().getTypeParameters().length;
  }

  @Override
  public Stream<TypeParameter<?>> getTypeParameters() {
    return Stream.empty();
  }

  @Override
  public Stream<TypeArgument<?>> getTypeArguments() {
    return Stream.empty();
  }

  @Override
  public FieldToken<O, T> withTypeArguments(Collection<? extends TypeArgument<?>> arguments) {
    return this;
  }

  @Override
  public boolean isGeneric() {
    return false;
  }

  @Override
  public FieldToken<O, T> withAllTypeArguments(List<Type> typeArguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FieldToken<O, T> withTypeArguments(List<Type> typeArguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FieldToken<O, T> withAllTypeArguments(Type... typeArguments) {
    return withAllTypeArguments(asList(typeArguments));
  }

  @Override
  public FieldToken<O, T> withTypeArguments(Type... typeArguments) {
    return withTypeArguments(asList(typeArguments));
  }

  /**
   * Find which fields are declared on this type.
   * 
   * @param declaringClass
   *          the declaring class for which to retrieve the fields
   * @return all {@link Field} objects applicable to this type, wrapped in
   *         {@link FieldToken} instances
   */
  public static Stream<FieldToken<Void, ?>> staticFields(Class<?> declaringClass) {
    return stream(declaringClass.getDeclaredFields())
        .filter(f -> Modifier.isStatic(f.getModifiers()))
        .map(FieldToken::forStaticField);
  }
}
