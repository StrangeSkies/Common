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
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOf;
import static uk.co.strangeskies.reflection.IntersectionTypes.uncheckedIntersectionOf;
import static uk.co.strangeskies.reflection.WildcardTypes.hasUpperBound;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.utility.Isomorphism;

public class TypeBounds {
  private final Isomorphism isomorphism;

  public TypeBounds() {
    isomorphism = new Isomorphism();
  }

  /**
   * See {@link TypeBounds#leastUpperBound(Collection)}.
   * 
   * @param upperBounds
   *          Forwards to {@code upperBounds} parameter.
   * @return As referenced method.
   */
  public Type leastUpperBound(Type... upperBounds) {
    return leastUpperBound(Arrays.asList(upperBounds));
  }

  /**
   * Derive the least upper bound for a set of types, as defined in the Java
   * language specification.
   * 
   * @param upperBounds
   *          A collection of types representing the upper bounds of an unknown
   *          type.
   * @return The least specific single type which, as an upper bound, will also
   *         satisfy each upper bound in the given set.
   */
  public Type leastUpperBound(Collection<Type> upperBounds) {
    Type upperBound = leastUpperBoundImpl(upperBounds);

    /*
     * Not sure if this is necessary! But it's cheap enough to check. Can't validate
     * IntersectionTypes and ParameterizedTypes as we create them, as they may
     * contain uninitialised proxies in place of ParameterizedTypes.
     */
    // validate(upperBound);

    return upperBound;
  }

  Type leastUpperBoundImpl(Collection<Type> upperBounds) {
    if (upperBounds.size() == 1)
      /*
       * If k = 1, then the lub is the type itself: lub(U) = U.
       */
      return upperBounds.iterator().next();
    else {
      /*
       * For each Ui (1 ≤ i ≤ k):
       */
      Iterator<Type> lowerBoundsIterator = upperBounds.iterator();
      MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates = new MultiHashMap<>(
          HashSet::new);
      erasedCandidates.addAll(getErasedSupertypes(lowerBoundsIterator.next()));

      while (lowerBoundsIterator.hasNext()) {
        Type t = lowerBoundsIterator.next();
        Map<Class<?>, ParameterizedType> erasedSupertypes = getErasedSupertypes(t);
        erasedCandidates.keySet().retainAll(erasedSupertypes.keySet());
        for (Map.Entry<Class<?>, ParameterizedType> erasedSupertype : erasedSupertypes.entrySet())
          if (erasedCandidates.containsKey(erasedSupertype.getKey())
              && erasedSupertype.getValue() != null)
            erasedCandidates.add(erasedSupertype.getKey(), erasedSupertype.getValue());
      }

      minimiseCandidates(erasedCandidates);

      List<Type> bestTypes = erasedCandidates
          .entrySet()
          .stream()
          .map(e -> best(e.getKey(), new ArrayList<>(e.getValue())))
          .collect(Collectors.toList());

      return uncheckedIntersectionOf(bestTypes);
    }
  }

  void minimiseCandidates(
      MultiMap<Class<?>, ParameterizedType, ? extends Set<ParameterizedType>> erasedCandidates) {
    List<Class<?>> minimalCandidates = new ArrayList<>(erasedCandidates.keySet());
    if (minimalCandidates.size() > 1)
      for (int i = 0; i < minimalCandidates.size(); i++)
        for (int j = i + 1; j < minimalCandidates.size(); j++) {
          if (minimalCandidates.get(i).isAssignableFrom(minimalCandidates.get(j))) {
            minimalCandidates.remove(i);
            j = i;
          } else if (minimalCandidates.get(j).isAssignableFrom(minimalCandidates.get(i))) {
            minimalCandidates.remove(j--);
          }
        }
    erasedCandidates.keySet().retainAll(minimalCandidates);
  }

  /**
   * Given a number of candidate parameterizations of a given class, derive the
   * most specific possible parameterization which is a supertype of all
   * candidates according to the Java 8 language specification regarding type
   * inference.
   * 
   * @param rawClass
   *          the class to be parameterized
   * @param parameterizations
   *          the candidate parameterizations
   * @return the parameterized type which minimally contains all the given types
   */
  public Type best(Class<?> rawClass, List<ParameterizedType> parameterizations) {
    if (parameterizations.isEmpty())
      return rawClass;
    else if (parameterizations.size() == 1) {
      Type parameterization = parameterizations.iterator().next();
      return parameterization == null ? rawClass : parameterization;
    }

    List<TypeVariable<?>> typeParameters = ParameterizedTypes
        .getAllTypeParameters(rawClass)
        .collect(toList());
    /*
     * Proxy guard against recursive generation of infinite types
     */
    return isomorphism
        .byEquality()
        .getProxiedMapping(
            new LinkedHashSet<>(parameterizations),
            ParameterizedType.class,
            p -> bestImpl(rawClass, typeParameters, new ArrayList<>(p)));
  }

