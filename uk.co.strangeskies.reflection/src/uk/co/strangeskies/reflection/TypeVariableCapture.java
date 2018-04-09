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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.IntersectionTypes.getTypes;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.IntersectionTypes.uncheckedIntersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterizeUnchecked;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A representation of an unknown instantiation of a type variable or inference
 * variable which is known to satisfy a certain set of upper and lower bonds.
 * 
 * @author Elias N Vasylenko
 */
public class TypeVariableCapture implements Type {
  private final static AtomicLong COUNTER = new AtomicLong();

  private final String name;

  private final Type source;

  private Type[] upperBounds;
  private final Type[] lowerBounds;

  TypeVariableCapture(Type[] upperBounds, Type[] lowerBounds, Type source) {
    this.name = "CAP#" + COUNTER.incrementAndGet();

    this.upperBounds = upperBounds.clone();
    this.lowerBounds = lowerBounds.clone();

    this.source = source;
  }

  private final void validate() {
    if (lowerBounds.length > 0 && !Types
        .isAssignable(uncheckedIntersectionOf(lowerBounds), uncheckedIntersectionOf(upperBounds))) {
      throw new ReflectionException(REFLECTION_PROPERTIES.invalidTypeVariableCaptureBounds(this));
    }
  }

  public String getName() {
    return name;
  }

  /**
   * @return The {@link InferenceVariable} or {@link WildcardType} captured by
   *         this type variable capture.
   */
  public Type getCapturedType() {
    return source;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getName());

    if (upperBounds.length > 0 && !(upperBounds.length == 1 && upperBounds[0] == null))
      builder.append(" extends ").append(uncheckedIntersectionOf(upperBounds));

    if (lowerBounds.length > 0 && !(lowerBounds.length == 1 && lowerBounds[0] == null))
      builder.append(" super ").append(uncheckedIntersectionOf(lowerBounds));

