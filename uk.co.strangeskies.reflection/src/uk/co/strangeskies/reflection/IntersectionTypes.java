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
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A collection of utility methods relating to intersection types.
 * 
 * @author Elias N Vasylenko
 */
@ProviderType
public class IntersectionTypes {
  private static class IntersectionTypeProxy implements IntersectionType {
    private final Supplier<IntersectionType> source;

    public IntersectionTypeProxy(Supplier<IntersectionType> source) {
      this.source = source;
    }

    @Override
    public Type[] getTypes() {
      return source.get().getTypes();
    }

    @Override
    public String toString() {
      return source.get().toString();
    }

    @Override
    public boolean equals(Object obj) {
      return source.get().equals(obj);
    }

    @Override
    public int hashCode() {
      return source.get().hashCode();
    }
  }

  private IntersectionTypes() {}

  /**
   * @param source
   *          A supplier of the intersection type we wish to proxy.
   * @return A proxy for an intersection type, forwarding to the instance provided
   *         by the given supplier at the moment of each invocation. This is
   *         generally useful for algorithms which deal with infinite types.
   */
  public static IntersectionType proxy(Supplier<IntersectionType> source) {
    return new IntersectionTypeProxy(source);
  }

  /**
   * @param types
   *          The set of types from which to derive a new intersection type.
   * @return An intersection type containing each of the given types, or a single
   *         type, if they can all be represented as such.
   */
  public static Type intersectionOf(Type... types) {
    return intersectionOf(Arrays.asList(types));
  }

  /**
   * @param types
   *          The set of types from which to derive a new intersection type.
   * @return An intersection type containing each of the given types, or a single
   *         type, if they can all be represented as such.
   */
  public static Type intersectionOf(Collection<? extends Type> types) {
    return intersectionOf(types, emptyBoundSet());
  }

  /*
   * Ensure no member is itself an intersection type.
   */
  private static List<Type> flatten(Collection<? extends Type> types) {
    List<Type> flattenedTypes = new ArrayList<>(types);

    for (int i = 0; i < flattenedTypes.size();) {
      Type type = flattenedTypes.get(i);

      if (Object.class.equals(type)) {
        flattenedTypes.remove(i);
      } else if (type instanceof IntersectionType) {
        flattenedTypes.remove(i);
        flattenedTypes.addAll(Arrays.asList(((IntersectionType) type).getTypes()));
      } else {
        i++;
      }
    }

    return flattenedTypes;
  }

  /*
   * remove redundancies and order so that the remaining class is at the front, if
   * one exists.
   */
  private static void orderAndMinimise(final List<Type> flattenedTypes) {
    Map<Type, Boolean> properType = new IdentityHashMap<>();
    for (Type type : flattenedTypes) {
      properType.put(type, InferenceVariable.isProperType(type));
    }

    /*
     * Loop through each pair of types, removing those which are redundant due to
     * subtype relation.
     * 
     * At same time, search for the most specific class type.
     */
    int mostSpecificIndex = -1;
    for (int i = 0; i < flattenedTypes.size(); i++) {
      Type iType = flattenedTypes.get(i);
      if (properType.get(iType)) {
        /*
         * For each proper type i
         */

        boolean iInterface = Types.getErasedType(iType).isInterface();

        if (mostSpecificIndex == -1 && !iInterface) {
          mostSpecificIndex = i;
        }

        for (int j = i + 1; j < flattenedTypes.size(); j++) {
          Type jType = flattenedTypes.get(j);
          if (properType.get(jType)) {
            /*
             * For each proper type j, coming after i
             */

            if (Types.isAssignable(iType, jType)) {
              flattenedTypes.remove(j--);

              if (mostSpecificIndex > j) {
                mostSpecificIndex--;
              }
            } else if (Types.isAssignable(jType, iType)) {
              if (i == mostSpecificIndex) {
                mostSpecificIndex = j;
              }

              flattenedTypes.remove(i--);

              if (mostSpecificIndex >= i) {
                mostSpecificIndex--;
              }
              break;
            } else if (!iInterface && !Types.getErasedType(jType).isInterface()) {
              throw new ReflectionException(
                  REFLECTION_PROPERTIES.invalidIntersectionTypes(flattenedTypes, iType, jType));
            }
          }
        }
      }
    }

    if (mostSpecificIndex >= 0) {
      Type mostSpecificType = flattenedTypes.remove(mostSpecificIndex);
      flattenedTypes.add(0, mostSpecificType);
    }
  }

