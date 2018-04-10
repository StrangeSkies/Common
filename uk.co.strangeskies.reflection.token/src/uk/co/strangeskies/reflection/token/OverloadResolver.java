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
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static uk.co.strangeskies.collection.stream.StreamUtilities.tryOptional;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;

import uk.co.strangeskies.collection.tuple.Pair;
import uk.co.strangeskies.reflection.ConstraintFormula;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.TypeResolver;
import uk.co.strangeskies.reflection.Types;

public class OverloadResolver<I extends ExecutableToken<?, ?>> {
  private final Set<I> candidates;

  public OverloadResolver(Collection<? extends I> candidates) {
    this.candidates = new HashSet<>(candidates);
  }

  public I resolve() {
    return resolve(emptyList());
  }

  public I resolve(Type... arguments) {
    return resolve(stream(arguments).map(TypeToken::forType).collect(toList()));
  }

  public I resolve(TypeToken<?>... arguments) {
    return resolve(asList(arguments));
  }

  public I resolve(Collection<? extends TypeToken<?>> arguments) {
    if (candidates.isEmpty())
      throw new IllegalArgumentException(
          "Cannot find any applicable invocable for arguments '" + arguments + "'");

    Set<? extends I> applicableCandidates = resolveApplicableExecutableMembers(
        candidates,
        arguments);

    return resolveMostSpecificExecutableMember(applicableCandidates);
  }

  public static <I extends ExecutableToken<?, ?>> Collector<I, ?, I> resolveOverload() {
    return resolveOverload(emptyList());
  }

  public static <I extends ExecutableToken<?, ?>> Collector<I, ?, I> resolveOverload(
      Type... arguments) {
    return resolveOverload(stream(arguments).map(TypeToken::forType).collect(toList()));
  }

  public static <I extends ExecutableToken<?, ?>> Collector<I, ?, I> resolveOverload(
      TypeToken<?>... arguments) {
    return resolveOverload(asList(arguments));
  }

  public static <I extends ExecutableToken<?, ?>> Collector<I, ?, I> resolveOverload(
      Collection<? extends TypeToken<?>> arguments) {
    return collectingAndThen(
        toSet(),
        candidates -> new OverloadResolver<>(candidates).resolve(arguments));
  }

  /**
   * Find the set of all given overload candidates which are applicable to
   * invocation with the given parameters. Strict applicability is considered
   * first, then if no candidates are found loose applicability is considered,
   * then if still no candidates are found, variable arity applicability is
   * considered.
   * 
   * @param <I>
   *          the type of invokable
   * @param candidates
   *          The candidates for which we wish to determine applicability.
   * @param parameters
   *          The parameters representing the invocation for which we wish to
   *          determine applicability.
   * @return The set of all given overload candidates which are most applicable to
   *         invocation with the given parameters.
   */
  @SuppressWarnings("unchecked")
  public static <I extends ExecutableToken<?, ?>> Set<? extends I> resolveApplicableExecutableMembers(
      Set<? extends I> candidates,
      Collection<? extends TypeToken<?>> parameters) {
    Map<I, RuntimeException> failures = new LinkedHashMap<>();
    BiConsumer<I, RuntimeException> putFailures = failures::put;

    Set<? extends I> compatibleCandidates = filterOverloadCandidates(
        candidates,
        i -> (I) i.withLooseApplicability(parameters),
        putFailures);

    if (compatibleCandidates.isEmpty()) {
      compatibleCandidates = new HashSet<>(candidates);
      for (I candidate : candidates)
        if (!candidate.isVariableArityDefinition())
          compatibleCandidates.remove(candidate);

      compatibleCandidates = filterOverloadCandidates(
          compatibleCandidates,
          i -> (I) i.withVariableArityApplicability(parameters),
          putFailures);

    } else {
      Set<? extends I> oldCompatibleCandidates = compatibleCandidates;
      compatibleCandidates = filterOverloadCandidates(
          candidates,
          i -> (I) i.withStrictApplicability(parameters),
          putFailures);
      if (compatibleCandidates.isEmpty())
        compatibleCandidates = oldCompatibleCandidates;
    }

    if (compatibleCandidates.isEmpty()) {
      Set<? extends Executable> candidateMembers = candidates
          .stream()
          .map(ExecutableToken::getMember)
          .collect(toSet());
      List<? extends Type> candidateParameters = parameters
          .stream()
          .map(TypeToken::getType)
          .collect(toList());

      Iterator<RuntimeException> exceptionIterator = failures.values().iterator();
      RuntimeException mainCause = exceptionIterator.next();
      while (exceptionIterator.hasNext()) {
        mainCause.addSuppressed(exceptionIterator.next());
      }

      throw new ReflectionException(
          REFLECTION_PROPERTIES.cannotResolveApplicable(candidateMembers, candidateParameters),
          mainCause);
    }

    return compatibleCandidates;
  }

  private static <I extends ExecutableToken<?, ?>> Set<? extends I> filterOverloadCandidates(
      Collection<? extends I> candidates,
      Function<? super I, I> applicabilityFunction,
      BiConsumer<I, RuntimeException> failures) {
    return candidates
        .stream()
        .flatMap(
            i -> tryOptional(() -> applicabilityFunction.apply(i), e -> failures.accept(i, e))
                .stream())
        .collect(toSet());
  }

