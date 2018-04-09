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
import static java.util.stream.Collectors.toMap;
import static uk.co.strangeskies.collection.stream.StreamUtilities.zip;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.Types.isSubtype;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;
import static uk.co.strangeskies.reflection.token.ExecutableToken.forConstructor;
import static uk.co.strangeskies.reflection.token.ExecutableToken.forMethod;
import static uk.co.strangeskies.reflection.token.FieldToken.forField;
import static uk.co.strangeskies.reflection.token.TypeParameter.forTypeVariable;
import static uk.co.strangeskies.reflection.token.TypeToken.Wildcards.RETAIN;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.tuple.Pair;
import uk.co.strangeskies.reflection.AnnotatedParameterizedTypes;
import uk.co.strangeskies.reflection.AnnotatedTypeSubstitution;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Annotations;
import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.Imports;
import uk.co.strangeskies.reflection.InferenceVariable;
import uk.co.strangeskies.reflection.InferenceVariableBounds;
import uk.co.strangeskies.reflection.ParameterizedTypes;
import uk.co.strangeskies.reflection.PrimitiveTypes;
import uk.co.strangeskies.reflection.TypeHierarchy;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.TypeSubstitution;
import uk.co.strangeskies.reflection.TypeVariableCapture;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.WildcardTypes;
import uk.co.strangeskies.utility.DeepCopyable;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * <p>
 * TypeToken provides reflective operations and services over the Java type
 * system. It is analogous to {@code Class<?>}, but provides access to a much
 * richer set of tools, and can be used over the domain of all types, not just
 * raw types.
 * 
 * <p>
 * TypeToken is effectively immutable, though may perform shared caching of
 * results transparently to the user. Like Class, A TypeToken will always be
 * parameterized with the type it reflects over when used as intended.
 * 
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          This is the type which the TypeToken object references.
 */
