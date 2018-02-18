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

import static java.util.stream.Stream.empty;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.token.TypeParameter.forTypeVariable;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeVariableCapture;

/**
 * A token representing a declaration. If the declaration is generic, it may be
 * raw or parameterized.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          the type of declaration
 */
public interface DeclarationToken<S extends DeclarationToken<S>> {
  /**
   * @return true if the declaration represents a raw type or invocation, false
   *         otherwise
   */
  boolean isRaw();

  /**
   * @return true if the declaration is generic, false otherwise
   */
  public boolean isGeneric();

  /**
   * @return the declaration directly enclosing this declaration
   */
  Optional<? extends DeclarationToken<?>> getOwningDeclaration();

  /**
   * @return the count of all generic type parameters of the declaration and any
   *         enclosing declarations
   */
  default int getAllTypeParameterCount() {
    return getTypeParameterCount()
        + getOwningDeclaration().map(DeclarationToken::getAllTypeParameterCount).orElse(0);
  }

  /**
   * @return all generic type parameters of the declaration and any enclosing
   *         declarations
   */
  default Stream<TypeParameter<?>> getAllTypeParameters() {
    return Stream.concat(
        getTypeParameters(),
        getOwningDeclaration().map(DeclarationToken::getAllTypeParameters).orElse(empty()));
  }

  /**
   * @return all generic type parameter instantiations of the declaration, or
   *         their inference variables if not yet instantiated.
   */
  default Stream<TypeArgument<?>> getAllTypeArguments() {
    if (isRaw())
      return Stream.empty();
    else
      return Stream.concat(
          getTypeArguments(),
          getOwningDeclaration().map(DeclarationToken::getAllTypeArguments).orElse(empty()));
  }

  /**
   * @return the count of the generic type parameters of the declaration.
   */
  int getTypeParameterCount();

  /**
   * @return The generic type parameter instantiations of the wrapped
   *         {@link Executable}, or their inference variables if not yet
   *         instantiated.
   */
  Stream<TypeParameter<?>> getTypeParameters();

  /**
   * @return The generic type parameter instantiations of the wrapped
   *         {@link Executable}, or their inference variables if not yet
   *         instantiated.
   */
  Stream<TypeArgument<?>> getTypeArguments();

  /**
   * Derive a new {@link ExecutableToken} instance from this, with the given
   * instantiation substituted for the given {@link TypeVariable}.
   * 
   * <p>
   * The substitution will only succeed if it is compatible with the bounds on
   * that type variable, and if it is more specific than the current type of the
   * type variable, whether it is an {@link InferenceVariable}, a
   * {@link TypeVariableCapture}, or another class of {@link Type}.
   * 
   * <p>
   * For example, the following method could be used to derive instances of
   * TypeToken over different parameterizations of {@code List<?>} at runtime.
   * 
   * <pre>
   * <code>
   * public TypeToken&lt;List&lt;T&gt;&gt; getListType(TypeToken&lt;T&gt; elementType)} {
   *   return new TypeToken&lt;T&gt;()} {}.withTypeArguments(new TypeParameter&lt;T&gt;() {}.as(elementType));
   * }
   * </code>
   * </pre>
   * 
   * <p>
   * This behavior is different from {@link #withTypeArguments(List)}, which
   * re-instantiates every parameter on the declaration rather than performing a
   * substitution for arbitrary type variables.
   * 
   * @param arguments
   *          the type variable instantiations
   * @return a new derived {@link ExecutableToken} instance with the given
   *         instantiation substituted for the given type variable
   */
  default S withTypeArguments(TypeArgument<?>... arguments) {
    return withTypeArguments(Arrays.asList(arguments));
  }

  /**
   * Derive a new {@link ExecutableToken} instance from this, with types
   * substituted according to the given arguments.
   * 
   * <p>
   * More specifically, each of the given arguments represents a type variable and
   * an instantiation for that type variable. Occurrences of those type variables
   * in the declaration will be substituted for their instantiations in the
   * derived declaration.
   * 
   * <p>
   * The substitution will only succeed if it results in a valid parameterization
   * of the declaration.
   * 
   * <p>
   * For example, the following method could be used to derive instances of
   * TypeToken over different parameterizations of {@code List<?>} at runtime.
   * 
   * <pre>
   * <code>
   * public TypeToken&lt;List&lt;T&gt;&gt; getListType(TypeToken&lt;T&gt; elementType)} {
   *   return new TypeToken&lt;T&gt;()} {}.withTypeArguments(new TypeParameter&lt;T&gt;() {}.as(elementType));
   * }
   * </code>
   * </pre>
   * 
   * @param arguments
   *          the type variable instantiations
   * @return a new derived {@link ExecutableToken} instance with the given
   *         instantiation substituted for the given type variable
   */
  S withTypeArguments(Collection<? extends TypeArgument<?>> arguments);

  /**
   * Resolve the instantiation of the given type variable in the context of this
   * declaration.
   * 
   * @param <U>
   *          the type of the type variable to resolve
   * @param parameter
   *          the type parameter
   * @return the argument of the given parameter with respect to this declaration
   */
  @SuppressWarnings("unchecked")
  default <U> TypeArgument<U> resolveTypeArgument(TypeParameter<U> parameter) {
    return getAllTypeArguments()
        .filter(a -> a.getParameter().getType().equals(parameter.getType()))
        .findAny()
        .map(p -> (TypeArgument<U>) p)
        .orElseThrow(
            () -> new ReflectionException(
                REFLECTION_PROPERTIES.cannotResolveTypeVariable(parameter.getType(), this)));
  }

  /**
   * @see #resolveTypeArgument(TypeParameter)
   */
  @SuppressWarnings("javadoc")
  default Type resolveTypeArgument(TypeVariable<?> parameter) {
    return resolveTypeArgument(forTypeVariable(parameter)).getType();
  }

  /**
   * @see #withTypeArguments(List)
   */
  @SuppressWarnings("javadoc")
  DeclarationToken<?> withAllTypeArguments(Type... typeArguments);

  /**
   * @see #withTypeArguments(List)
   */
  @SuppressWarnings("javadoc")
  DeclarationToken<?> withTypeArguments(Type... typeArguments);

  /**
   * Derive a new {@link DeclarationToken} instance with the given type argument
   * parameterization.
   * 
   * <p>
   * The types in the given list correspond, in order, to the
   * {@link #getTypeParameters() type parameters} of this declaration. The current
   * parameterization of the declaration is substituted for that given.
   * 
   * <p>
   * Each substitution will only succeed if it is compatible with the bounds on
   * that type variable, and if it is more specific than the current argument,
   * whether it is an {@link InferenceVariable}, a {@link TypeVariableCapture}, or
   * another kind of {@link Type}.
   * 
   * <p>
   * This behavior is different from {@link #withTypeArguments(Collection)}, which
   * performs a substitution for arbitrary type variables rather than
   * re-instantiating every parameter on the declaration.
   * 
   * @param typeArguments
   *          a list of arguments for each generic type parameter of the
   *          underlying declaration
   * @return a new derived {@link DeclarationToken} instance with the given
   *         instantiations substituted for each generic type parameter, in order
   */
  DeclarationToken<?> withAllTypeArguments(List<Type> typeArguments);

  /**
   * As @see {@link #withAllTypeArguments(List)}, but only providing arguments for
   * the parameters occurring directly on the declaration.
   */
  @SuppressWarnings("javadoc")
  DeclarationToken<?> withTypeArguments(List<Type> typeArguments);
}
