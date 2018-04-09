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

import static java.util.Arrays.stream;
import static java.util.stream.Stream.of;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.PrimitiveTypes.isPrimitive;
import static uk.co.strangeskies.reflection.PrimitiveTypes.unwrapPrimitive;
import static uk.co.strangeskies.reflection.PrimitiveTypes.wrapPrimitive;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.text.parsing.Parser;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of general utility methods relating to the Java type system.
 * Some utilities related to more specific classes of type may be found in
 * {@link WildcardTypes}, {@link ParameterizedTypes}, and {@link ArrayTypes}..
 * 
 * @author Elias N Vasylenko
 */
public final class Types {
  private static final TypeParser TYPE_PARSER = new TypeParser(Imports.empty());

  private Types() {}

  /**
   * Determine whether a {@link Class} represents a generic class or an array with
   * a generic class as a component type.
   * 
   * @param type
   *          the type we wish to classify
   * @return true if the given class is generic or if a non-statically enclosing
   *         class is generic, false otherwise
   */
  public static boolean isGeneric(Class<?> type) {
    while (type.isArray()) {
      type = type.getComponentType();
    }

    do {
      if (type.getTypeParameters().length > 0)
        return true;
    } while (!Types.isStatic(type) && (type = type.getEnclosingClass()) != null);

    return false;
  }

  /**
   * Determine whether a type is raw, i.e. a generic type without a
   * parameterization.
   * 
   * @param type
   *          the type we wish to classify
   * @return true if the given class is raw or if a non-statically enclosing class
   *         is raw, false otherwise
   */
  public static boolean isRaw(Type type) {
    return type instanceof Class<?> && isGeneric((Class<?>) type);
  }

