/*
 * Copyright (C) 2017 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.collection.stream.StreamUtilities.entriesToMap;
import static uk.co.strangeskies.collection.stream.StreamUtilities.streamOptional;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.collection.stream.StreamUtilities.tryOptional;
import static uk.co.strangeskies.collection.stream.StreamUtilities.zip;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.LOOSE_COMPATIBILILTY;
import static uk.co.strangeskies.reflection.ConstraintFormula.Kind.SUBTYPE;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.Types.getErasedType;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;
import static uk.co.strangeskies.reflection.token.TypeToken.forType;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.stream.StreamUtilities;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.Types;

/**
 * <p>
 * A type safe wrapper around {@link Executable} instances, with proper handling
 * of generic methods, and methods on generic classes.
 * 
 * <p>
 * {@link ExecutableToken executable members} may be created over types which
 * mention inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <O>
 *          the receiver type of the executable
 * @param <R>
 *          the return type of the executable
 */
public class ExecutableToken<O, R> implements MemberToken<O, ExecutableToken<O, R>> {
  private final Executable executable;
  private final List<Type> typeArguments;

  private final TypeToken<? super O> receiverType;
  private final TypeToken<? extends R> returnType;
  private List<ExecutableParameter> parameters;

  private final boolean variableArityInvocation;

  protected ExecutableToken(Class<?> instance, Constructor<?> constructor) {
    this(instance, constructor.getDeclaringClass(), constructor);
  }

  protected ExecutableToken(Class<?> instance, Method method) {
    this(instance, method.getReturnType(), method);
  }

  @SuppressWarnings("unchecked")
  private ExecutableToken(Class<?> receiverType, Class<?> returnType, Executable executable) {
    this.executable = executable;
    this.typeArguments = null;

    this.receiverType = (TypeToken<? super O>) forClass(receiverType);
    this.returnType = (TypeToken<? extends R>) forClass(returnType);
    this.parameters = null;

    this.variableArityInvocation = false;
  }

  /*
   * To avoid unnecessary recalculation, or a huge list of constructors, the
   * receiver type, return type, type arguments etc. are assumed to be in a
   * consistent state at this point. The responsibility to ensure this is with the
   * caller.
   */
  protected ExecutableToken(
      TypeToken<? super O> receiverType,
      TypeToken<? extends R> returnType,
      List<ExecutableParameter> parameters,
      List<Type> typeArguments,
      Executable executable,
      boolean variableArityInvocation) {
    this.executable = executable;
    this.variableArityInvocation = variableArityInvocation;
    this.typeArguments = typeArguments;
    this.receiverType = receiverType;
    this.returnType = returnType;
    this.parameters = parameters;
  }

  /**
   * Create a new {@link ExecutableToken} instance from a reference to a
   * {@link Constructor} of an outer or static class.
   * 
   * <p>
   * If the method is generic it will be parameterized with its own type
   * variables.
   * 
   * @param constructor
   *          the constructor to wrap
   * @return an executable member wrapping the given constructor
   */
  public static ExecutableToken<Void, ?> forConstructor(Constructor<?> constructor) {
    if (!Modifier.isStatic(constructor.getDeclaringClass().getModifiers())
        && constructor.getDeclaringClass().getEnclosingClass() != null) {
      throw new ReflectionException(REFLECTION_PROPERTIES.declaringClassMustBeStatic(constructor));
    }
    return new ExecutableToken<>(void.class, constructor);
  }

  /**
   * Create a new {@link ExecutableToken} instance from a reference to a
   * {@link Constructor} of an inner class.
   * 
   * <p>
   * If the constructor or the enclosing class are generic they will be
   * parameterized with their own type variables.
   * 
   * @param constructor
   *          the constructor to wrap
   * @return an executable member wrapping the given method
   */
  public static ExecutableToken<?, ?> forInnerConstructor(Constructor<?> constructor) {
    return new ExecutableToken<>(constructor.getDeclaringClass().getEnclosingClass(), constructor);
  }

  /**
   * Create a new {@link ExecutableToken} instance from a reference to a static
   * {@link Method}.
   * 
   * <p>
   * If the method is generic it will be parameterized with its own type
   * variables.
   * 
   * @param method
   *          the method to wrap
   * @return an executable member wrapping the given method
   */
  public static ExecutableToken<Void, ?> forStaticMethod(Method method) {
    if (!Modifier.isStatic(method.getModifiers())) {
      throw new ReflectionException(REFLECTION_PROPERTIES.methodMustBeStatic(method));
    }
    return new ExecutableToken<>(void.class, method);
  }