  /**
   * Find which of the given overload candidates is the most specific according to
   * the rules described by the Java 8 language specification.
   * 
   * <p>
   * If no single most specific candidate can be found, the method will throw a
   * {@link ReflectionException}.
   * 
   * @param <I>
   *          the type of invokable
   * @param candidates
   *          The candidates from which to select the most specific.
   * @return The most specific of the given candidates.
   */
  public static <I extends ExecutableToken<?, ?>> I resolveMostSpecificExecutableMember(
      Collection<? extends I> candidates) {
    if (candidates.size() == 1)
      return candidates.iterator().next();

    Set<I> mostSpecificSoFar = resolveMostSpecificCandidateSet(candidates);

    /*
     * Find which of the remaining candidates, which should all have identical
     * parameter types, is declared in the lowermost class.
     */
    Iterator<I> overrideCandidateIterator = mostSpecificSoFar.iterator();
    I mostSpecific = overrideCandidateIterator.next();
    while (overrideCandidateIterator.hasNext()) {
      I candidate = overrideCandidateIterator.next();

      if (!candidate.getParameters().equals(mostSpecific.getParameters())
          || !candidate.getName().equals(mostSpecific.getName())) {
        I mostSpecificFinal = mostSpecific;
        throw new ReflectionException(
            REFLECTION_PROPERTIES
                .cannotResolveAmbiguity(candidate.getMember(), mostSpecificFinal.getMember()));
      }

      mostSpecific = candidate
          .getMember()
          .getDeclaringClass()
          .isAssignableFrom(mostSpecific.getMember().getDeclaringClass())
              ? candidate
              : mostSpecific;
    }

    return mostSpecific;
  }

  private static <I extends ExecutableToken<?, ?>> Set<I> resolveMostSpecificCandidateSet(
      Collection<? extends I> candidates) {
    List<I> remainingCandidates = new ArrayList<>(candidates);

    /*
     * For each remaining candidate in the list...
     */
    for (int first = 0; first < remainingCandidates.size(); first++) {
      I firstCandidate = remainingCandidates.get(first);

      /*
       * Compare with each other remaining candidate...
       */
      for (int second = first + 1; second < remainingCandidates.size(); second++) {
        I secondCandidate = remainingCandidates.get(second);

        /*
         * Determine which of the executable members, if either, are more specific.
         */
        Pair<Boolean, Boolean> moreSpecific = compareCandidates(firstCandidate, secondCandidate);

        if (moreSpecific.getLeft()) {
          if (moreSpecific.getRight()) {
            /*
             * First and second are equally specific.
             */
          } else {
            /*
             * First is strictly more specific.
             */
            remainingCandidates.remove(second--);
          }
        } else if (moreSpecific.getRight()) {
          /*
           * Second is strictly more specific.
           */
          remainingCandidates.remove(first--);

          break;
        } else {
          /*
           * Neither first nor second are more specific.
           */
          throw new ReflectionException(
              REFLECTION_PROPERTIES
                  .cannotResolveAmbiguity(firstCandidate.getMember(), secondCandidate.getMember()));
        }
      }
    }

    return new HashSet<>(remainingCandidates);
  }

  private static Pair<Boolean, Boolean> compareCandidates(
      ExecutableToken<?, ?> firstCandidate,
      ExecutableToken<?, ?> secondCandidate) {
    boolean firstMoreSpecific = true;
    boolean secondMoreSpecific = true;

    if (firstCandidate.isGeneric())
      secondMoreSpecific = compareGenericCandidate(secondCandidate, firstCandidate);
    if (secondCandidate.isGeneric())
      firstMoreSpecific = compareGenericCandidate(firstCandidate, secondCandidate);

    if (!firstCandidate.isGeneric() || !secondCandidate.isGeneric()) {
      Iterator<ExecutableParameter> firstParameterIteror = firstCandidate
          .getParameters()
          .iterator();
      Iterator<ExecutableParameter> secondParameterIteror = secondCandidate
          .getParameters()
          .iterator();
      while (firstParameterIteror.hasNext()) {
        ExecutableParameter firstParameter = firstParameterIteror.next();
        ExecutableParameter secondParameter = secondParameterIteror.next();

        if (!secondMoreSpecific && !secondCandidate.isGeneric()) {
          if (!Types.isAssignable(firstParameter.getType(), secondParameter.getType())) {
            firstMoreSpecific = false;
            break;
          }
        } else if (!firstMoreSpecific && !firstCandidate.isGeneric()) {
          if (!Types.isAssignable(secondParameter.getType(), firstParameter.getType())) {
            secondMoreSpecific = false;
            break;
          }
        } else {
          secondMoreSpecific = Types
              .isAssignable(secondParameter.getType(), firstParameter.getType());
          firstMoreSpecific = Types
              .isAssignable(firstParameter.getType(), secondParameter.getType());

          if (!(firstMoreSpecific || secondMoreSpecific))
            break;
        }
      }
    }

    return new Pair<>(firstMoreSpecific, secondMoreSpecific);
  }

  private static boolean compareGenericCandidate(
      ExecutableToken<?, ?> firstCandidate,
      ExecutableToken<?, ?> genericCandidate) {
    TypeResolver resolver = new TypeResolver(genericCandidate.getBounds());

    try {
      List<ExecutableParameter> firstParameters = firstCandidate.getParameters().collect(toList());
      List<ExecutableParameter> genericParameters = genericCandidate
          .getParameters()
          .collect(toList());
      int parameters = firstParameters.size();
      if (firstCandidate.isVariableArityDefinition()) {
        parameters--;

        resolver
            .reduceConstraint(
                new ConstraintFormula(
                    Kind.SUBTYPE,
                    firstParameters.get(parameters).getType(),
                    genericParameters.get(parameters).getType()));
      }

      for (int i = 0; i < parameters; i++) {
        resolver
            .reduceConstraint(
                new ConstraintFormula(
                    Kind.SUBTYPE,
                    firstParameters.get(i).getType(),
                    genericParameters.get(i).getType()));
      }

      resolver.resolve();
    } catch (Exception e) {
      return false;
    }

    return true;
  }
}