    return builder.toString();
  }

  /**
   * Determine whether a given type is a valid instantiation of this
   * TypeVariableCapture, or in other words, whether it is contained by the bounds
   * of this capture.
   * 
   * @param type
   *          The potential instantiation to validate.
   * @return True if the instantiation is valid, false otherwise.
   */
  public boolean isPossibleInstantiation(Type type) {
    return Arrays.stream(getUpperBounds()).allMatch(b -> Types.isAssignable(type, b))
        && !Arrays.stream(getLowerBounds()).allMatch(b -> !Types.isAssignable(b, type));
  }

  /**
   * @return The upper bounds of this TypeVariableCapture.
   */
  public Type[] getUpperBounds() {
    return upperBounds.clone();
  }

  /**
   * @return The lower bounds of this TypeVariableCapture.
   */
  public Type[] getLowerBounds() {
    return lowerBounds.clone();
  }

  static <T extends Type> void substituteBounds(Map<T, Type> captures) {
    TypeSubstitution substitution = new TypeSubstitution(captures);

    for (T type : captures.keySet()) {
      if (captures.get(type) instanceof TypeVariableCapture) {
        TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

        for (int i = 0; i < capture.upperBounds.length; i++) {
          capture.upperBounds[i] = substitution.resolve(capture.upperBounds[i]);
        }

        for (int i = 0; i < capture.lowerBounds.length; i++)
          capture.lowerBounds[i] = substitution.resolve(capture.lowerBounds[i]);

        capture.validate();
      } else {
        Type capture = substitution.resolve(captures.get(type));
        if (capture instanceof IntersectionType)
          capture = intersectionOf(capture);
        captures.put(type, capture);
      }
    }

    for (T type : captures.keySet()) {
      if (captures.get(type) instanceof TypeVariableCapture) {
        TypeVariableCapture capture = (TypeVariableCapture) captures.get(type);

        Type upperBound = new TypeBounds().greatestLowerBound(capture.upperBounds);
        capture.upperBounds = IntersectionTypes.getTypes(upperBound);

        if (!InferenceVariable.isProperType(capture)) {
          throw new ReflectionException(REFLECTION_PROPERTIES.improperCaptureType(capture));
        }
      }
    }
  }

  /**
   * Capture new type variable instantiations over any wildcard arguments of the
   * given generic array type.
   * 
   * @param type
   *          The generic array type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static GenericArrayType captureWildcardArguments(GenericArrayType type) {
    Type component = Types.getInnerComponentType(type);

    if (component instanceof ParameterizedType)
      component = captureWildcardArguments((GenericArrayType) component);

    component = type = (GenericArrayType) arrayFromComponent(
        component,
        Types.getArrayDimensions(type));

    return type;
  }

  /**
   * Capture new type variable instantiations over any wildcard arguments of the
   * given parameterized type.
   * 
   * @param type
   *          The parameterized type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static ParameterizedType captureWildcardArguments(ParameterizedType type) {
    LinkedHashMap<TypeVariable<?>, Type> arguments = new LinkedHashMap<>();

    getAllTypeArguments(type).forEach(e -> arguments.put(e.getKey(), e.getValue()));

    boolean containsWildcards = false;

    for (Map.Entry<TypeVariable<?>, Type> argument : arguments.entrySet()) {
      Type capture;

      if (argument.getValue() instanceof WildcardType) {
        containsWildcards = true;
        capture = captureWildcard(argument.getKey(), (WildcardType) argument.getValue(), false);
      } else {
        capture = argument.getValue();
      }

      argument.setValue(capture);
    }

    ParameterizedType capture;
    if (containsWildcards) {
      substituteBounds(arguments);
      capture = parameterizeUnchecked(
          (Class<?>) type.getRawType(),
          new ArrayList<>(arguments.values()));
    } else {
      capture = type;
    }

    return capture;
  }

  /**
   * Capture new type variable instantiation over a given wildcard type.
   * 
   * @param typeVariable
   *          The type variable the new capture of the given wildcard is intended
   *          to represent an instantiation of.
   * @param type
   *          The parameterized type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static TypeVariableCapture captureWildcard(
      TypeVariable<?> typeVariable,
      WildcardType type) {
    return captureWildcard(typeVariable, type, true);
  }

  /**
   * Capture new type variable instantiation over a given wildcard type.
   * 
   * @param type
   *          The parameterized type whose arguments we wish to capture.
   * @return A new parameterized type of the same class as the passed type,
   *         parameterized with the captures of the original arguments.
   */
  public static TypeVariableCapture captureWildcard(WildcardType type) {
    TypeVariable<GenericDeclaration> typeVariable = new TypeVariable<GenericDeclaration>() {
      @Override
      public <T extends Annotation> T getAnnotation(Class<T> arg0) {
        return null;
      }

      @Override
      public Annotation[] getAnnotations() {
        return new Annotation[0];
      }

      @Override
      public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
      }

      @Override
      public AnnotatedType[] getAnnotatedBounds() {
        return new AnnotatedType[0];
      }

      @Override
      public Type[] getBounds() {
        return new Type[0];
      }

      @Override
      public GenericDeclaration getGenericDeclaration() {
        return null;
      }

      @Override
      public String getName() {
        return "?";
      }
    };

    return captureWildcard(typeVariable, type, true);
  }

  private static TypeVariableCapture captureWildcard(
      TypeVariable<?> typeVariable,
      WildcardType type,
      boolean validate) {
    Type upperBound;

    List<Type> aggregation = new ArrayList<>(
        typeVariable.getBounds().length + type.getUpperBounds().length);
    for (int i = 0; i < typeVariable.getBounds().length; i++)
      aggregation.add(typeVariable.getBounds()[i]);
    for (int i = 0; i < type.getUpperBounds().length; i++)
      aggregation.add(type.getUpperBounds()[i]);

    if (validate)
      upperBound = intersectionOf(aggregation);
    else
      upperBound = uncheckedIntersectionOf(aggregation);

    Type[] upperBounds = IntersectionTypes.getTypes(upperBound);

    TypeVariableCapture capture = new TypeVariableCapture(upperBounds, type.getLowerBounds(), type);

    if (validate)
      capture.validate();

    return capture;
  }

  /**
   * Capture fresh type variables as valid stand-in instantiations for a set of
   * inference variables.
   * 
   * @param types
   *          The inference variables to capture.
   * @param bounds
   *          The context from which to determine the current bounds on the given
   *          inference variables, and to incorporate new bounds into.
   * @return A mapping from the inference variables passes to their new captures.
   */
  public static BoundSet captureInferenceVariables(
      Collection<? extends InferenceVariable> types,
      BoundSet bounds) {
    TypeSubstitution properTypeSubstitutuion = properTypeSubstitution(types, bounds);

    Map<InferenceVariable, Type> typeVariableCaptures = new HashMap<>();
    for (InferenceVariable inferenceVariable : types) {
      Optional<Type> existingMatch = bounds
          .getBoundsOn(inferenceVariable)
          .getEqualities()
          .filter(typeVariableCaptures::containsKey)
          .findAny();

      if (existingMatch.isPresent()) {
        /*
         * We have already captured an inference variable equal with this one
         */
        typeVariableCaptures.put(inferenceVariable, typeVariableCaptures.get(existingMatch.get()));
      } else {
        Set<Type> equalitySet = new HashSet<>();

        for (Type equality : bounds
            .getBoundsOn(inferenceVariable)
            .getEqualities()
            .collect(toList())) {
          if (!(equality instanceof InferenceVariable)) {
            try {
              equalitySet.add(properTypeSubstitutuion.resolve(equality));
            } catch (ReflectionException e) {
              equalitySet.clear();
              break;
            }
          }
        }

        if (!equalitySet.isEmpty()) {
          typeVariableCaptures.put(inferenceVariable, intersectionOf(equalitySet));
        } else {
          /*
           * For all i (1 ≤ i ≤ n), if αi has one or more proper lower bounds L1, ..., Lk,
           * then let the lower bound of Yi be lub(L1, ..., Lk); if not, then Yi has no
           * lower bound.
           */
          List<Type> lowerBoundSet = bounds
              .getBoundsOn(inferenceVariable)
              .getLowerBounds()
              .filter(InferenceVariable::isProperType)
              .collect(toList());

          Type[] lowerBounds;
          if (lowerBoundSet.isEmpty())
            lowerBounds = new Type[0];
          else {
            Type lub = new TypeBounds().leastUpperBound(lowerBoundSet);
            lowerBounds = getTypes(lub);
          }

          /*
           * For all i (1 ≤ i ≤ n), where αi has upper bounds U1, ..., Uk, let the upper
           * bound of Yi be glb(U1 θ, ..., Uk θ), where θ is the substitution [α1:=Y1,
           * ..., αn:=Yn].
           */

          BoundSet finalBounds = bounds;
          Set<Type> upperBoundSet = bounds
              .getBoundsOn(inferenceVariable)
              .getUpperBounds()
              .map(t -> {
                try {
                  return properTypeSubstitutuion.resolve(t);
                } catch (ReflectionException e) {
                  throw new ReflectionException(
                      REFLECTION_PROPERTIES.improperUpperBound(t, inferenceVariable, finalBounds),
                      e);
                }
              })
              .collect(Collectors.toSet());

          /*
           * no need to be checked properly here, as we do this later in #substituteBounds
           */
          Type[] upperBounds = upperBoundSet.toArray(new Type[upperBoundSet.size()]);

          /*
           * If the type variables Y1, ..., Yn do not have well-formed bounds (that is, a
           * lower bound is not a subtype of an upper bound, or an intersection type is
           * inconsistent), then resolution fails.
           */
          TypeVariableCapture capture = new TypeVariableCapture(
              upperBounds,
              lowerBounds,
              inferenceVariable);

          typeVariableCaptures.put(inferenceVariable, capture);
        }
      }
    }

    substituteBounds(typeVariableCaptures);

    return bounds.withInstantiations(typeVariableCaptures);
  }

  private static TypeSubstitution properTypeSubstitution(
      Collection<? extends InferenceVariable> types,
      BoundSet bounds) {
    return new TypeSubstitution().where(InferenceVariable.class::isInstance, i -> {
      InferenceVariableBounds inferenceVariableBounds = bounds.getBoundsOn((InferenceVariable) i);

      /*
       * The intent of this substitution is to replace all instances of inference
       * variables with proper forms where possible.
       * 
       * Otherwise, if the inference variable is not contained within the set to be
       * captured we search for a non-proper equality which only mentions inference
       * variables which *are* in the set to be captured.
       * 
       * The wider purpose of this is to try to ensure that inference variables in the
       * upper bound may be substituted with captures wherever possible, such that the
       * bound is ultimately proper.
       * 
       * TODO may need to rethink approach to cases like the recent compiler-dev issue
       */
      Type replacement;
      if (inferenceVariableBounds.getInstantiation().isPresent()) {
        replacement = inferenceVariableBounds.getInstantiation().get();
      } else if (!types.contains(i)) {
        replacement = inferenceVariableBounds
            .getEqualities()
            .filter(types::contains)
            .findAny()
            .orElse(
                inferenceVariableBounds
                    .getEqualities()
                    .filter(
                        equality -> InferenceVariable
                            .getMentionedBy(equality)
                            .allMatch(types::contains))
                    .findAny()
                    .orElseThrow(
                        () -> new ReflectionException(
                            REFLECTION_PROPERTIES.cannotFindSubstitution(i))));
      } else {
        replacement = i;
      }

      return replacement;
    });
  }
}