  /**
   * Create a new {@link ExecutableToken} instance from a reference to an instance
   * {@link Method}.
   * 
   * <p>
   * If the method or its declaring class are generic they will be parameterized
   * with their own type variables.
   * 
   * @param method
   *          the method to wrap
   * @return an executable member wrapping the given method
   */
  public static ExecutableToken<?, ?> forMethod(Method method) {
    return new ExecutableToken<>(method.getDeclaringClass(), method);
  }

  public static ExecutableToken<?, ?> forExecutable(Executable executable) {
    return executable instanceof Method
        ? forMethod((Method) executable)
        : forConstructor((Constructor<?>) executable);
  }

  /**
   * If the invocation is raw, the target type and method are parameterized with
   * inference variables. Bounds are incorporated according to those present on
   * the type variables each argument instantiates.
   * 
   * <p>
   * If the invocation is already parameterized, the existing arguments are
   * substituted according to their type. Bounds are incorporated according to
   * those present on the type variables each argument instantiates.
   * 
   * <ul>
   * <li>Substitute wildcards with inference variables, incorporating bounds
   * according to those present on the wildcard.</li>
   * 
   * <li>Do not substitute types which are not wildcards.</li>
   * </ul>
   * 
   * @return an inference over the exact invocation type
   */
  public ExecutableToken<? extends O, R> infer() {
    if (!isGeneric()) {
      return this;

    } else if (isRaw()) {
      return partialParameterization(emptyBoundSet(), emptyMap());

    } else {
      return this;
    }
  }

  /**
   * If the declaration is raw, parameterize it with its own type parameters,
   * otherwise return the declaration itself.
   * 
   * @return the parameterized version of the declaration where applicable, else
   *         the unmodified declaration
   */
  @SuppressWarnings("unchecked")
  public ExecutableToken<? extends O, R> parameterize() {
    if (isRaw()) {
      return (ExecutableToken<? extends O, R>) getParameterizedFromRaw();
    } else {
      return this;
    }
  }

  private ExecutableToken<?, R> getParameterizedFromRaw() {
    @SuppressWarnings("unchecked")
    TypeToken<Object> receiverType = (TypeToken<Object>) getReceiverType()
        .getErasedTypeToken()
        .parameterize();

    @SuppressWarnings("unchecked")
    TypeToken<? extends R> returnType = isConstructor()
        ? getReturnType().parameterize()
        : (TypeToken<? extends R>) forType(((Method) getMember()).getGenericReturnType());

    return new ExecutableToken<>(
        receiverType,
        returnType,
        Arrays
            .stream(getMember().getParameters())
            .map(p -> new ExecutableParameter(p, forType(p.getParameterizedType())))
            .collect(toList()),
        asList(getMember().getTypeParameters()),
        getMember(),
        isVariableArityInvocation());
  }

  @Override
  public Executable getMember() {
    return executable;
  }

  @Override
  public Optional<TypeToken<?>> getOwningDeclaration() {
    return Optional.of(isConstructor() ? getReturnType() : getReceiverType());
  }

  /**
   * @return the name of the executable member
   */
  @Override
  public String getName() {
    return getMember().getName();
  }

  @Override
  public BoundSet getBounds() {
    return getOwningDeclaration().get().getBounds();
  }

  @Override
  public String toString() {
    return toString(getParametersImpl());
  }

  private String toString(List<ExecutableParameter> parameters) {
    StringBuilder builder = new StringBuilder();

    getVisibility().getKeyword().ifPresent(visibility -> builder.append(visibility).append(" "));

    if (isNative())
      builder.append("native ");
    if (isStatic())
      builder.append("static ");
    if (isStrict())
      builder.append("strictfp ");
    if (isSynchronized())
      builder.append("synchronized ");

    if (isAbstract())
      builder.append("abstract ");
    else if (isFinal())
      builder.append("final ");

    if (isGeneric()) {
      builder
          .append("<")
          .append(
              getTypeArguments().map(TypeArgument::getType).map(Objects::toString).collect(
                  joining(", ")))
          .append("> ");
    }

    builder.append(returnType).toString();
    if (getMember() instanceof Method)
      builder.append(" ").append(receiverType).append(".").append(getMember().getName());

    return builder
        .append("(")
        .append(parameters.stream().map(Objects::toString).collect(joining(", ")))
        .append(")")
        .toString();
  }