  /**
   * Get the erasure of the given type.
   * 
   * @param type
   *          The type of which we wish to determine the raw type.
   * @return The raw type of the type represented by this TypeToken.
   */
  public static Class<?> getErasedType(Type type) {
    if (type == null) {
      return null;
    } else if (type instanceof TypeVariableCapture) {
      Type[] bounds = ((TypeVariableCapture) type).getUpperBounds();
      if (bounds.length == 0)
        return Object.class;
      else
        return getErasedType(bounds[0]);
    } else if (type instanceof TypeVariable<?>) {
      Type[] bounds = ((TypeVariable<?>) type).getBounds();
      if (bounds.length == 0)
        return Object.class;
      else
        return getErasedType(bounds[0]);
    } else if (type instanceof InferenceVariable) {
      return Object.class;
    } else if (type instanceof WildcardType) {
      Type[] bounds = ((WildcardType) type).getUpperBounds();
      if (bounds.length == 0)
        return Object.class;
      else
        return getErasedType(bounds[0]);
    } else if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    } else if (type instanceof Class) {
      return (Class<?>) type;
    } else if (type instanceof GenericArrayType) {
      return Array
          .newInstance(getErasedType(((GenericArrayType) type).getGenericComponentType()), 0)
          .getClass();
    } else if (type instanceof IntersectionType) {
      return getErasedType(((IntersectionType) type).getTypes()[0]);
    } else {
      return Object.class;
    }
  }

  /**
   * Find the upper bounding classes and parameterized types of a given type.
   * 
   * @param type
   *          The type whose bounds we wish to discover.
   * @return The upper bounds of the given type.
   */
  public static Stream<Type> getUpperBounds(Type type) {
    Type[] types;

    if (type instanceof IntersectionType)
      types = ((IntersectionType) type).getTypes();

    else if (type instanceof WildcardType)
      types = ((WildcardType) type).getUpperBounds();

    else if (type instanceof TypeVariable)
      types = ((TypeVariable<?>) type).getBounds();

    else if (type instanceof TypeVariableCapture)
      types = ((TypeVariableCapture) type).getUpperBounds();

    else
      return of(type);

    return stream(types).flatMap(Types::getUpperBounds);
  }

  /**
   * Find the lower bounds of a given type.
   * 
   * @param type
   *          The type whose bounds we wish to discover.
   * @return The lower bounds of the given type, or null if no such bounds exist.
   */
  public static Stream<Type> getLowerBounds(Type type) {
    Type[] types;

    if (type instanceof IntersectionType)
      types = ((IntersectionType) type).getTypes();
    else if (type instanceof WildcardType)
      types = ((WildcardType) type).getLowerBounds();
    else if (type instanceof TypeVariableCapture)
      types = ((TypeVariableCapture) type).getLowerBounds();
    else
      types = new Type[] { type };

    return Arrays.stream(types);
  }

  /**
   * Determine whether a given class is abstract.
   * 
   * @param rawType
   *          The type we wish to classify.
   * @return True if the type is abstract, false otherwise.
   */
  public static boolean isAbstract(Class<?> rawType) {
    return Modifier.isAbstract(rawType.getModifiers());
  }

  /**
   * Determine whether a given class is final.
   * 
   * @param rawType
   *          The type we wish to classify.
   * @return True if the type is final, false otherwise.
   */
  public static boolean isFinal(Class<?> rawType) {
    return Modifier.isFinal(rawType.getModifiers());
  }

  /**
   * Determine whether a given class is an interface.
   * 
   * @param rawType
   *          The type we wish to classify.
   * @return True if the type is an interface, false otherwise.
   */
  public static boolean isInterface(Class<?> rawType) {
    return Modifier.isInterface(rawType.getModifiers());
  }

  /**
   * Determine the visibility of the type
   * 
   * @param type
   *          the type whose visibility we wish to determine
   * @return a {@link Visibility} object describing the type
   */
  public static Visibility getVisibility(Class<?> type) {
    int modifiers = type.getModifiers();

    if (Modifier.isPrivate(modifiers)) {
      return Visibility.PRIVATE;
    } else if (Modifier.isProtected(modifiers)) {
      return Visibility.PROTECTED;
    } else if (Modifier.isPublic(modifiers)) {
      return Visibility.PUBLIC;
    } else {
      return Visibility.PACKAGE_PRIVATE;
    }
  }

  /**
   * Determine whether a given class is static.
   * 
   * @param rawType
   *          The type we wish to classify.
   * @return True if the type is static, false otherwise.
   */
  public static boolean isStatic(Class<?> rawType) {
    return Modifier.isStatic(rawType.getModifiers());
  }

  /**
   * Find the component type of the given type, if the given {@link Type} instance
   * is an array {@link Class} or an instance of {@link GenericArrayType}.
   * 
   * @param type
   *          The type of which we wish to determine the component type.
   * @return The component type of the given type, if it is an array type,
   *         otherwise null.
   */
  public static Type getComponentType(Type type) {
    if (type instanceof Class)
      return ((Class<?>) type).getComponentType();
    else if (type instanceof GenericArrayType)
      return ((GenericArrayType) type).getGenericComponentType();
    else
      return null;
  }

  /**
   * Find the innermost component type of the given type, if the given
   * {@link Type} instance is an array {@link Class} or an instance of
   * {@link GenericArrayType} with any number of dimensions.
   * 
   * @param type
   *          The type of which we wish to determine the component type.
   * @return The component type of the given type if it is an array type,
   *         otherwise null.
   */
  public static Type getInnerComponentType(Type type) {
    Type component = null;

    if (type instanceof Class)
      while ((type = ((Class<?>) type).getComponentType()) != null)
        component = type;
    else
      while (type instanceof GenericArrayType
          && (type = ((GenericArrayType) type).getGenericComponentType()) != null)
        component = type;

    return component;
  }

  /**
   * Determine the number of array dimensions exist on the given type.
   * 
   * @param type
   *          A type which may or may not be an array.
   * @return The number of dimensions on the given type, or 0 if it is not an
   *         array type.
   */
  public static int getArrayDimensions(Type type) {
    int count = 0;

    if (type instanceof Class)
      while ((type = ((Class<?>) type).getComponentType()) != null)
        count++;
    else
      while (type instanceof GenericArrayType
          && (type = ((GenericArrayType) type).getGenericComponentType()) != null)
        count++;

    return count;
  }

  /**
   * Determine if a given type, {@code to}, is legally castable from another given
   * type, {@code from}.
   * 
   * @param from
   *          The type from which we wish to determine castability.
   * @param to
   *          The type to which we wish to determine castability.
   * @return True if the types are castable, false otherwise.
   */
  public static boolean isCastable(Type from, Type to) {
    throw new UnsupportedOperationException();
  }

  /**
   * If a given object is assignable to a given raw type, it will be converted to
   * that type. Generally this is an identity conversion, but for wrapped
   * primitive types the extra step is taken to make conversions to which are
   * consistent with widening primitive conversions.
   * 
   * @param <T>
   *          the type to which we wish to assign
   * @param object
   *          the type from which we wish to assign
   * @param type
   *          the type to which we wish to assign
   * @return true if the types are assignable, false otherwise
   */
  @SuppressWarnings("unchecked")
  public static <T> T assign(Object object, Class<T> type) {
    Class<?> currentType = unwrapPrimitive(object.getClass());
    Class<?> rawTargetType = unwrapPrimitive(getErasedType(type));

    if (isStrictInvocationContextCompatible(currentType, rawTargetType)) {
      if (isPrimitive(rawTargetType)) {
        /*
         * If assignable primitives:
         */
        if (rawTargetType.equals(double.class)) {
          object = ((Number) object).doubleValue();
        } else if (rawTargetType.equals(float.class)) {
          object = ((Number) object).floatValue();
        } else if (rawTargetType.equals(long.class)) {
          object = ((Number) object).longValue();
        } else if (rawTargetType.equals(int.class)) {
          object = ((Number) object).intValue();
        } else if (rawTargetType.equals(short.class)) {
          object = ((Number) object).shortValue();
        }
      }
    } else {
      Object finalObject = object; // Get your shit together Eclipse ffs.
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invalidAssignmentObject(finalObject, type));
    }

    return (T) object;
  }

  private static class EqualityRelation {
    private Type a, b;

    public EqualityRelation(Type a, Type b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof EqualityRelation))
        return false;

      EqualityRelation that = (EqualityRelation) obj;
      return (a == that.a && b == that.b) || (a == that.b && b == that.a);
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(a) ^ (System.identityHashCode(b) * 7);
    }
  }

  /**
   * Test whether two types are equal to one another.
   * 
   * @param a
   *          the first type
   * @param b
   *          the second type
   * @return true if the two given types are equal, false otherwise
   */
  public static boolean equals(Type a, Type b) {
    return equals(a, b, new Isomorphism());
  }

  private static boolean equals(Type[] first, Type[] second, Isomorphism isomorphism) {
    if (first.length != second.length) {
      return false;
    } else if (first.length == 0) {
      return true;
    } else if (first.length == 1) {
      return equals(first[0], second[0], isomorphism);
    } else {
      return stream(first).allMatch(f -> stream(second).anyMatch(s -> equals(f, s, isomorphism)));
    }
  }

  private static boolean equals(Type first, Type second, Isomorphism isomorphism) {
    if (first == second) {
      return true;
    } else if (first == null || second == null) {
      return false;
    }

    boolean equal = isomorphism
        .byEquality()
        .getPartialMapping(new EqualityRelation(first, second), () -> true, e -> {
          if (first == second) {
            return true;

          } else if (first instanceof ParameterizedType) {
            return second instanceof ParameterizedType && parameterizedTypeEquals(
                (ParameterizedType) first,
                (ParameterizedType) second,
                isomorphism);

          } else if (first instanceof IntersectionType) {
            return second instanceof IntersectionType && equals(
                ((IntersectionType) first).getTypes(),
                ((IntersectionType) second).getTypes(),
                isomorphism);

          } else if (first instanceof Class) {
            return first.equals(second);

          } else if (first instanceof WildcardType) {
            return second instanceof WildcardType
                && equals(
                    ((WildcardType) first).getUpperBounds(),
                    ((WildcardType) second).getUpperBounds(),
                    isomorphism)
                && equals(
                    ((WildcardType) first).getLowerBounds(),
                    ((WildcardType) second).getLowerBounds(),
                    isomorphism);

          } else {
            return first.equals(second);
          }
        });

    return equal;
  }

  private static boolean parameterizedTypeEquals(
      ParameterizedType a,
      ParameterizedType b,
      Isomorphism isomorphism) {
    return Objects.equals(a.getRawType(), b.getRawType())
        && equals(a.getOwnerType(), b.getOwnerType(), isomorphism)
        && equals(a.getActualTypeArguments(), b.getActualTypeArguments(), isomorphism);
  }

  /**
   * Determine if the given type, {@code from}, contains the given type,
   * {@code to}. In other words, if either of the given types are wildcards,
   * determine if every possible instantiation of {@code to} is also a valid
   * instantiation of {@code from}. Or if neither type is a wildcard, determine
   * whether both types are assignable to each other as per
   * {@link Types#isSubtype(Type, Type)}.
   * 
   * @param from
   *          the type within which we are determining containment
   * @param to
   *          the type of which we are determining containment
   * @return true if {@code from} <em>contains</em> {@code to}, false otherwise
   */
  public static boolean isContainedBy(Type from, Type to) {
    return isContainedBy(from, to, new Isomorphism());
  }

  private static class SubtypeRelation {
    private Type from, to;

    public SubtypeRelation(Type from, Type to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SubtypeRelation))
        return false;

      SubtypeRelation that = (SubtypeRelation) obj;
      return this.from == that.from && this.to == that.to;
    }

    @Override
    public int hashCode() {
      return System.identityHashCode(from) ^ (System.identityHashCode(to) * 7);
    }

    @Override
    public String toString() {
      return from + "@" + System.identityHashCode(from) + " <: " + to + "@"
          + System.identityHashCode(to);
    }
  }

  /**
   * Determine if a given type, {@code supertype}, is a subtype of another given
   * type, {@code subtype}. Or in other words, if {@code supertype} is a supertype
   * of {@code subtype}. Types are considered assignable if they involve unchecked
   * generic casts.
   * 
   * @param subtype
   *          the type from which we wish to determine assignability
   * @param supertype
   *          the type to which we wish to determine assignability
   * @return true if the types are assignable, false otherwise
   */
  public static boolean isSubtype(Type subtype, Type supertype) {
    return isSubtype(subtype, supertype, new Isomorphism());
  }

  private static boolean isSubtype(Type subtype, Type[] supertypes, Isomorphism isomorphism) {
    return Arrays.stream(supertypes).allMatch(t -> isSubtype(subtype, t, isomorphism));
  }

  private static boolean isSubtype(Type[] subtypes, Type supertype, Isomorphism isomorphism) {
    if (subtypes.length == 0) {
      return isSubtype(Object.class, supertype, isomorphism);
    } else {
      return Arrays.stream(subtypes).anyMatch(f -> isSubtype(f, supertype, isomorphism));
    }
  }

  private static boolean isSubtype(Type subtype, Type supertype, Isomorphism isomorphism) {
    return isomorphism
        .byEquality()
        .getPartialMapping(new SubtypeRelation(subtype, supertype), (a, partial) -> {
          partial.accept(() -> true);

          return isSubtypeImpl(a.from, a.to, isomorphism);
        });
  }

  private static Boolean isSubtypeImpl(Type subtype, Type supertype, Isomorphism isomorphism) {
    boolean assignable;

    if (subtype == null || supertype == null || supertype.equals(Object.class)
        || subtype == supertype) {
      /*
       * We can always assign to or from 'null', and we can always assign to Object.
       */
      assignable = true;
    } else if (supertype instanceof IntersectionType) {
      /*
       * We must be able to assign to each member of the intersection type.
       */
      Type[] types = ((IntersectionType) supertype).getTypes();

      assignable = Arrays.stream(types).allMatch(t -> isSubtype(subtype, t, isomorphism));
    } else if (subtype instanceof IntersectionType) {
      /*
       * We must be able to assign from at least one member of the intersection type.
       */
      Type[] types = ((IntersectionType) subtype).getTypes();

      assignable = isSubtype(types, supertype, isomorphism);
    } else if (subtype instanceof WildcardType) {
      /*
       * We must be able to assign from at least one of the upper bounds, including
       * the implied upper bound of Object, to the target type.
       */
      Type[] upperBounds = ((WildcardType) subtype).getUpperBounds();

      assignable = isSubtype(upperBounds, supertype, isomorphism);
    } else if (supertype instanceof WildcardType) {
      /*
       * If there are no lower bounds the target may be arbitrarily specific, so we
       * can never assign to it. Otherwise we must be able to assign to each lower
       * bound.
       */
      Type[] lowerBounds = ((WildcardType) supertype).getLowerBounds();

      if (lowerBounds.length == 0)
        assignable = false;
      else
        assignable = isSubtype(subtype, lowerBounds, isomorphism);
    } else if (subtype instanceof TypeVariableCapture) {
      /*
       * We must be able to assign from at least one of the upper bound, including the
       * implied upper bound of Object, to the target type.
       */
      Type[] upperBounds = ((TypeVariableCapture) subtype).getUpperBounds();

      assignable = isSubtype(upperBounds, supertype, isomorphism);

      if (!assignable && supertype instanceof TypeVariableCapture) {
        assignable = Arrays
            .asList(((TypeVariableCapture) supertype).getLowerBounds())
            .contains(subtype);
      }
    } else if (subtype instanceof TypeVariable) {
      /*
       * We must be able to assign from at least one of the upper bound, including the
       * implied upper bound of Object, to the target type.
       */
      Type[] upperBounds = ((TypeVariable<?>) subtype).getBounds();

      assignable = isSubtype(upperBounds, supertype, isomorphism);

      if (!assignable && supertype instanceof TypeVariableCapture) {
        assignable = Arrays
            .asList(((TypeVariableCapture) supertype).getLowerBounds())
            .contains(subtype);
      }
    } else if (supertype instanceof TypeVariableCapture) {
      /*
       * We assign to a type variable capture if we can assign to its lower bounds, or
       * if it is from the exact same type, or explicitly mentioned in an upper bound
       * or intersection type.
       */
      assignable = ((TypeVariableCapture) supertype).getLowerBounds().length > 0
          && isSubtype(subtype, ((TypeVariableCapture) supertype).getLowerBounds(), isomorphism);

    } else if (supertype instanceof TypeVariable) {
      /*
       * We can only assign to a type variable if it is from the exact same type, or
       * explicitly mentioned in an upper bound or intersection type.
       */
      assignable = false;

    } else if (subtype instanceof GenericArrayType) {
      GenericArrayType fromArray = (GenericArrayType) subtype;

      if (supertype instanceof Class<?>) {
        Class<?> toClass = (Class<?>) supertype;

        assignable = toClass.isArray() && isSubtype(
            fromArray.getGenericComponentType(),
            toClass.getComponentType(),
            isomorphism);
      } else if (supertype instanceof GenericArrayType) {
        GenericArrayType toArray = (GenericArrayType) supertype;

        assignable = isSubtype(
            fromArray.getGenericComponentType(),
            toArray.getGenericComponentType(),
            isomorphism);
      } else
        assignable = false;
    } else if (supertype instanceof GenericArrayType) {
      GenericArrayType toArray = (GenericArrayType) supertype;
      if (subtype instanceof Class<?>) {
        Class<?> fromClass = (Class<?>) subtype;
        assignable = fromClass.isArray() && isSubtype(
            fromClass.getComponentType(),
            toArray.getGenericComponentType(),
            isomorphism);
      } else
        assignable = false;
    } else if (supertype instanceof Class<?>) {
      assignable = ((Class<?>) supertype).isAssignableFrom(getErasedType(subtype));
    } else if (supertype instanceof ParameterizedType) {
      Class<?> matchedClass = getErasedType(supertype);

      if (!matchedClass.isAssignableFrom(getErasedType(subtype))) {
        assignable = false;
      } else {
        Type subtypeParameterization = new TypeHierarchy(subtype).resolveSupertype(matchedClass);

        if (!(subtypeParameterization instanceof ParameterizedType))
          assignable = false;
        else {
          Iterator<Type> toTypeArguments = ParameterizedTypes
              .getAllTypeArguments((ParameterizedType) supertype)
              .map(Map.Entry::getValue)
              .iterator();
          Iterator<Type> fromTypeArguments = ParameterizedTypes
              .getAllTypeArguments((ParameterizedType) subtypeParameterization)
              .map(Map.Entry::getValue)
              .iterator();

          assignable = true;
          while (toTypeArguments.hasNext()) {
            if (!isContainedBy(fromTypeArguments.next(), toTypeArguments.next(), isomorphism))
              assignable = false;
          }
        }
      }
    } else {
      assignable = false;
    }

    return assignable;
  }

  private static boolean isContainedBy(Type from, Type to, Isomorphism isomorphism) {
    boolean contained;

    if (to.equals(from)) {
      contained = true;
    } else if (to instanceof WildcardType) {
      WildcardType toWildcard = (WildcardType) to;

      contained = isSubtype(from, toWildcard.getUpperBounds(), isomorphism);

      contained = contained && (toWildcard.getLowerBounds().length == 0
          || isSubtype(toWildcard.getLowerBounds(), from, isomorphism));
    } else {
      contained = isSubtype(from, to, isomorphism) && isSubtype(to, from, isomorphism);
    }

    return contained;
  }

  /**
   * Determine if a given type, {@code to}, is assignable from another given type,
   * {@code from}. Or in other words, if {@code to} is a supertype of
   * {@code from}. Types are considered assignable if they involve unchecked
   * generic casts.
   * 
   * @param from
   *          the type from which we wish to determine assignability
   * @param to
   *          the type to which we wish to determine assignability
   * @return true if the types are assignable, false otherwise
   */
  public static boolean isAssignable(Type from, Type to) {
    return isLooseInvocationContextCompatible(from, to);
  }

  /**
   * <p>
   * Determine whether a given type, {@code from}, is compatible with a given
   * type, {@code to}, within a strict invocation context.
   * 
   * 
   * <p>
   * Types are considered so compatible if assignment is possible through
   * application of the following conversions:
   * 
   * <ul>
   * <li>an identity conversion (§5.1.1)</li>
   * <li>a widening primitive conversion (§5.1.2)</li>
   * <li>a widening reference conversion (§5.1.5)</li>
   * </ul>
   * 
   * @param from
   *          The type from which to determine compatibility.
   * @param to
   *          The type to which to determine compatibility.
   * @return True if the type {@code from} is compatible with the type {@code to},
   *         false otherwise.
   */
  public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
    if (isPrimitive(from)) {
      if (isPrimitive(to)) {
        if (to.equals(from) || to.equals(double.class)) {
          return true;
        } else if (to.equals(float.class)) {
          return !from.equals(double.class);
        } else if (to.equals(long.class)) {
          return !from.equals(double.class) && !from.equals(float.class);
        } else if (to.equals(int.class)) {
          return from.equals(byte.class) || from.equals(short.class) || from.equals(char.class);
        } else if (to.equals(short.class)) {
          return from.equals(byte.class);
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else if (isPrimitive(to)) {
      return false;
    }

    return isSubtype(from, to);
  }

  /**
   * <p>
   * Determine whether a given type, {@code from}, is compatible with a given
   * type, {@code to}, within a loose invocation context.
   * 
   * 
   * <p>
   * Types are considered so compatible if assignment is possible through
   * application of the following conversions:
   * 
   * <ul>
   * <li>an identity conversion</li>
   * <li>a widening primitive conversion</li>
   * <li>a widening reference conversion</li>
   * <li>a boxing conversion, optionally followed by widening reference
   * conversion</li>
   * <li>an unboxing conversion, optionally followed by a widening primitive
   * conversion</li>
   * </ul>
   * 
   * @param from
   *          The type from which to determine compatibility.
   * @param to
   *          The type to which to determine compatibility.
   * @return True if the type {@code from} is compatible with the type {@code to},
   *         false otherwise.
   */
  public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
    if (from instanceof IntersectionType) {
      return Arrays
          .stream(((IntersectionType) from).getTypes())
          .anyMatch(f -> isLooseInvocationContextCompatible(f, to));

    }

    if (to instanceof IntersectionType) {
      return Arrays
          .stream(((IntersectionType) to).getTypes())
          .allMatch(t -> isLooseInvocationContextCompatible(from, t));

    }

    if (from instanceof Class<?> && isGeneric((Class<?>) from)) {
      return isStrictInvocationContextCompatible(from, getErasedType(to));
    }

    if (isPrimitive(from) && !isPrimitive(to)) {
      return isStrictInvocationContextCompatible(wrapPrimitive(from), to);
    }

    if (!isPrimitive(from) && isPrimitive(to)) {
      return isStrictInvocationContextCompatible(unwrapPrimitive(from), to);
    }

    return isStrictInvocationContextCompatible(from, to);
  }

  /**
   * Search through all types mentioned by a given type, whether by identity, or
   * through bound relationships, type parameterizations, type intersections, or
   * generic array components, and collect all types meeting a given condition.
   * 
   * @param type
   *          The type to search for mentions which match the given condition.
   * @param condition
   *          The condition to classify matching types.
   * @return A set of all mentioned types matching the condition.
   */
  public static Stream<Type> getAllMentionedBy(Type type, Predicate<Type> condition) {
    Set<Type> types = new HashSet<>();

    Consumer<Type> conditionalAdd = t -> {
      if (condition.test(t))
        types.add(t);
    };

    RecursiveTypeVisitor
        .build()
        .visitEnclosingTypes()
        .visitParameters()
        .visitBounds()
        .classVisitor(conditionalAdd::accept)
        .genericArrayVisitor(conditionalAdd::accept)
        .intersectionTypeVisitor(conditionalAdd::accept)
        .inferenceVariableVisitor(conditionalAdd::accept)
        .parameterizedTypeVisitor(conditionalAdd::accept)
        .wildcardVisitor(conditionalAdd::accept)
        .typeVariableVisitor(conditionalAdd::accept)
        .create()
        .visit(type);

    return types.stream();
  }

  /**
   * Give a canonical String representation of a given type, which is intended to
   * be more easily human-readable than implementations of
   * {@link Object#toString()} for certain implementations of {@link Type}.
   * 
   * @param type
   *          The type for which we wish to determine a string representation.
   * @return A canonical string representation of the given type.
   */
  public static String toString(Type type) {
    return toString(type, Imports.empty());
  }

  /**
   * Give a canonical String representation of a given type, which is intended to
   * be more easily human-readable than implementations of
   * {@link Object#toString()} for certain implementations of {@link Type}.
   * Provided class and package imports allow the names of some classes to be
   * output without full package qualification.
   * 
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from output
   * @param type
   *          the type for which we wish to determine a string representation
   * @return A canonical string representation of the given type.
   */
  public static String toString(Type type, Imports imports) {
    return toString(type, imports, new Isomorphism());
  }

  /**
   * Give a canonical String representation of a given type, which is intended to
   * be more easily human-readable than implementations of
   * {@link Object#toString()} for certain implementations of {@link Type}.
   * Provided class and package imports allow the names of some classes to be
   * output without full package qualification.
   * 
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from output
   * @param type
   *          the type for which we wish to determine a string representation
   * @param isomorphism
   *          a type to string isomorphic mapping to deal with recursion
   * @return A canonical string representation of the given type.
   */
  public static String toString(Type type, Imports imports, Isomorphism isomorphism) {
    if (type == null) {
      return Objects.toString(null);
    } else if (type instanceof Class) {
      if (((Class<?>) type).isArray())
        return new StringBuilder(toString(((Class<?>) type).getComponentType(), imports))
            .append("[]")
            .toString();
      else
        return imports.getClassName((Class<?>) type);
    } else if (type instanceof ParameterizedType) {
      return ParameterizedTypes.toString((ParameterizedType) type, imports, isomorphism);
    } else if (type instanceof GenericArrayType) {
      return new StringBuilder(
          toString(((GenericArrayType) type).getGenericComponentType(), imports))
              .append("[]")
              .toString();
    } else if (type instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type;
      StringBuilder builder = new StringBuilder("?");

      appendBounds(
          builder,
          wildcardType.getUpperBounds(),
          wildcardType.getLowerBounds(),
          imports,
          isomorphism);

      return builder.toString();
    } else if (type instanceof TypeVariableCapture) {
      TypeVariableCapture typeVariableCapture = (TypeVariableCapture) type;
      StringBuilder builder = new StringBuilder(typeVariableCapture.getName());

      appendBounds(
          builder,
          typeVariableCapture.getUpperBounds(),
          typeVariableCapture.getLowerBounds(),
          imports,
          isomorphism);

      return builder.toString();
    } else if (type instanceof IntersectionType) {
      return IntersectionTypes.toString((IntersectionType) type, imports, isomorphism);
    } else
      return type.getTypeName();
  }

  static String toString(Type[] types, String delimiter, Imports imports, Isomorphism isomorphism) {
    return Arrays
        .stream(types)
        .map(t -> toString(t, imports, isomorphism))
        .collect(Collectors.joining(delimiter));
  }

  private static void appendBounds(
      StringBuilder builder,
      Type[] upperBounds,
      Type[] lowerBounds,
      Imports imports,
      Isomorphism isomorphism) {
    if (upperBounds.length > 0 && (upperBounds.length != 1
        || (upperBounds[0] != null && !upperBounds[0].equals(Object.class))))
      builder.append(" extends ").append(toString(upperBounds, " & ", imports, isomorphism));

    if (lowerBounds.length > 0 && !(lowerBounds.length == 1 && lowerBounds[0] == null))
      builder.append(" super ").append(toString(lowerBounds, " & ", imports, isomorphism));
  }

  /**
   * Create a Type instance from a parsed String. Here infinitely recurring types
   * are represented by, for example:
   * 
   * {@code java.util.List<java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable
   * <...>>>>}
   * 
   * Where "..." would be substituted, recursively, with the parameterization of
   * the an outer instance of the same raw class. TODO add clarity, and a proper
   * description of how ambiguity is resolved here.
   * 
   * @param typeString
   *          The String to parse.
   * @return The type described by the String.
   */
  public static Type fromString(String typeString) {
    return fromString(typeString, Imports.empty());
  }

  /**
   * Create a Type instance from a parsed String. Here infinitely recurring types
   * are represented by, for example:
   * 
   * {@code java.util.List<java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable<? extends java.lang.Number & java.lang.Comparable
   * <...>>>>}
   * 
   * Where "..." would be substituted, recursively, with the parameterization of
   * the an outer instance of the same raw class. TODO add clarity, and a proper
   * description of how ambiguity is resolved here.
   * 
   * @param typeString
   *          The String to parse.
   * @param imports
   *          Classes and packages for which full package qualification may be
   *          omitted from input.
   * @return The type described by the String.
   */
  private static Type fromString(String typeString, Imports imports) {
    return new TypeParser(imports).classType().parse(typeString);
  }

  /**
   * Get the default type parser. All type names will need to be fully qualified
   * to correctly parse.
   * 
   * @return The default annotated type parser
   */
  public static TypeParser getParser() {
    return TYPE_PARSER;
  }

  /**
   * Get a type parser with knowledge of the given imports. Type names may omit
   * full qualification if those types are imported according to the given
   * imports.
   * 
   * @param imports
   *          A list of imports the type parser should be aware of
   * @return A type parser with knowledge of the given imports
   */
  public static TypeParser getParser(Imports imports) {
    return new TypeParser(imports);
  }

  /**
   * A parser for {@link Type}s, and various related types.
   * 
   * @author Elias N Vasylenko
   */
  public static class TypeParser {
    private final Parser<Class<?>> rawType;

    private final Parser<Type> classOrArrayType;
    private final Parser<WildcardType> wildcardType;
    private final Parser<Type> typeParameter;

    private TypeParser(Imports imports) {
      rawType = Parser
          .matching("[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*")
          .transform(imports::getNamedClass);

      classOrArrayType = rawType
          .transform(Type.class::cast)
          .tryAppendTransform(
              Parser
                  .list(Parser.proxy(this::type), "\\s*,\\s*")
                  .prepend("\\s*<\\s*")
                  .append("\\s*>\\s*"),
              (t, p) -> ParameterizedTypes.parameterize((Class<?>) t, p))
          .appendTransform(
              Parser.list(Parser.matching("\\s*\\[\\s*\\]"), "\\s*").prepend("\\s*"),
              (t, l) -> {
                t = arrayFromComponent(t, l.size());
                return t;
              });

      wildcardType = Parser
          .matching("\\s*\\?\\s*extends(?![_a-zA-Z0-9])\\s*")
          .appendTransform(
              Parser.list(classOrArrayType, "\\s*\\&\\s*"),
              (s, t) -> WildcardTypes.wildcardExtending(t))
          .orElse(
              Parser
                  .matching("\\s*\\?\\s*super(?![_a-zA-Z0-9])\\s*")
                  .appendTransform(
                      Parser.list(classOrArrayType, "\\s*\\&\\s*"),
                      (s, t) -> WildcardTypes.wildcardSuper(t)))
          .orElse(Parser.matching("\\s*\\?").transform(s -> WildcardTypes.wildcard()));

      typeParameter = classOrArrayType.orElse(wildcardType.transform(Type.class::cast));
    }

    /**
     * A parser for raw class types.
     * 
     * @return The raw type of the parsed type name
     */
    public Parser<Class<?>> rawType() {
      return rawType;
    }

    /**
     * A parser for a class type, which may be parameterized.
     * 
     * @return The type of the expressed name, and the given parameterization where
     *         appropriate
     */
    public Parser<Type> classType() {
      return classOrArrayType;
    }

    /**
     * A parser for a wildcard type.
     * 
     * @return The type of the expressed wildcard type
     */
    public Parser<WildcardType> wildcardType() {
      return wildcardType;
    }

    /**
     * A parser for a class type or wildcard type.
     * 
     * @return The annotated type of the expressed type
     */
    public Parser<Type> type() {
      return typeParameter;
    }
  }
}