public class TypeToken<T> implements DeepCopyable<TypeToken<T>>, ReifiedType<TypeToken<T>>,
    DeclarationToken<TypeToken<T>> {
  /**
   * Treatment of wildcards for {@link TypeToken}s created over parameterized
   * types.
   * 
   * @author Elias N Vasylenko
   */
  public enum Wildcards {
    /**
     * Wildcards will be left alone, though capture may be necessary for
     * incorporation into backing {@link TypeResolver}, as wildcards alone do not
     * always fully specify valid bounds.
     */
    RETAIN(Retain.class),

    /**
     * Wildcards should be substituted with inference variables, with appropriate
     * bounds incorporated based on both type variable bounds and wildcard bounds.
     */
    INFER(Infer.class),

    /**
     * Wildcards should be substituted with fresh {@link TypeVariableCapture}
     * instances, as per
     * {@link TypeVariableCapture#captureWildcardArguments(ParameterizedType)} .
     */
    CAPTURE(Capture.class);

    private final Annotation annotation;

    private Wildcards(Class<? extends Annotation> annotationClass) {
      this.annotation = Annotations.from(annotationClass);
    }

    /**
     * @return An instance of the annotation which describes this behavior for
     *         capture of type literals.
     */
    public Annotation getAnnotation() {
      return annotation;
    }
  }

  /**
   * Specifies behavior of wildcards. If the annotated type is a wildcard type, it
   * will behave according to {@link Wildcards#RETAIN}, and if it is a
   * parameterized type, this rule will apply to its parameters instead.
   * Annotations on wildcards directly override annotations on declaring types.
   * 
   * @author Elias N Vasylenko
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE_USE)
  public @interface Retain {}

  /**
   * Specifies behavior of wildcards. If the annotated type is a wildcard type, it
   * will behave according to {@link Wildcards#INFER}, and if it is a
   * parameterized type, this rule will apply to its parameters instead.
   * Annotations on wildcards directly override annotations on declaring types.
   * 
   * @author Elias N Vasylenko
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE_USE)
  public @interface Infer {}

  /**
   * Specifies behavior of wildcards. If the annotated type is a wildcard type, it
   * will behave according to {@link Wildcards#CAPTURE}, and if it is a
   * parameterized type, this rule will apply to its parameters instead.
   * Annotations on wildcards directly override annotations on declaring types.
   * 
   * @author Elias N Vasylenko
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE_USE)
  public @interface Capture {}

  private static final TypeToken<?> NULL_TYPE_TOKEN = new TypeToken<>(emptyBoundSet(), (Type) null);

  private final BoundSet bounds;
  private final Type type;
  private final AnnotatedType declaration;

  protected TypeToken() {
    declaration = resolveAnnotatedSuperclassParameter();

    Pair<BoundSet, Type> resolvedType = incorporateAnnotatedType(declaration);
    this.type = resolvedType.getRight();
    this.bounds = resolvedType.getLeft();
  }

  private TypeToken(BoundSet bounds, AnnotatedType annotatedType) {
    Objects.requireNonNull(annotatedType);

    declaration = AnnotatedTypes.wrap(annotatedType);

    Pair<BoundSet, Type> resolvedType;
    if (bounds == null)
      resolvedType = incorporateAnnotatedType(declaration);
    else
      resolvedType = incorporateAnnotatedType(new TypeResolver(bounds), declaration);

    this.type = resolvedType.getRight();
    this.bounds = resolvedType.getLeft();
  }

  private TypeToken(AnnotatedType annotatedType) {
    this(null, annotatedType);
  }

  protected TypeToken(Type type) {
    this(type, Wildcards.RETAIN);
  }

  private TypeToken(Type type, Wildcards wildcards) {
    this(null, type, wildcards);
  }

  private TypeToken(BoundSet bounds, Type type, Wildcards wildcards) {
    this(bounds, AnnotatedTypes.annotated(type, wildcards.getAnnotation()));
  }

  protected TypeToken(BoundSet bounds, Type type) {
    this.declaration = null;
    this.bounds = bounds;
    this.type = type;
  }

  private AnnotatedType resolveAnnotatedSuperclassParameter() {
    Class<?> subclass = getClass();

    AnnotatedType type;

    final Map<TypeVariable<?>, AnnotatedType> resolvedParameters = new HashMap<>();
    final AnnotatedTypeSubstitution resolvedParameterSubstitution = new AnnotatedTypeSubstitution()
        .where(t -> t instanceof AnnotatedTypeVariable, t -> resolvedParameters.get(t.getType()));

    do {
      AnnotatedType annotatedType = subclass.getAnnotatedSuperclass();
      annotatedType = AnnotatedTypes.wrap(annotatedType);

      if (annotatedType instanceof AnnotatedParameterizedType) {
        if (!resolvedParameters.isEmpty()) {
          type = resolvedParameterSubstitution.resolve(annotatedType);
        } else
          type = annotatedType;

        resolvedParameters.clear();
        AnnotatedParameterizedTypes
            .getAllTypeArguments((AnnotatedParameterizedType) type)
            .forEach(e -> resolvedParameters.put(e.getKey(), e.getValue()));

        Annotation defaultAnnotation = getWildcardsAnnotation(annotatedType);
        if (defaultAnnotation != null) {
          Iterator<Map.Entry<TypeVariable<?>, AnnotatedType>> parameterIterator = resolvedParameters
              .entrySet()
              .iterator();
          while (parameterIterator.hasNext()) {
            Map.Entry<TypeVariable<?>, AnnotatedType> parameter = parameterIterator.next();

            Annotation givenAnnotation = getWildcardsAnnotation(parameter.getValue());
            if (givenAnnotation == null) {
              parameter
                  .setValue(
                      AnnotatedTypes.annotated(parameter.getValue().getType(), defaultAnnotation));
            }
          }
        }
      } else {
        type = annotatedType;

        resolvedParameters.clear();
      }

      subclass = subclass.getSuperclass();
    } while (!subclass.equals(TypeToken.class));

    return resolvedParameters.values().iterator().next();
  }

  private Annotation getWildcardsAnnotation(AnnotatedType type) {
    Annotation annotation = type.getAnnotation(Retain.class);
    if (annotation == null)
      annotation = type.getAnnotation(Capture.class);
    if (annotation == null)
      annotation = type.getAnnotation(Infer.class);

    return annotation;
  }

  private static Pair<BoundSet, Type> incorporateAnnotatedType(
      TypeResolver resolver,
      AnnotatedType annotatedType) {
    Type type = substituteAnnotatedWildcards(new Isomorphism(), annotatedType, resolver);

    return new Pair<>(resolver.getBounds(), type);
  }

  private static Pair<BoundSet, Type> incorporateAnnotatedType(AnnotatedType annotatedType) {
    return incorporateAnnotatedType(new TypeResolver(), annotatedType);
  }

  private static Type substituteAnnotatedWildcards(
      Isomorphism isomorphism,
      AnnotatedType annotatedType,
      TypeResolver resolver) {
    Wildcards behavior = annotatedType.isAnnotationPresent(Retain.class)
        ? Wildcards.RETAIN
        : annotatedType.isAnnotationPresent(Infer.class)
            ? Wildcards.INFER
            : annotatedType.isAnnotationPresent(Capture.class)
                ? Wildcards.CAPTURE
                : Wildcards.RETAIN;

    if (annotatedType instanceof AnnotatedParameterizedType) {
      return substituteAnnotatedWildcardsForParameterizedType(
          isomorphism,
          behavior,
          (AnnotatedParameterizedType) annotatedType,
          resolver);
    } else if (annotatedType instanceof AnnotatedWildcardType) {
      return substituteAnnotatedWildcardsForWildcardType(
          isomorphism,
          behavior,
          (AnnotatedWildcardType) annotatedType,
          resolver);
    } else if (annotatedType instanceof AnnotatedArrayType) {
      return arrayFromComponent(
          substituteAnnotatedWildcards(
              isomorphism,
              ((AnnotatedArrayType) annotatedType).getAnnotatedGenericComponentType(),
              resolver));
    } else {
      return annotatedType.getType();
    }
  }

  private static ParameterizedType substituteAnnotatedWildcardsForParameterizedType(
      Isomorphism isomorphism,
      Wildcards behavior,
      AnnotatedParameterizedType annotatedType,
      TypeResolver resolver) {
    return isomorphism.byIdentity().getProxiedMapping(annotatedType, ParameterizedType.class, t -> {
      /*
       * Deal with annotations on types mentioned by parameters, preserving any
       * parameters which are wildcards themselves.
       */
      Type[] arguments = substituteAnnotatedWildcardsForEach(
          isomorphism,
          annotatedType.getAnnotatedActualTypeArguments(),
          resolver);

      /*
       * Collect all arguments into a mapping from type variables, including on
       * enclosing types.
       */
      Map<TypeVariable<?>, Type> allArguments = ParameterizedTypes
          .getAllTypeArguments((ParameterizedType) annotatedType.getType())
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      TypeVariable<?>[] parameters = Types
          .getErasedType(annotatedType.getType())
          .getTypeParameters();
      for (int i = 0; i < arguments.length; i++) {
        allArguments.put(parameters[i], arguments[i]);
      }

      /*
       * New parameterized type
       */
      ParameterizedType parameterizedType = ParameterizedTypes
          .parameterizeUnchecked(Types.getErasedType(annotatedType.getType()), allArguments::get);
      if (allArguments.values().stream().anyMatch(WildcardType.class::isInstance)) {
        if (behavior == Wildcards.CAPTURE) {
          parameterizedType = TypeVariableCapture.captureWildcardArguments(parameterizedType);
        } else if (behavior == Wildcards.INFER) {
          TypeResolver inferenceResolver = new TypeResolver(resolver.getBounds());
          parameterizedType = inferenceResolver.inferTypeArguments(parameterizedType);
          resolver.incorporateBounds(inferenceResolver.getBounds());
        }
      }
      return parameterizedType;
    });
  }

  private static Type substituteAnnotatedWildcardsForWildcardType(
      Isomorphism isomorphism,
      Wildcards behavior,
      AnnotatedWildcardType annotatedType,
      TypeResolver resolver) {
    AnnotatedWildcardType annotatedWildcardType = annotatedType;
    WildcardType wildcardType;

    if (annotatedWildcardType.getAnnotatedLowerBounds().length > 0) {
      wildcardType = WildcardTypes
          .wildcardSuper(
              substituteAnnotatedWildcardsForEach(
                  isomorphism,
                  annotatedWildcardType.getAnnotatedLowerBounds(),
                  resolver));

    } else if (annotatedWildcardType.getAnnotatedUpperBounds().length > 0) {
      wildcardType = WildcardTypes
          .wildcardExtending(
              substituteAnnotatedWildcardsForEach(
                  isomorphism,
                  annotatedWildcardType.getAnnotatedUpperBounds(),
                  resolver));

    } else {
      wildcardType = WildcardTypes.wildcard();
    }

    Type type;
    if (behavior != null) {
      switch (behavior) {
      case RETAIN:
        type = wildcardType;
        break;
      case INFER:
        type = resolver.inferWildcardType(wildcardType);
        break;
      case CAPTURE:
        type = TypeVariableCapture.captureWildcard(wildcardType);
        break;
      default:
        throw new AssertionError();
      }
    } else {
      type = wildcardType;
    }

    return type;
  }

  private static Type[] substituteAnnotatedWildcardsForEach(
      Isomorphism isomorphism,
      AnnotatedType[] annotatedTypes,
      TypeResolver resolver) {
    Type[] types = new Type[annotatedTypes.length];
    for (int i = 0; i < types.length; i++)
      types[i] = substituteAnnotatedWildcards(isomorphism, annotatedTypes[i], resolver);
    return types;
  }

  /**
   * Create a TypeToken for a raw class.
   * 
   * @param <T>
   *          the type of the new {@link TypeToken}
   * @param type
   *          the class to create a TypeToken for
   * @return a TypeToken over the requested class
   */
  public static <T> TypeToken<T> forClass(Class<T> type) {
    return new TypeToken<>(type);
  }

  /**
   * Create a TypeToken for an arbitrary type, preserving wildcards where
   * possible.
   * 
   * @param type
   *          the requested type
   * @return a TypeToken over the requested type
   */
  public static TypeToken<?> forType(Type type) {
    return new TypeToken<>(type);
  }

  /**
   * Create a TypeToken for an arbitrary type, preserving wildcards where
   * possible.
   * 
   * @param bounds
   *          the set of bounds on any inference variables mentioned in the type
   * @param type
   *          the requested type
   * @param wildcards
   *          how to deal with wildcard parameters on the given type
   * @return a TypeToken over the requested type
   */
  public static TypeToken<?> forType(BoundSet bounds, Type type, Wildcards wildcards) {
    return new TypeToken<>(bounds, type, wildcards);
  }

  /**
   * Create a TypeToken for an arbitrary type, preserving wildcards where
   * possible.
   * 
   * @param type
   *          the requested type
   * @return a TypeToken over the requested type
   */
  public static TypeToken<?> forAnnotatedType(AnnotatedType type) {
    return new TypeToken<>(type);
  }

  /**
   * Create a TypeToken for an arbitrary type.
   * 
   * @param type
   *          the requested type
   * @param wildcards
   *          how to deal with wildcard parameters on the given type
   * @return a TypeToken over the requested type
   */
  public static TypeToken<?> forType(Type type, Wildcards wildcards) {
    return new TypeToken<>(type, wildcards);
  }

  /**
   * Create a TypeToken over the null type.
   * 
   * @return a TypeToken over the null type
   */
  public static TypeToken<?> forNull() {
    return NULL_TYPE_TOKEN;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TypeToken && getType().equals(((TypeToken<?>) obj).getType());
  }

  @Override
  public int hashCode() {
    return getType().hashCode();
  }

  @Override
  public TypeToken<T> getThis() {
    return ReifiedType.super.getThis();
  }

  @Override
  public TypeToken<TypeToken<T>> getThisTypeToken() {
    return new TypeToken<TypeToken<T>>() {}
        .withTypeArguments(new TypeParameter<T>() {}.asType(this));
  }

  @Override
  public TypeToken<T> copy() {
    return this;
  }

  @Override
  public TypeToken<T> deepCopy(Isomorphism isomorphism) {
    return new TypeToken<>(
        bounds.deepCopy(isomorphism),
        new TypeSubstitution().withIsomorphism(isomorphism).resolve(getType()));
  }

  /**
   * Derive a new {@link TypeToken} instance, with the given bounds incorporated
   * into the bounds of the underlying resolver. The original {@link TypeToken}
   * will remain unmodified.
   * 
   * <p>
   * All bounds are incorporated if and only if they have the potential to affect
   * the resolution of inference variables mentioned by this type.
   * 
   * @param bounds
   *          the new bounds to incorporate
   * @return the newly derived {@link TypeToken}
   */
  public TypeToken<T> withBounds(BoundSet bounds) {
    return new TypeToken<>(this.bounds.withBounds(bounds), getType());
  }

  /**
   * Equivalent to the application of {@link TypeToken#forType(Type)} to the
   * result of {@link Types#fromString(String)}.
   * 
   * @param typeString
   *          the String to parse
   * @return a TypeToken representing the type described by the String
   */
  public static TypeToken<?> fromString(String typeString) {
    return forAnnotatedType(AnnotatedTypes.fromString(typeString));
  }

  /**
   * Equivalent to the application of {@link TypeToken#forType(Type)} to the
   * result of {@link AnnotatedTypes#fromString(String, Imports)}, with the given
   * imports.
   * 
   * @param typeString
   *          the String to parse
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from input
   * @return a TypeToken representing the type described by the string
   */
  public static TypeToken<?> fromString(String typeString, Imports imports) {
    return forAnnotatedType(AnnotatedTypes.fromString(typeString, imports));
  }

  /**
   * Equivalent to the application of
   * {@link AnnotatedTypes#toString(AnnotatedType, Imports)} to
   * {@link #getAnnotatedDeclaration()}, with the given imports.
   * 
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from output
   * @return a string representing the type described by this type token
   */
  public String toString(Imports imports) {
    return AnnotatedTypes.toString(getAnnotatedDeclaration(), imports);
  }

  /**
   * Create a TypeToken over a wildcard type which has the type represented by
   * this TypeToken as an upper bound.
   * 
   * For example, invoking this method on a {@code TypeToken<List<?>>} will give a
   * {@code TypeToken<? extends List<?>>}.
   * 
   * @return the TypeToken representing a capture of the wildcard described
   */
  public TypeToken<? extends T> getWildcardExtending() {
    return getExtending(Wildcards.RETAIN);
  }

  /**
   * Create a TypeToken over a wildcard type which has the type represented by
   * this TypeToken as an upper bound.
   * 
   * The wildcard will be represented according to the {@link Wildcards} argument
   * given. Either the wildcard will be preserved, a fresh
   * {@link TypeVariableCapture} of the wildcard type will be captured, or an
   * {@link InferenceVariable} will be substituted.
   * 
   * For example, invoking this method on a {@code TypeToken<List<?>>} will give a
   * {@code TypeToken<? extends List<?>>}.
   * 
   * @param wildcards
   *          how to deal with the wildcard parameter on the new type
   * @return the TypeToken representing a capture of the wildcard described
   */
  @SuppressWarnings("unchecked")
  public TypeToken<? extends T> getExtending(Wildcards wildcards) {
    if (wildcards == Wildcards.INFER) {
      TypeResolver resolver = new TypeResolver(this.bounds);
      InferenceVariable inferenceVariable = resolver
          .inferWildcardType(wildcardExtending(getType()));

      return new TypeToken<>(resolver.getBounds(), inferenceVariable);
    } else {
      return (TypeToken<? extends T>) forType(wildcardExtending(getType()), wildcards);
    }
  }

  /**
   * Create a TypeToken over a wildcard type which has the type represented by
   * this TypeToken as a lower bound.
   * 
   * For example, invoking this method on a {@code TypeToken<List<?>>} will give a
   * {@code TypeToken<? super List<?>>}.
   * 
   * @return the TypeToken representing a capture of the wildcard described
   */
  public TypeToken<? super T> getWildcardSuper() {
    return getSuper(Wildcards.RETAIN);
  }

  /**
   * Create a TypeToken over a wildcard type which has the type represented by
   * this TypeToken as a lower bound.
   * 
   * The wildcard will be represented according to the {@link Wildcards} argument
   * given. Either the wildcard will be preserved, a fresh
   * {@link TypeVariableCapture} of the wildcard type will be captured, or an
   * {@link InferenceVariable} will be substituted.
   * 
   * For example, invoking this method on a {@code TypeToken<List<?>>} will give a
   * {@code TypeToken<? super List<?>>}.
   * 
   * @param wildcards
   *          how to deal with the wildcard parameter on the new type
   * @return the TypeToken representing a capture of the wildcard described
   */
  @SuppressWarnings("unchecked")
  public TypeToken<? super T> getSuper(Wildcards wildcards) {
    if (wildcards == Wildcards.INFER) {
      TypeResolver resolver = new TypeResolver(this.bounds);
      InferenceVariable inferenceVariable = resolver.inferWildcardType(wildcardSuper(getType()));
      return new TypeToken<>(resolver.getBounds(), inferenceVariable);
    } else {
      return (TypeToken<? super T>) forType(WildcardTypes.wildcardSuper(getType()), wildcards);
    }
  }

  /**
   * See {@link Types#getErasedType(Type)}.
   * 
   * @return the raw type of the type represented by this TypeToken
   */
  @SuppressWarnings("unchecked")
  public Class<? super T> getErasedType() {
    return (Class<? super T>) getErasedUpperBounds().findFirst().orElse(Object.class);
  }

  /**
   * See {@link Types#getErasedType(Type)}.
   * 
   * @return the raw type of the type represented by this TypeToken
   */
  @SuppressWarnings("unchecked")
  public TypeToken<? super T> getErasedTypeToken() {
    if (isRaw()) {
      return this;
    } else {
      return (TypeToken<? super T>) forType(
          getErasedUpperBounds().findFirst().orElse(Object.class));
    }
  }

  /**
   * Find the upper bounding classes and parameterized types of a given type.
   * Unlike {@link Types#getUpperBounds(Type)} this respects bounds on the
   * inference variables in this resolver.
   * 
   * @return the upper bounds of the type represented by this TypeToken
   */
  public Stream<Type> getUpperBounds() {
    List<Type> upperBounds = Types.getUpperBounds(getType()).collect(toList());

    for (int i = 0; i < upperBounds.size(); i++) {
      Type upperBound = upperBounds.get(i);

      if (getBounds().containsInferenceVariable(upperBound)) {
        upperBounds.remove(upperBound);

        InferenceVariableBounds bounds = getBounds().getBoundsOn((InferenceVariable) upperBound);
        bounds
            .getUpperBounds()
            .filter(t -> !getBounds().containsInferenceVariable(t))
            .forEach(upperBounds::add);
      }
    }

    if (upperBounds.isEmpty())
      upperBounds.add(Object.class);

    return upperBounds.stream();
  }

  /**
   * Determine the raw types of a given type, accounting for inference variables
   * which may have instantiations or upper bounds within the context of this
   * resolver.
   * 
   * @return the raw types of the type represented by this TypeToken
   */
  public Stream<Class<?>> getErasedUpperBounds() {
    return getUpperBounds().map(Types::getErasedType);
  }

  /**
   * This method returns a copy of the Resolver backing by this TypeToken.
   * 
   * @return A new Resolver object containing whichever bounds have been
   *         internally derived from the type of this TypeToken.
   */
  public BoundSet getBounds() {
    return bounds;
  }

  @Override
  public String toString() {
    return Types.toString(getType());
  }

  /**
   * The type represented by this {@link TypeToken}.
   * 
   * @return The actual Type object described.
   */
  public Type getType() {
    return type;
  }

  /**
   * Is the type a primitive type as per the Java type system.
   * 
   * @return True if the type is primitive, false otherwise.
   */
  public boolean isPrimitive() {
    return PrimitiveTypes.isPrimitive(getType());
  }

  /**
   * Is the type a wrapper for a primitive type as per the Java type system.
   * 
   * @return True if the type is a primitive wrapper, false otherwise.
   */
  public boolean isPrimitiveWrapper() {
    return PrimitiveTypes.isPrimitiveWrapper(getType());
  }

  /**
   * If this TypeToken is a primitive type, determine the wrapped primitive type.
   * 
   * @return The wrapper type of the primitive type this TypeToken represents,
   *         otherwise this TypeToken itself.
   */
  @SuppressWarnings("unchecked")
  public TypeToken<T> wrapPrimitive() {
    if (isPrimitive())
      return (TypeToken<T>) forClass(PrimitiveTypes.wrapPrimitive(getErasedType()));
    else
      return this;
  }

  /**
   * If this TypeToken is a wrapper of a primitive type, determine the unwrapped
   * primitive type.
   * 
   * @return The primitive type wrapped by this TypeToken, otherwise this
   *         TypeToken itself.
   */
  @SuppressWarnings("unchecked")
  public TypeToken<T> unwrapPrimitive() {
    if (isPrimitiveWrapper())
      return (TypeToken<T>) forClass(PrimitiveTypes.unwrapPrimitive(getErasedType()));
    else
      return this;
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public TypeToken<T> withConstraintTo(Kind kind, Type type) {
    BoundSet bounds = new ConstraintFormula(kind, getType(), type).reduce(getBounds());

    return new TypeToken<>(bounds, getType());
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public TypeToken<T> withConstraintTo(Kind kind, TypeToken<?> type) {
    BoundSet bounds = getBounds().withBounds(type.getBounds());
    bounds = new ConstraintFormula(kind, getType(), type.getType()).reduce(bounds);

    return new TypeToken<>(bounds, getType());
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public TypeToken<T> withConstraintFrom(Kind kind, Type type) {
    BoundSet bounds = new ConstraintFormula(kind, type, getType()).reduce(getBounds());

    return new TypeToken<>(bounds, getType());
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public TypeToken<T> withConstraintFrom(Kind kind, TypeToken<?> type) {
    BoundSet bounds = getBounds().withBounds(type.getBounds());
    bounds = new ConstraintFormula(kind, type.getType(), getType()).reduce(bounds);

    return new TypeToken<>(bounds, getType());
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public boolean satisfiesConstraintTo(Kind kind, Type type) {
    try {
      withConstraintTo(kind, type);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public boolean satisfiesConstraintTo(Kind kind, TypeToken<?> type) {
    try {
      withConstraintTo(kind, type);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public boolean satisfiesConstraintFrom(Kind kind, Type type) {
    try {
      withConstraintFrom(kind, type);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Derive a new type from this one, with a constraint between this type and a
   * given type. The invocation will fail if the constraint cannot be satisfied.
   * For types which mention inference variables, this constraint may have an
   * effect on the bounds of those inference variables within the resulting type.
   * 
   * @param type
   *          the type to constrain against
   * @param kind
   *          the kind of the constraint formula
   * @return a new type token which satisfies the constraint
   */
  public boolean satisfiesConstraintFrom(Kind kind, TypeToken<?> type) {
    try {
      withConstraintFrom(kind, type);
      return true;
    } catch (Exception e) {
      return false;
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
  public TypeToken<? extends T> parameterize() {
    if (isRaw()) {
      return (TypeToken<T>) forType(ParameterizedTypes.parameterize(getErasedType()));
    } else {
      return this;
    }
  }

  /**
   * This method will attempt to substitute any inference variables mentioned by
   * this type with their instantiations, if instantiations are available, and
   * return a TypeToken over the resulting type.
   * 
   * @return A TypeToken with the fully inferred type.
   */
  public TypeToken<T> substituteInstantiations() {
    Type type = new TypeResolver(getBounds()).substituteInstantiations(getType());

    BoundSet bounds = getBounds();

    if (InferenceVariable.isProperType(type)) {
      bounds = emptyBoundSet();
    }

    return new TypeToken<>(bounds, type);
  }

  /**
   * If the type is raw it is parameterized with inference variables. Bounds are
   * incorporated according to those present on the type variables each argument
   * instantiates.
   * 
   * <p>
   * If the type is already parameterized, the existing arguments are substituted
   * according to their type. Bounds are incorporated according to those present
   * on the type variables each argument instantiates.
   * 
   * <ul>
   * <li>Substitute wildcards with inference variables, incorporating bounds
   * according to those present on the wildcard.</li>
   * 
   * <li>Do not substitute types which are not wildcards.</li>
   * </ul>
   * 
   * @return an inference over the exact type
   */
  @SuppressWarnings("unchecked")
  public TypeToken<? extends T> infer() {
    if (!isGeneric()) {
      return this;

    } else if (isRaw()) {
      TypeResolver resolver = new TypeResolver();
      Type type = resolver.inferTypeParameters(getErasedType());
      return (TypeToken<? extends T>) forType(resolver.getBounds(), type, RETAIN);

    } else if (type instanceof ParameterizedType) {
      TypeResolver resolver = new TypeResolver();
      Type type = resolver.inferTypeArguments((ParameterizedType) getType());
      return (TypeToken<? extends T>) forType(resolver.getBounds(), type, RETAIN);

    } else {
      return this;
    }
  }

  /**
   * This method will attempt to infer the actual type represented by this
   * TypeToken, which means the types of any inference variables mentioned will be
   * inferred and substituted. The receiver TypeToken instance will not be
   * changed.
   * 
   * @return A TypeToken with the fully inferred type.
   */
  public TypeToken<T> resolve() {
    TypeResolver resolver = new TypeResolver(getBounds());

    return new TypeToken<>(emptyBoundSet(), resolver.resolve(getType()));
  }

  /**
   * Determine whether this {@link TypeToken} represents a proper type.
   * 
   * @return True if the type is proper, false otherwise.
   */
  public boolean isProper() {
    return InferenceVariable.isProperType(getType());
  }

  /**
   * Determine which inference variables are mentioned by the type of this
   * {@link TypeToken}.
   * 
   * @return A set of all the inference variables which are contained within the
   *         bound set backing this {@link TypeToken} and which are mentioned by
   *         its type.
   */
  public Stream<InferenceVariable> getInferenceVariablesMentioned() {
    return InferenceVariable.getMentionedBy(getType());
  }

  /**
   * Determine which inference variables are dependencies of those mentioned by
   * the type of this {@link TypeToken}.
   * 
   * @return A set of all the dependencies of the inference variables which are
   *         contained within the bound set backing this {@link TypeToken} and
   *         which are mentioned by its type.
   */
  public Stream<InferenceVariable> getRemainingInferenceVariableDependencies() {
    return getInferenceVariablesMentioned()
        .flatMap(d -> getBounds().getBoundsOn(d).getRemainingDependencies());
  }

  /**
   * @return The annotated declaring type of this type token, if one exists, else
   *         an unannotated representation of the type of this type token.
   */
  public AnnotatedType getAnnotatedDeclaration() {
    return declaration != null ? declaration : annotated(getType());
  }

  /**
   * @param object
   *          an object to cast to this type
   * @return the given object cast to a reference of this type
   */
  @SuppressWarnings("unchecked")
  public T cast(Object object) {
    if (!isSubtype(object.getClass(), intersectionOf(getErasedUpperBounds().collect(toList())))) {
      throw new ClassCastException();
    }
    return (T) object;
  }

  /**
   * @param type
   *          the type to determine castability to
   * @return true if the cast is possible, false otherwise
   */
  public boolean isCastableTo(TypeToken<?> type) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param type
   *          the type to determine castability from
   * @return true if the cast is possible, false otherwise
   */
  public boolean isCastableFrom(TypeToken<?> type) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param type
   *          the type to determine castability to
   * @return true if the cast is possible, false otherwise
   */
  public boolean isCastableTo(Type type) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param type
   *          the type to determine castability from
   * @return true if the cast is possible, false otherwise
   */
  public boolean isCastableFrom(Type type) {
    throw new UnsupportedOperationException();
  }

  /**
   * Find which fields can be resolved on this type.
   * 
   * @return a list of all {@link Field} objects applicable to this type, wrapped
   *         in {@link FieldToken} instances
   */
  public Stream<FieldToken<T, ?>> fields() {
    TypeHierarchy typeHierarchy = new TypeHierarchy(getType());

    return stream(getErasedType().getFields())
        .filter(f -> !Modifier.isStatic(f.getModifiers()))
        .map(
            f -> forField(f)
                .withReceiverType(
                    new TypeToken<>(
                        getBounds(),
                        typeHierarchy.resolveSupertype(f.getDeclaringClass()))));
  }

  /**
   * Find which fields are declared on this type.
   * 
   * @return a list of all {@link Field} objects applicable to this type, wrapped
   *         in {@link FieldToken} instances
   */
  public Stream<FieldToken<T, ?>> declaredFields() {
    return stream(getErasedType().getDeclaredFields())
        .filter(f -> !Modifier.isStatic(f.getModifiers()))
        .map(f -> forField(f).withReceiverType(this));
  }

  /**
   * Find which constructors can be invoked for this type.
   * 
   * @return a list of all {@link Constructor} objects applicable to this type,
   *         wrapped in {@link ExecutableToken} instances
   */
  public Stream<ExecutableToken<Void, T>> constructors() {
    return stream(getErasedType().getConstructors())
        .map(m -> forConstructor(m).withTargetType(this));
  }

  /**
   * Find which constructors can be invoked for this type.
   * 
   * @return a list of all {@link Constructor} objects applicable to this type,
   *         wrapped in {@link ExecutableToken} instances
   */
  public Stream<ExecutableToken<Void, T>> declaredConstructors() {
    return stream(getErasedType().getDeclaredConstructors())
        .map(m -> forConstructor(m).withTargetType(this));
  }

  /**
   * Find which methods of the given name can be invoked on instances of this
   * type.
   * 
   * @return a list of all {@link Method} objects applicable to this type, wrapped
   *         in {@link ExecutableToken} instances
   */
  public Stream<ExecutableToken<T, ?>> methods() {
    List<Class<?>> upperBounds = getErasedUpperBounds().collect(toList());

    Stream<Method> methodStream = upperBounds.stream().flatMap(t -> stream(t.getMethods()));

    if (upperBounds.stream().allMatch(Types::isInterface))
      methodStream = Stream.concat(methodStream, stream(Object.class.getMethods()));

    TypeHierarchy typeHierarchy = new TypeHierarchy(getType());

    return methodStream
        .filter(m -> !Modifier.isStatic(m.getModifiers()))
        .map(
            m -> forMethod(m)
                .withReceiverType(
                    new TypeToken<>(
                        getBounds(),
                        typeHierarchy.resolveSupertype(m.getDeclaringClass()))));
  }

  /**
   * Find which methods can be invoked on this type, whether statically or on
   * instances.
   * 
   * @return a list of all {@link Method} objects applicable to this type, wrapped
   *         in {@link ExecutableToken} instances
   */
  public Stream<ExecutableToken<T, ?>> declaredMethods() {
    return stream(getErasedType().getDeclaredMethods())
        .filter(m -> !Modifier.isStatic(m.getModifiers()))
        .map(m -> forMethod(m).withReceiverType(this));
  }

  @Override
  public boolean isRaw() {
    return getType() instanceof Class<?> && ((Class<?>) getType()).getTypeParameters().length > 0;
  }

  @Override
  public boolean isGeneric() {
    return isRaw() || getType() instanceof ParameterizedType;
  }

  @Override
  public int getTypeParameterCount() {
    if (getType() instanceof ParameterizedType) {
      return ((ParameterizedType) getType()).getActualTypeArguments().length;
    } else {
      return 0;
    }
  }

  @Override
  public Stream<TypeParameter<?>> getTypeParameters() {
    if (getType() instanceof ParameterizedType) {
      return Arrays.stream(getErasedType().getTypeParameters()).map(e -> forTypeVariable(e));
    } else {
      return Stream.empty();
    }
  }

  @Override
  public Stream<TypeArgument<?>> getTypeArguments() {
    if (getType() instanceof ParameterizedType) {

      Stream<TypeVariable<?>> parameters = Arrays.stream(getErasedType().getTypeParameters());
      Stream<Type> arguments = Arrays
          .stream(((ParameterizedType) getType()).getActualTypeArguments());

      return zip(parameters, arguments).map(e -> forTypeVariable(e.getKey()).asType(e.getValue()));
    } else {
      return Stream.empty();
    }
  }

  @Override
  public TypeToken<T> withTypeArguments(Collection<? extends TypeArgument<?>> arguments) {
    BoundSet bounds = arguments
        .stream()
        .map(TypeArgument::getTypeToken)
        .map(TypeToken::getBounds)
        .reduce(getBounds(), (a, b) -> a.withBounds(b));

    Map<TypeVariable<?>, Type> argumentMap = arguments
        .stream()
        .collect(toMap(a -> a.getParameter().getType(), TypeArgument::getType));

    return new TypeToken<>(bounds, new TypeSubstitution(argumentMap).resolve(getType()));
  }

  @Override
  public Optional<DeclarationToken<?>> getOwningDeclaration() {
    if (getType() instanceof ParameterizedType) {
      ParameterizedType type = (ParameterizedType) getType();
      Class<?> rawType = (Class<?>) type.getRawType();
      /*
       * Classes enclosed by constructors or methods cannot be parameterized with
       * anything other than their own type parameters.
       */
      if (rawType.getEnclosingConstructor() != null) {
        return Optional.of(ExecutableToken.forConstructor(rawType.getEnclosingConstructor()));

      } else if (rawType.getEnclosingMethod() != null) {
        return Optional.of(ExecutableToken.forMethod(rawType.getEnclosingMethod()));

      } else if (rawType.getEnclosingClass() != null) {
        return Optional.of(new TypeToken<>(getBounds(), type.getOwnerType()));

      } else {
        return Optional.empty();
      }

    } else if (getType() instanceof Class<?>) {
      Class<?> type = (Class<?>) getType();
      if (type.getEnclosingClass() != null) {
        return Optional.of(new TypeToken<>(emptyBoundSet(), type.getEnclosingClass()));

      } else {
        return Optional.empty();
      }

    } else if (getType() instanceof TypeVariable<?>) {
      GenericDeclaration enclosingDeclaration = ((TypeVariable<?>) getType())
          .getGenericDeclaration();

      if (enclosingDeclaration instanceof Class<?>) {
        Class<?> enclosingClass = (Class<?>) enclosingDeclaration;
        return Optional
            .of(
                forType(
                    Types.isGeneric(enclosingClass)
                        ? ParameterizedTypes.parameterize(enclosingClass)
                        : enclosingClass));

      } else if (enclosingDeclaration instanceof Method) {
        return Optional.of(ExecutableToken.forMethod((Method) enclosingDeclaration));

      } else if (enclosingDeclaration instanceof Constructor<?>) {
        return Optional.of(ExecutableToken.forConstructor((Constructor<?>) enclosingDeclaration));

      } else {
        return Optional.empty();
      }

    } else {
      return Optional.empty();
    }
  }

  /**
   * As @see {@link TypeHierarchy#resolveSupertype( Class)}.
   */
  @SuppressWarnings({ "unchecked", "javadoc" })
  public TypeToken<? super T> resolveSupertype(Class<?> superclass) {
    TypeToken<?> superType = forType(new TypeHierarchy(getType()).resolveSupertype(superclass));

    return (TypeToken<? super T>) superType;
  }

  @Override
  public TypeToken<T> withTypeArguments(List<Type> typeArguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeToken<T> withAllTypeArguments(List<Type> typeArguments) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeToken<T> withTypeArguments(Type... typeArguments) {
    return withTypeArguments(asList(typeArguments));
  }

  @Override
  public TypeToken<T> withAllTypeArguments(Type... typeArguments) {
    return withAllTypeArguments(asList(typeArguments));
  }
}