  /**
   * @return true if the wrapped executable is abstract, false otherwise
   */
  public boolean isAbstract() {
    return Modifier.isAbstract(getMember().getModifiers());
  }

  /**
   * @return true if the wrapped executable is native, false otherwise
   */
  public boolean isNative() {
    return Modifier.isNative(getMember().getModifiers());
  }

  /**
   * @return true if the executable is a constructor, false otherwise
   */
  public boolean isConstructor() {
    return getMember() instanceof Constructor<?>;
  }

  /**
   * @return true if the executable is a method, false otherwise
   */
  public boolean isMethod() {
    return getMember() instanceof Method;
  }

  /**
   * @return true if the wrapped executable is strict, false otherwise
   */
  public boolean isStrict() {
    return Modifier.isStrict(getMember().getModifiers());
  }

  /**
   * @return true if the wrapped executable is synchronized, false otherwise
   */
  public boolean isSynchronized() {
    return Modifier.isSynchronized(getMember().getModifiers());
  }

  /**
   * @return true if the wrapped executable is generic, false otherwise
   */
  @Override
  public boolean isGeneric() {
    return getMember().getTypeParameters().length > 0
        || getOwningDeclaration().map(DeclarationToken::isGeneric).orElse(false);
  }

  /**
   * @return true if the wrapped executable is variable arity, false otherwise
   */
  public boolean isVariableArityDefinition() {
    return getMember().isVarArgs();
  }

  /**
   * Check whether the executable is flagged to be invoked with varargs. If an
   * executable is flagged to be invoked with varargs, then the
   * {@link #invoke(Object, List) invocation} will be made by putting trailing
   * arguments into an array as per Java variable arity method invocation rules.
   * 
   * @return true if the executable is flagged to be invoked with varargs, false
   *         otherwise
   */
  public boolean isVariableArityInvocation() {
    return variableArityInvocation;
  }