  /**
   * Create an intersection type from the given types, with leniency towards
   * validation of intersections between types which may contain inference
   * variables according to the given bound set.
   * 
   * @param types
   *          The set of types from which to derive a new intersection type.
   * @param bounds
   *          The bound set which provides context for any inference variables
   *          which may be mentioned by the given types.
   * @return An intersection type containing each of the given types, or a single
   *         type, if they can all be represented as such.
   */
  public static Type intersectionOf(Collection<? extends Type> types, BoundSet bounds) {
    List<Type> flattenedTypes = flatten(types);

    if (flattenedTypes.isEmpty())
      return Object.class;

    if (flattenedTypes.size() == 1)
      return flattenedTypes.iterator().next();

    orderAndMinimise(flattenedTypes);

    if (flattenedTypes.isEmpty())
      return Object.class;

    if (flattenedTypes.size() == 1)
      return flattenedTypes.iterator().next();

    try {
      /*
       * TODO this needs replacing with a more manual and direct implementation of the
       * intersection type validity algorithm
       */
      InferenceVariable inferenceVariable = new InferenceVariable();
      bounds = bounds.withInferenceVariables(inferenceVariable);

      for (Type type : flattenedTypes) {
        bounds = new ConstraintFormula(Kind.SUBTYPE, inferenceVariable, type).reduce(bounds);
      }
    } catch (Exception e) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.invalidIntersectionType(flattenedTypes),
          e);
    }

    return intersectionOfImpl(flattenedTypes);
  }

  static Type uncheckedIntersectionOf(Type... types) {
    return uncheckedIntersectionOf(Arrays.asList(types));
  }

  static Type uncheckedIntersectionOf(Collection<? extends Type> types) {
    if (types.isEmpty()) {
      return Object.class;

    } else if (types.size() == 1) {
      return types.iterator().next();

    } else {
      return intersectionOfImpl(types);
    }
  }

  static IntersectionType intersectionOfImpl(Collection<? extends Type> types) {
    if (types.size() <= 1)
      throw new IllegalArgumentException();

    return new IntersectionType() {
      final Type[] typeArray = types.toArray(new Type[types.size()]);

      @Override
      public Type[] getTypes() {
        return typeArray;
      }

      @Override
      public String toString() {
        return IntersectionTypes.toString(this, Imports.empty());
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == this)
          return true;

        if (!(obj instanceof Type))
          return false;

        Type that = (Type) obj;
        return Types.equals(this, that);
      }

      @Override
      public int hashCode() {
        return Arrays.hashCode(getTypes());
      }
    };
  }

  /**
   * Give a canonical String representation of an intersection type which supports
   * infinite types. Provided class and package imports allow the names of some
   * classes to be output without full package qualification.
   * 
   * @param type
   *          the type to print
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from output
   * @return a canonical string representation of the given type
   */
  public static String toString(IntersectionType type, Imports imports) {
    return toString(type, imports, new Isomorphism());
  }

  /**
   * Give a canonical String representation of an intersection type which supports
   * infinite types. Provided class and package imports allow the names of some
   * classes to be output without full package qualification.
   * 
   * @param type
   *          the type to print
   * @param imports
   *          classes and packages for which full package qualification may be
   *          omitted from output
   * @param isomorphism
   *          a type to string isomorphic mapping to deal with recursion
   * @return a canonical string representation of the given type
   */
  public static String toString(IntersectionType type, Imports imports, Isomorphism isomorphism) {
    return isomorphism
        .byIdentity()
        .getPartialMapping(
            type,

            () -> "...",

            i -> stream(i.getTypes()).map(t -> {
              String typeName = Types.toString(t, imports, isomorphism);
              if (t instanceof TypeVariableCapture)
                typeName = new StringBuilder()
                    .append("[ ")
                    .append(typeName)
                    .append(" ]")
                    .toString();
              return typeName;
            }).collect(Collectors.joining(" & ")));
  }

  public static Type[] getTypes(Type type) {
    if (type instanceof IntersectionType) {
      return ((IntersectionType) type).getTypes();
    } else {
      return new Type[] { type };
    }
  }
}