  ParameterizedType bestImpl(
      Class<?> rawClass,
      List<TypeVariable<?>> typeParameters,
      List<ParameterizedType> parameterizations) {
    Map<TypeVariable<?>, Type> leastContainingParameterization = new HashMap<>();

    for (int j = 0; j < typeParameters.size(); j++) {
      TypeVariable<?> variable = typeParameters.get(j);
      for (int i = 0; i < parameterizations.size(); i++) {
        ParameterizedType parameterization = parameterizations.get(i);
        if (parameterization != null) {
          Type argumentU = parameterization.getActualTypeArguments()[j];
          Type argumentV = leastContainingParameterization.get(variable);

          if (argumentV == null)
            leastContainingParameterization.put(variable, argumentU);
          else {
            leastContainingParameterization
                .put(variable, leastContainingArgument(argumentU, argumentV));
          }
        }
      }
    }

    ParameterizedType best = ParameterizedTypes
        .parameterizeUnchecked(rawClass, leastContainingParameterization::get);

    return best;
  }

  /**
   * Fetch the least containing argument of type type arguments according to the
   * Java 8 language specification.
   * 
   * @param argumentU
   *          the first argument
   * @param argumentV
   *          the second argument
   * @return the type argument which minimally contains both the given type
   *         arguments
   */
  public Type leastContainingArgument(Type argumentU, Type argumentV) {
    if (argumentU instanceof WildcardType
        && (!(argumentV instanceof WildcardType) || hasUpperBound((WildcardType) argumentV))) {
      Type swap = argumentU;
      argumentU = argumentV;
      argumentV = swap;
    }

    if (argumentU instanceof WildcardType) {
      WildcardType wildcardU = (WildcardType) argumentU;
      WildcardType wildcardV = (WildcardType) argumentV;

      if (hasUpperBound(wildcardU)) {
        if (hasUpperBound(wildcardV)) {
          /*
           * lcta(? extends U, ? extends V) = ? extends lub(U, V)
           */
          List<Type> aggregation = Arrays
              .asList(
                  intersectionOf(wildcardU.getUpperBounds()),
                  intersectionOf(wildcardV.getUpperBounds()));
          return WildcardTypes.wildcardExtending(leastUpperBoundImpl(aggregation));
        } else {
          /*
           * lcta(? extends U, ? super V) = U if U = V, otherwise ?
           */
          return intersectionOf(wildcardU.getUpperBounds())
              .equals(intersectionOf(wildcardV.getLowerBounds()))
                  ? intersectionOf(wildcardU.getUpperBounds())
                  : WildcardTypes.wildcard();
        }
      } else {
        /*
         * lcta(? super U, ? super V) = ? super glb(U, V)
         */
        return WildcardTypes
            .wildcardSuper(
                greatestLowerBound(
                    uncheckedIntersectionOf(((WildcardType) argumentU).getLowerBounds()),
                    uncheckedIntersectionOf(((WildcardType) argumentV).getLowerBounds())));
      }
    } else if (argumentV instanceof WildcardType) {
      if (hasUpperBound((WildcardType) argumentV)) {
        /*
         * lcta(U, ? extends V) = ? extends lub(U, V)
         */
        List<Type> bounds = new ArrayList<>(
            Arrays.asList(((WildcardType) argumentV).getUpperBounds()));
        bounds.add(argumentU);
        return WildcardTypes.wildcardExtending(leastUpperBoundImpl(bounds));
      } else {
        /*
         * lcta(U, ? super V) = ? super glb(U, V)
         */
        return WildcardTypes
            .wildcardSuper(
                greatestLowerBound(
                    argumentU,
                    uncheckedIntersectionOf(((WildcardType) argumentV).getLowerBounds())));
      }
    } else {
      /*
       * lcta(U, V) = U if U = V, otherwise ? extends lub(U, V)
       */
      return argumentU.equals(argumentV)
          ? argumentU
          : WildcardTypes
              .wildcardExtending(leastUpperBoundImpl(Arrays.asList(argumentU, argumentV)));
    }
  }

  Map<Class<?>, ParameterizedType> getErasedSupertypes(Type of) {
    Map<Class<?>, ParameterizedType> supertypes = new HashMap<>();

    new TypeHierarchy(of).resolveCompleteSupertypeHierarchy(Object.class).forEach(type -> {
      if (type instanceof Class<?>) {
        supertypes.put((Class<?>) type, null);
      } else if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        supertypes.put((Class<?>) parameterizedType.getRawType(), parameterizedType);
      }
    });

    return supertypes;
  }

  /**
   * See {@link TypeBounds#greatestLowerBound(Collection)}.
   * 
   * @param lowerBounds
   *          Forwards to {@code lowerBounds} parameter.
   * @return As referenced method.
   */
  public Type greatestLowerBound(Type... lowerBounds) {
    return greatestLowerBound(Arrays.asList(lowerBounds));
  }

  /**
   * Derive the greatest lower bound for a set of types, as defined in the Java
   * language specification.
   * 
   * @param lowerBounds
   *          A collection of types representing the lower bounds of an unknown
   *          type.
   * @return The most specific single type which, as a lower bound, will also
   *         satisfy each lower bound in the given set.
   */
  public Type greatestLowerBound(Collection<? extends Type> lowerBounds) {
    return intersectionOf(lowerBounds);
  }
}