  /**
   * @return copy of the {@link ExecutableToken} flagged to be invoked with
   *         {@link #isVariableArityInvocation() variable arity}
   */
  public ExecutableToken<O, R> asVariableArityInvocation() {
    if (isVariableArityInvocation()) {
      return this;
    } else if (!isVariableArityDefinition()) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invalidVariableArityInvocation(getMember()));
    } else {
      return new ExecutableToken<>(
          receiverType,
          returnType,
          parameters,
          typeArguments,
          executable,
          true);
    }
  }

  @Override
  public TypeToken<? super O> getReceiverType() {
    return receiverType;
  }

  /**
   * @return The exact return type of this executable member instance. Generic
   *         type parameters may include inference variables.
   */
  public TypeToken<? extends R> getReturnType() {
    return returnType;
  }

  /**
   * @return The exact types of the expected parameters of this executable member
   *         instance. Inference variables may be mentioned.
   */
  public Stream<ExecutableParameter> getParameters() {
    return getParametersImpl().stream();
  }

  public List<ExecutableParameter> getParametersImpl() {
    if (parameters == null)
      parameters = Arrays
          .stream(executable.getParameters())
          .map(p -> new ExecutableParameter(p, forType(p.getType())))
          .collect(toList());
    return parameters;
  }

  /**
   * Derive a new {@link ExecutableToken} instance, with the given bounds
   * incorporated into the bounds of the underlying resolver. The original
   * {@link ExecutableToken} will remain unmodified.
   * 
   * @param bounds
   *          The new bounds to incorporate.
   * @return The newly derived {@link ExecutableToken}.
   */
  @Override
  public ExecutableToken<O, R> withBounds(BoundSet bounds) {
    if (isRaw() || bounds.isEmpty()) {
      return this;
    } else {
      return withTypeSubstitution(getBounds().withBounds(bounds), new TypeSubstitution());
    }
  }

  @Override
  public ExecutableToken<? extends O, R> withTypeArguments(Type... typeArguments) {
    return withTypeArguments(asList(typeArguments));
  }

  @Override
  public ExecutableToken<? extends O, R> withAllTypeArguments(Type... typeArguments) {
    return withAllTypeArguments(asList(typeArguments));
  }

  @SuppressWarnings("unchecked")
  @Override
  public ExecutableToken<? extends O, R> withTypeArguments(List<Type> typeArguments) {
    if (typeArguments.size() != getTypeParameterCount()) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.incorrectTypeArgumentCount(
              getTypeParameters().map(TypeParameter::getType).collect(toList()),
              typeArguments));
    }

    if (isRaw()) {
      Map<TypeVariable<?>, Type> argumentMap = zip(
          getTypeParameters().map(TypeParameter::getType),
          typeArguments.stream()).collect(entriesToMap());

      return (ExecutableToken<? extends O, R>) partialParameterization(getBounds(), argumentMap);

    } else {
      BoundSet bounds = zip(
          typeArguments.stream(),
          getTypeArguments(),
          (a, b) -> new ConstraintFormula(Kind.EQUALITY, a, b.getType()))
              .reduce(getBounds(), (b, c) -> c.reduce(b), throwingReduce());

      return withBounds(bounds);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ExecutableToken<O, R> withAllTypeArguments(List<Type> typeArguments) {
    if (typeArguments.size() != getAllTypeParameterCount()) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.incorrectTypeArgumentCount(
              getAllTypeParameters().map(TypeParameter::getType).collect(toList()),
              typeArguments));
    }

    if (isRaw()) {
      ExecutableToken<?, ?> executable = getParameterizedFromRaw().withTypeArguments(
          zip(getAllTypeParameters(), typeArguments.stream(), TypeParameter::asType)
              .collect(toList()));

      return (ExecutableToken<O, R>) executable;
    } else {
      BoundSet bounds = zip(
          typeArguments.stream(),
          getAllTypeArguments(),
          (a, b) -> new ConstraintFormula(Kind.EQUALITY, a, b.getType()))
              .reduce(getBounds(), (b, c) -> c.reduce(b), throwingReduce());

      return withBounds(bounds);
    }
  }

  @Override
  public ExecutableToken<?, R> withReceiverType(Type type) {
    return withReceiverType(forType(type));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <U> ExecutableToken<U, R> withReceiverType(TypeToken<U> type) {
    if (!receiverType.isGeneric()) {
      if (!receiverType.satisfiesConstraintFrom(SUBTYPE, type)) {
        throw new ReflectionException(
            REFLECTION_PROPERTIES.cannotResolveOverride(getMember(), type.getType()));
      }

      return (ExecutableToken<U, R>) this;
    }

    if (isRaw()) {
      Class<?> rawType = getReceiverType().getErasedType();
      TypeToken<? super U> receiverType = type.resolveSupertype(rawType);

      return partialParameterization(
          type.getBounds(),
          receiverType.getAllTypeArguments().collect(
              toMap(a -> a.getParameter().getType(), TypeArgument::getType)));

    } else {
      return (ExecutableToken<U, R>) withBounds(
          new ConstraintFormula(Kind.SUBTYPE, type.getType(), receiverType.getType())
              .reduce(getBounds().withBounds(type.getBounds())));
    }
  }

  @SuppressWarnings("unchecked")
  private <P, Q> ExecutableToken<P, Q> partialParameterization(
      BoundSet bounds,
      Map<TypeVariable<?>, Type> argumentMap) {
    TypeResolver resolver = new TypeResolver(bounds);

    resolver.inferTypeParameters(executable, argumentMap).forEach(
        e -> argumentMap.put(e.getKey(), e.getValue()));

    return (ExecutableToken<P, Q>) getParameterizedFromRaw()
        .withTypeSubstitution(resolver.getBounds(), new TypeSubstitution(argumentMap));
  }

  /**
   * As @see {@link #withTargetType(TypeToken)}.
   */
  @SuppressWarnings("javadoc")
  public <S extends R> ExecutableToken<O, S> withTargetType(Class<S> target) {
    return withTargetType(TypeToken.forClass(target));
  }

  public ExecutableToken<O, ?> withTargetType(Type target) {
    if (target == null)
      return this;

    return withTargetType(forType(target));
  }

  /**
   * Derive a new instance of {@link ExecutableToken} with the given target type.
   * 
   * <p>
   * The new {@link ExecutableToken} will always have a target type which is as or
   * more specific than both the current target type <em>and</em> the given type.
   * This means that the new target will be assignment compatible with the given
   * type, but if the given type contains wildcards or inference variables which
   * are less specific that those implied by the <em>current</em> target type, new
   * type arguments will be inferred in their place, or further bounds may be
   * added to them.
   * 
   * @param <S>
   *          The derived {@link ExecutableToken} must be assignment compatible
   *          with this type.
   * @param target
   *          The derived {@link ExecutableToken} must be assignment compatible
   *          with this type.
   * @return A new {@link ExecutableToken} compatible with the given target type.
   * 
   *         <p>
   *         The new target type will not be effectively more specific than the
   *         intersection type of the current target type and the given type. That
   *         is, any type which can be assigned to both the given type and the
   *         current target type, will also be assignable to the new type.
   */
  @SuppressWarnings("unchecked")
  public <S> ExecutableToken<O, S> withTargetType(TypeToken<S> target) {
    if (!isGeneric()) {
      if (!returnType.satisfiesConstraintTo(LOOSE_COMPATIBILILTY, target)) {
        throw new ReflectionException(
            REFLECTION_PROPERTIES.cannotResolveOverride(getMember(), target.getType()));
      }
      return (ExecutableToken<O, S>) this;

    } else if (target.getType() == null) {
      return (ExecutableToken<O, S>) this;

    } else if (isRaw() && isConstructor()) {
      if (returnType.getType() == target.getErasedType()) {
        return (ExecutableToken<O, S>) partialParameterization(
            target.getBounds(),
            target.getAllTypeArguments().collect(
                toMap(a -> a.getParameter().getType(), TypeArgument::getType)));

      } else {
        return this.<O, S>partialParameterization(target.getBounds(), emptyMap()).withTargetType(
            target);

      }
    } else {
      return (ExecutableToken<O, S>) withBounds(
          new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, returnType.getType(), target.getType())
              .reduce(getBounds().withBounds(target.getBounds())));
    }
  }

  /**
   * Get the overriding method in the given type, or the same method if there is
   * no override.
   * 
   * @param <U>
   *          the type possibly containing the override
   * @param type
   *          the type possibly containing the override
   * @return the override
   */
  @SuppressWarnings("unchecked")
  public <U> ExecutableToken<U, R> getOverride(TypeToken<U> type) {
    if (isConstructor()) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotOverrideConstructor(getMember(), type.getType()));
    }

    boolean matchingRawType = (type.getType() instanceof Class<?>
        || type.getType() instanceof ParameterizedType)
        && type.getErasedType().equals(receiverType.getErasedType());
    if (matchingRawType) {
      return withReceiverType(type);
    }

    /*
     * If there is a public override we can find out via getMethod, as even if the
     * erased signature is different there will be a bridge method with the same
     * erased signature.
     * 
     * TODO the bridge method may exist for another reason and not be a true
     * override. Should we return the bridge in this case? What if the bridge
     * somehow *hides* an actual override?
     */
    Method override = type
        .getUpperBounds()
        .flatMap(
            t -> stream(getErasedType(t).getMethods())
                .filter(m -> m.getName().equals(getName()))
                .filter(m -> Arrays.equals(m.getParameterTypes(), getMember().getParameterTypes())))
        .reduce(
            (a, b) -> Types.isAssignable(a.getGenericReturnType(), b.getGenericReturnType())
                ? a
                : b)
        .orElse(null);

    if (override == null) {
      /*
       * Either there is no override, or there is a non-public override, so we have to
       * check the declared methods on each superclass. Again, if the erased signature
       * is different we will still find a bridge.
       */
      override = StreamUtilities
          .<Class<?>>iterate(type.getErasedType(), Class::getSuperclass)
          .flatMap(
              t -> streamOptional(
                  tryOptional(
                      () -> getErasedType(t)
                          .getDeclaredMethod(getName(), getMember().getParameterTypes()))))
          .findFirst()
          .orElse(null);
    }

    if (override == null) {
      return withReceiverType(type);
    }

    if (override.isBridge()) {
      /*
       * TODO find which method the bridge is to.
       */
    } else {
      /*
       * TODO this is the exact override
       */
    }

    ExecutableToken<U, ?> overrideToken = forMethod(override)
        .withReceiverType(type.withConstraintTo(SUBTYPE, receiverType));

    return (ExecutableToken<U, R>) overrideToken;
  }

  public Stream<ExecutableToken<O, ? super R>> getOverridden() {
    if (isConstructor()) {
      return Stream.empty();
    } else {
      // TODO
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Derived a new {@link ExecutableToken} instance with generic method parameters
   * inferred, and if this is a member of a generic type, with generic type
   * parameters inferred, too.
   * 
   * @return The derived {@link ExecutableToken} with inferred invocation type.
   */
  @Override
  public ExecutableToken<O, R> resolve() {
    TypeResolver resolver = new TypeResolver(getBounds());
    resolver.resolve();
    return withBounds(resolver.getBounds());
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a strict invocation
   * context. Where necessary, the derived {@link ExecutableToken} may infer new
   * bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a strict compatibility invocation context, we throw an
   *         exception. Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withStrictApplicability(TypeToken<?>... arguments) {
    return withStrictApplicability(Arrays.asList(arguments));
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a strict invocation
   * context. Where necessary, the derived {@link ExecutableToken} may infer new
   * bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a strict compatibility invocation context, we throw an
   *         exception. Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withStrictApplicability(
      Collection<? extends TypeToken<?>> arguments) {
    // TODO && make sure no boxing/unboxing occurs!

    return withLooseApplicability(arguments);
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a loose invocation
   * context. Where necessary, the derived {@link ExecutableToken} may infer new
   * bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a loose compatibility invocation context, we throw an
   *         exception. Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withLooseApplicability(TypeToken<?>... arguments) {
    return withLooseApplicability(Arrays.asList(arguments));
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a loose invocation
   * context. Where necessary, the derived {@link ExecutableToken} may infer new
   * bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a loose compatibility invocation context, we throw an
   *         exception. Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withLooseApplicability(
      Collection<? extends TypeToken<?>> arguments) {
    return withLooseApplicability(false, arguments);
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a variable arity
   * invocation context. Where necessary, the derived {@link ExecutableToken} may
   * infer new bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a variable arity invocation context, we throw an exception.
   *         Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withVariableArityApplicability(TypeToken<?>... arguments) {
    return withVariableArityApplicability(Arrays.asList(arguments));
  }

  /**
   * Derive a new {@link ExecutableToken} instance with inferred invocation type
   * such that it is compatible with the given arguments in a variable arity
   * invocation context. Where necessary, the derived {@link ExecutableToken} may
   * infer new bounds or instantiations on type parameters.
   * 
   * @param arguments
   *          The argument types of an invocation of this {@link ExecutableToken}.
   * @return If the given parameters are not compatible with this executable
   *         member in a variable arity invocation context, we throw an exception.
   *         Otherwise, we return the derived {@link ExecutableToken}.
   */
  public ExecutableToken<O, R> withVariableArityApplicability(
      Collection<? extends TypeToken<?>> arguments) {
    return asVariableArityInvocation().withLooseApplicability(true, arguments);
  }

  private ExecutableToken<O, R> withLooseApplicability(
      boolean variableArity,
      Collection<? extends TypeToken<?>> arguments) {
    if (variableArity) {
      if (getParametersImpl().size() > arguments.size() + 1) {
        throw new ReflectionException(
            REFLECTION_PROPERTIES.cannotResolveInvocationType(
                getMember(),
                arguments.stream().map(TypeToken::getType).collect(toList())));
      }
    } else if (getParametersImpl().size() != arguments.size()) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotResolveInvocationType(
              getMember(),
              arguments.stream().map(TypeToken::getType).collect(toList())));
    }

    TypeResolver resolver = new TypeResolver(getBounds());

    if (!getParametersImpl().isEmpty()) {
      Iterator<ExecutableParameter> parameters = getParametersImpl().iterator();
      Type nextParameter = parameters.next().getType();
      Type parameter = nextParameter;
      for (TypeToken<?> argument : arguments) {
        if (nextParameter != null) {
          parameter = nextParameter;
          if (parameters.hasNext()) {
            nextParameter = parameters.next().getType();
          } else if (variableArity) {
            parameter = Types.getComponentType(parameter);
            nextParameter = null;
          }
        }

        resolver.incorporateBounds(argument.getBounds());
        resolver.reduceConstraint(
            new ConstraintFormula(Kind.LOOSE_COMPATIBILILTY, argument.getType(), parameter));
      }

      // Test resolution is possible.
      resolver.copy().resolve();
    }

    return withBounds(resolver.getBounds());
  }

  /**
   * @return The generic type parameter instantiations of the wrapped
   *         {@link Executable}, or their inference variables if not yet
   *         instantiated.
   */
  @Override
  public Stream<TypeParameter<?>> getTypeParameters() {
    return stream(getMember().getTypeParameters()).map(TypeParameter::forTypeVariable);
  }

  /**
   * @return The generic type parameter instantiations of the wrapped
   *         {@link Executable}, or their inference variables if not yet
   *         instantiated.
   */
  @Override
  public Stream<TypeArgument<?>> getTypeArguments() {
    return zip(getTypeParameters(), typeArguments.stream(), TypeParameter::asType);
  }

  @Override
  public int getTypeParameterCount() {
    return getMember().getTypeParameters().length;
  }

  @Override
  public ExecutableToken<O, R> withTypeArguments(Collection<? extends TypeArgument<?>> arguments) {
    if (arguments.isEmpty()) {
      return this;
    }

    TypeSubstitution typeSubstitution = new TypeSubstitution(
        arguments.stream().collect(toMap(a -> a.getParameter().getType(), TypeArgument::getType)));

    typeSubstitution = typeSubstitution.where(
        getBounds()::containsInferenceVariable,
        t -> getBounds().getBoundsOn((InferenceVariable) t).getInstantiation().orElse(null));

    return withTypeSubstitution(getBounds(), typeSubstitution);
  }

  protected ExecutableToken<O, R> withTypeSubstitution(
      BoundSet bounds,
      TypeSubstitution typeSubstitution) {
    return new ExecutableToken<>(
        determineReceiverType(bounds, typeSubstitution),
        determineReturnType(bounds, typeSubstitution),
        determineParameterTypes(bounds, typeSubstitution),
        determineTypeArguments(typeSubstitution),
        executable,
        variableArityInvocation);
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

  private TypeToken<? extends R> determineReturnType(
      BoundSet bounds,
      TypeSubstitution typeArguments) {
    if (getReturnType().getType() instanceof Class<?>) {
      return getReturnType();
    } else {
      return new TypeToken<>(bounds, typeArguments.resolve(getReturnType().getType()));
    }
  }

  private List<ExecutableParameter> determineParameterTypes(
      BoundSet bounds,
      TypeSubstitution typeSubstitution) {
    return getParameters()
        .map(
            p -> new ExecutableParameter(
                p.getParameter(),
                new TypeToken<>(
                    bounds,
                    typeSubstitution.resolve(p.getParameter().getParameterizedType()))))
        .collect(toList());
  }

  private List<Type> determineTypeArguments(TypeSubstitution typeSubstitution) {
    return isRaw() ? null : typeArguments.stream().map(typeSubstitution::resolve).collect(toList());
  }

  /**
   * Invoke the wrapped {@link Executable} on the given receiver and with the
   * given parameters. The receiver will be ignored for static methods or
   * constructors. Variable arity invocation is not attempted.
   * 
   * <p>
   * Due to erasure of the types of the arguments, there is a limit to what type
   * checking can be performed at runtime. For type safe invocation, wrap
   * arguments in {@link TypedObject} instances and use an overload of
   * {@link #invokeSafely(Object, TypedObject...)} instead.
   * 
   * 
   * @param receiver
   *          the receiving object for the invocation. This parameter will be
   *          ignored in the case of a constructor invocation or other static
   *          method invocation
   * @param arguments
   *          the argument list for the invocation
   * @return the result of the invocation
   */
  public R invoke(O receiver, Object... arguments) {
    try {
      if (variableArityInvocation) {
        int regularArgumentCount = getParametersImpl().size() - 1;

        Object[] actualArguments = new Object[getParametersImpl().size()];
        Object[] varargs = (Object[]) Array.newInstance(
            getParametersImpl().get(regularArgumentCount).getErasure().getComponentType(),
            arguments.length - regularArgumentCount);

        System.arraycopy(arguments, 0, actualArguments, 0, regularArgumentCount);
        actualArguments[actualArguments.length - 1] = varargs;

        System.arraycopy(
            arguments,
            0,
            varargs,
            regularArgumentCount,
            arguments.length - regularArgumentCount);

        return invokeImpl(receiver, actualArguments);
      } else {
        return invokeImpl(receiver, arguments);
      }
    } catch (
        IllegalArgumentException
        | InstantiationException
        | IllegalAccessException
        | InvocationTargetException e) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invocationFailed(getMember(), receiverType.getType(), arguments),
          e);
    }
  }

  /**
   * Invoke the wrapped {@link Executable} on the given receiver and with the
   * given parameters. The receiver will be ignored for static methods or
   * constructors. Variable arity invocation is not attempted.
   * 
   * <p>
   * Due to erasure of the types of the arguments, there is a limit to what type
   * checking can be performed at runtime. For type safe invocation, wrap
   * arguments in {@link TypedObject} instances and use an overload of
   * {@link #invokeSafely(Object, TypedObject...)} instead.
   * 
   * 
   * @param receiver
   *          the receiving object for the invocation. This parameter will be
   *          ignored in the case of a constructor invocation or other static
   *          method invocation
   * @param arguments
   *          the argument list for the invocation
   * @return the result of the invocation
   */
  public R invoke(O receiver, List<? extends Object> arguments) {
    return invoke(receiver, arguments.toArray());
  }

  @SuppressWarnings("unchecked")
  protected R invokeImpl(O receiver, Object[] arguments) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    if (isConstructor()) {
      if (!getReceiverType().getType().equals(void.class)) {
        Object[] argumentsWithReceiver = new Object[arguments.length + 1];
        argumentsWithReceiver[0] = receiver;
        System.arraycopy(arguments, 0, argumentsWithReceiver, 1, arguments.length);
        arguments = argumentsWithReceiver;
      }

      return (R) ((Constructor<?>) getMember()).newInstance(arguments);
    } else {
      return (R) ((Method) getMember()).invoke(receiver, arguments);
    }
  }

  /**
   * <p>
   * As {@link #invoke(Object, Object...)}, but with arguments passed with their
   * exact types, meaning full type checking can be performed at runtime. Also,
   * here it is possible to determine whether the invocation is intended to be
   * variable arity, and if so an attempt is made to invoke as such.
   * 
   * <p>
   * If the expected parameter types of this executable member contain inference
   * variables or type variable captures, an attempt will be made to satisfy their
   * bounds according to the given argument types.
   * 
   * @param receiver
   *          the receiving object for the invocation. This parameter will be
   *          ignored in the case of a constructor invocation or other static
   *          method invocation
   * @param arguments
   *          the typed argument list for the invocation
   * @return the result of the invocation
   */
  public R invokeSafely(O receiver, TypedObject<?>... arguments) {
    return invokeSafely(receiver, Arrays.asList(arguments));
  }

  /**
   * <p>
   * As {@link #invoke(Object, Object...)}, but with arguments passed with their
   * exact types, meaning full type checking can be performed at runtime. Also,
   * here it is possible to determine whether the invocation is intended to be
   * variable arity, and if so an attempt is made to invoke as such.
   * 
   * <p>
   * If the expected parameter types of this executable member contain inference
   * variables or type variable captures, an attempt will be made to satisfy their
   * bounds according to the given argument types.
   * 
   * @param receiver
   *          the receiving object for the invocation. This parameter will be
   *          ignored in the case of a constructor invocation or other static
   *          method invocation
   * @param arguments
   *          the typed argument list for the invocation
   * @return the result of the invocation
   */
  public R invokeSafely(O receiver, List<? extends TypedObject<?>> arguments) {
    for (int i = 0; i < arguments.size(); i++)
      if (!arguments.get(i).getTypeToken().satisfiesConstraintTo(
          LOOSE_COMPATIBILILTY,
          getParametersImpl().get(i).getType())) {
        int finalI = i;
        throw new ReflectionException(
            REFLECTION_PROPERTIES.incompatibleArgument(
                arguments.get(finalI).getObject(),
                arguments.get(finalI).getTypeToken().getType(),
                getParametersImpl().get(finalI).getType(),
                finalI,
                getMember()));
      }
    return invoke(receiver, arguments);
  }

  /**
   * Find which methods can be invoked on this type, whether statically or on
   * instances.
   * 
   * @param declaringClass
   *          the declaring class for which to retrieve the methods
   * @return all {@link Method} objects applicable to this type, wrapped in
   *         {@link ExecutableToken} instances
   */
  public static Stream<ExecutableToken<Void, ?>> staticMethods(Class<?> declaringClass) {
    return stream(declaringClass.getDeclaredMethods())
        .filter(m -> Modifier.isStatic(m.getModifiers()))
        .map(ExecutableToken::forStaticMethod);
  }
}
