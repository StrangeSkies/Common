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
import static java.util.Collections.emptyMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.collection.stream.StreamUtilities.entriesToMap;
import static uk.co.strangeskies.collection.stream.StreamUtilities.throwingReduce;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.BoundSet.emptyBoundSet;
import static uk.co.strangeskies.reflection.IntersectionTypes.uncheckedIntersectionOf;
import static uk.co.strangeskies.reflection.ParameterizedTypes.getAllTypeArguments;
import static uk.co.strangeskies.reflection.ReflectionException.REFLECTION_PROPERTIES;
import static uk.co.strangeskies.reflection.Types.getAllMentionedBy;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.reflection.ConstraintFormula.Kind;
import uk.co.strangeskies.utility.DeepCopyable;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * <p>
 * A {@link TypeResolver} represents a view over an underlying {@link BoundSet},
 * and provides a number of important functionalities for interacting with that
 * {@link BoundSet}. Multiple {@link TypeResolver}s can provide different views
 * of the same {@link BoundSet} instance.
 * 
 * <p>
 * Whenever any {@link InferenceVariable} is created by way of a
 * {@link TypeResolver} instance, that {@link InferenceVariable} will be
 * associated with a particular {@link GenericDeclaration}. Within this context,
 * which is so described by a {@link GenericDeclaration}, at most only one
 * {@link InferenceVariable} may by created for any given {@link TypeVariable}.
 * A {@link TypeResolver} always creates {@link InferenceVariable} according to
 * the behavior of {@link TypeResolver#inferTypeParameters(GenericDeclaration)}.
 * 
 * @author Elias N Vasylenko
 */
public class TypeResolver implements DeepCopyable<TypeResolver> {
  private BoundSet bounds;

  /**
   * Create a new {@link TypeResolver} over the given {@link BoundSet}.
   * 
   * @param bounds
   *          The exact bound set we wish to create a resolver over. Operations on
   *          the new resolver may mutate the given bound set.
   */
  public TypeResolver(BoundSet bounds) {
    this.bounds = bounds;
  }

  /**
   * Create a new resolver over a new bound set.
   */
  public TypeResolver() {
    this(emptyBoundSet());
  }

  @Override
  public TypeResolver copy() {
    TypeResolver copy = new TypeResolver(bounds.copy());

    return copy;
  }

  /**
   * Create a copy of an existing resolver. All the inference variables contained
   * within the resolver will be substituted for the new inference variables, and
   * all the bounds on them will be substituted for equivalent bounds. These
   * mappings will be entered into the given map when they are made.
   * 
   * @param isomorphism
   *          an isomorphism for inference variables
   * @return A newly derived resolver, with each instance of an inference variable
   *         substituted for new mappings.
   */
  @Override
  public TypeResolver deepCopy(Isomorphism isomorphism) {
    return new TypeResolver(bounds.deepCopy(isomorphism));
  }

  /**
   * @return The bound set backing this resolver.
   */
  public BoundSet getBounds() {
    return bounds;
  }

  private Stream<TypeVariable<?>> getAllTypeParameters(GenericDeclaration declaration) {
    if (declaration instanceof Class<?>) {
      return ParameterizedTypes.getAllTypeParameters((Class<?>) declaration);
    } else if (declaration instanceof Executable) {
      return concat(
          ParameterizedTypes.getAllTypeParameters(((Executable) declaration).getDeclaringClass()),
          stream(declaration.getTypeParameters()));
    } else {
      throw new AssertionError();
    }
  }

  private Map<TypeVariable<?>, InferenceVariable> inferTypeParametersImpl(
      GenericDeclaration declaration,
      Map<TypeVariable<?>, ? extends Type> existingCaptures) {

    Map<TypeVariable<?>, InferenceVariable> newCaptures = getAllTypeParameters(declaration)
        .filter(p -> !existingCaptures.containsKey(p))
        .collect(
            toMap(
                identity(),
                v -> new InferenceVariable(v.getName()),
                throwingReduce(),
                LinkedHashMap::new));

    if (!newCaptures.isEmpty()) {
      bounds = bounds.withInferenceVariables(newCaptures.values());

      TypeSubstitution substitution = new TypeSubstitution(existingCaptures)
          .where(newCaptures::containsKey, newCaptures::get);

      bounds = concat(newCaptures.entrySet().stream(), existingCaptures.entrySet().stream())
          .reduce(
              bounds,
              (b, e) -> b
                  .withIncorporated()
                  .subtype(
                      e.getValue(),
                      substitution.resolve(uncheckedIntersectionOf(e.getKey().getBounds()))),
              throwingReduce());
    }

    return newCaptures;
  }

  public Stream<Map.Entry<TypeVariable<?>, InferenceVariable>> inferTypeParameters(
      GenericDeclaration declaration,
      Map<TypeVariable<?>, ? extends Type> existingCaptures) {
    return inferTypeParametersImpl(declaration, existingCaptures).entrySet().stream();
  }

  /**
   * Each type variable within the given {@link GenericDeclaration}, and each
   * non-statically enclosing declaration thereof, is incorporated into the
   * backing {@link BoundSet}.
   * 
   * @param declaration
   *          the declaration we wish to incorporate
   * @param enclosing
   *          the parameterized enclosing declaration
   * @return mapping from the {@link InferenceVariable}s on the given declaration,
   *         to their new capturing {@link InferenceVariable}s
   */
  public Stream<Map.Entry<TypeVariable<?>, InferenceVariable>> inferTypeParameters(
      GenericDeclaration declaration,
      ParameterizedType enclosing) {
    if (enclosing == null) {
      return inferTypeParameters(declaration);
    }

    Object enclosingClass = enclosing.getRawType();
    if (!getEnclosingDeclaration(declaration).get().equals(enclosingClass)) {
      throw new ReflectionException(
          REFLECTION_PROPERTIES.incorrectEnclosingDeclaration(enclosing.getRawType(), declaration));
    }

    return inferTypeParameters(
        declaration,
        ParameterizedTypes.getAllTypeArguments(enclosing).collect(entriesToMap()));
  }

  /**
   * Each type variable within the given {@link GenericDeclaration}, and each
   * non-statically enclosing declaration thereof, is incorporated into the
   * backing {@link BoundSet}.
   * 
   * @param declaration
   *          the declaration we wish to incorporate
   * @return a mapping from the {@link InferenceVariable}s on the given
   *         declaration, to their new capturing {@link InferenceVariable}s
   */
  public Stream<Map.Entry<TypeVariable<?>, InferenceVariable>> inferTypeParameters(
      GenericDeclaration declaration) {
    Map<TypeVariable<?>, InferenceVariable> inferenceVariables = getEnclosingDeclaration(
        declaration).map(this::inferTypeParametersImpl).orElse(emptyMap());

    return concat(
        inferenceVariables.entrySet().stream(),
        inferTypeParameters(declaration, inferenceVariables));
  }

  private Map<TypeVariable<?>, InferenceVariable> inferTypeParametersImpl(
      GenericDeclaration declaration) {
    return inferTypeParameters(declaration)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Optional<GenericDeclaration> getEnclosingDeclaration(GenericDeclaration declaration) {
    if (declaration instanceof Executable
        && !Modifier.isStatic(((Executable) declaration).getModifiers())) {
      return Optional.of(((Executable) declaration).getDeclaringClass());

    } else if (declaration instanceof Class<?>
        && !Modifier.isStatic(((Class<?>) declaration).getModifiers())) {
      return Optional.ofNullable(((Class<?>) declaration).getEnclosingClass());

    } else {
      return Optional.empty();
    }
  }

  /**
   * Add inference variables for the type parameters of the given type to the
   * resolver, then incorporate containment constraints based on the arguments of
   * the given type.
   * 
   * @param type
   *          The type whose generic type arguments we wish to perform inference
   *          operations over.
   * @return A parameterized type derived from the given type, with inference
   *         variables in place of wildcards where appropriate.
   */
  public GenericArrayType inferTypeArguments(GenericArrayType type) {
    Type innerComponent = Types.getInnerComponentType(type);
    if (innerComponent instanceof ParameterizedType) {
      return arrayFromComponent(
          inferTypeArguments((ParameterizedType) innerComponent),
          Types.getArrayDimensions(type));
    } else
      return type;
  }

  /**
   * Add inference variables for the type parameters of the given type to the
   * resolver, then incorporate containment constraints based on the arguments of
   * the given type.
   * 
   * @param type
   *          The type whose generic type arguments we wish to perform inference
   *          operations over.
   * @return A parameterized type derived from the given type, with inference
   *         variables in place of wildcards where appropriate.
   */
  public ParameterizedType inferTypeArguments(ParameterizedType type) {
    Class<?> rawType = (Class<?>) type.getRawType();

    List<Entry<TypeVariable<?>, Type>> typeArguments = getAllTypeArguments(type).collect(toList());

    /*
     * TODO this is a bit lazy, we should probably only create new inference
     * variables for parameters with wildcard arguments.
     */
    Map<TypeVariable<?>, InferenceVariable> inferenceVariables = inferTypeParametersImpl(rawType);

    for (Map.Entry<TypeVariable<?>, Type> argument : typeArguments) {
      bounds = new ConstraintFormula(
          Kind.CONTAINMENT,
          inferenceVariables.get(argument.getKey()),
          argument.getValue()).reduce(bounds);
    }

    return (ParameterizedType) substituteInstantiations(
        ParameterizedTypes.parameterizeUnchecked(rawType, inferenceVariables::get));
  }

  public ParameterizedType inferTypeParameters(Class<?> declaration) {
    return ParameterizedTypes.parameterize(declaration, inferTypeParametersImpl(declaration)::get);
  }

  /**
   * Incorporate a new inference variable for a given wildcard type, and add the
   * bounds of the wildcard as bounds to the inference variable.
   * 
   * @param type
   *          The wildcard type to capture as a bounded inference variable.
   * @return The new inference variable created to satisfy the given wildcard.
   */
  public InferenceVariable inferWildcardType(WildcardType type) {
    InferenceVariable w = new InferenceVariable();
    BoundSet bounds = this.bounds.withInferenceVariables(w);

    for (Type lowerBound : type.getLowerBounds())
      bounds = new ConstraintFormula(Kind.SUBTYPE, lowerBound, w).reduce(bounds);

    for (Type upperBound : type.getUpperBounds())
      bounds = new ConstraintFormula(Kind.SUBTYPE, w, upperBound).reduce(bounds);

    this.bounds = bounds;

    return w;
  }

  /**
   * Infer proper instantiations for each inference variable registered within
   * this {@link TypeResolver} instance.
   * 
   * @return A mapping from each inference variable registered under this
   *         resolver, to their newly inferred instantiations.
   */
  public Stream<Entry<InferenceVariable, Type>> resolve() {
    resolveIndependentSet(bounds.getInferenceVariables().collect(toList()));
    return bounds
        .getInferenceVariables()
        .map(i -> new AbstractMap.SimpleEntry<>(i, getInstantiation(i)));
  }

  /**
   * Infer a proper instantiations for each {@link InferenceVariable} mentioned by
   * the given type.
   * 
   * @param type
   *          the type whose proper form we wish to infer
   * @return a new type derived from the given type by substitution of
   *         instantiations for each {@link InferenceVariable} mentioned
   */
  public Type resolve(Type type) {
    resolve(
        getAllMentionedBy(type, getBounds()::containsInferenceVariable)
            .map(InferenceVariable.class::cast)
            .collect(toList()));

    return new TypeSubstitution(
        t -> getBounds().containsInferenceVariable(t)
            ? getInstantiation((InferenceVariable) t)
            : null).resolve(type);
  }

  /**
   * Infer a proper instantiations for a single given {@link InferenceVariable}.
   * 
   * @param inferenceVariable
   *          the type whose proper form we wish to infer
   * @return a new instantiation for the given {@link InferenceVariable}
   */
  public Type resolve(InferenceVariable inferenceVariable) {
    if (getBounds().containsInferenceVariable(inferenceVariable)) {
      if (!getBounds().getBoundsOn(inferenceVariable).getInstantiation().isPresent()) {
        Set<InferenceVariable> set = new HashSet<>(1);
        set.add(inferenceVariable);
        resolve(set);
      }
      return getInstantiation(inferenceVariable);
    } else {
      return inferenceVariable;
    }
  }

  /**
   * Infer proper instantiations for the given {@link InferenceVariable}s.
   * 
   * @param variables
   *          the inference variables for which we wish to infer instantiations
   * @return a mapping from each of the given inference variables to their
   *         inferred instantiations
   */
  public Stream<Map.Entry<InferenceVariable, Type>> resolve(InferenceVariable... variables) {
    return resolve(Arrays.asList(variables));
  }

  /**
   * Infer proper instantiations for the given {@link InferenceVariable}s.
   * 
   * @param variables
   *          The inference variables for which we wish to infer instantiations.
   * @return A mapping from each of the given inference variables to their
   *         inferred instantiations.
   */
  public Stream<Map.Entry<InferenceVariable, Type>> resolve(
      Collection<? extends InferenceVariable> variables) {
    /*
     * Given a set of inference variables to resolve, let V be the union of this set
     * and all variables upon which the resolution of at least one variable in this
     * set depends.
     */
    Set<InferenceVariable> independentSet = variables
        .stream()
        .filter(getBounds()::containsInferenceVariable)
        .filter(v -> !bounds.getBoundsOn(v).getInstantiation().isPresent())
        .flatMap(v -> bounds.getBoundsOn(v).getRemainingDependencies())
        .collect(toSet());

    resolveIndependentSet(independentSet);

    return variables
        .stream()
        .map(
            i -> new AbstractMap.SimpleEntry<>(
                i,
                bounds
                    .getBoundsOn(i)
                    .getInstantiation()
                    .orElseThrow(
                        () -> new ReflectionException(
                            REFLECTION_PROPERTIES.cannotInstantiateInferenceVariable(i, bounds)))));
  }

  private void resolveIndependentSet(Collection<InferenceVariable> variables) {
    /*
     * If every variable in V has an instantiation, then resolution succeeds and
     * this procedure terminates.
     */
    while (variables != null && !variables.isEmpty()) {
      /*
       * Otherwise, let { α1, ..., αn } be a non-empty subset of uninstantiated
       * variables in V such that i) for all i (1 ≤ i ≤ n), if αi depends on the
       * resolution of a variable β, then either β has an instantiation or there is
       * some j such that β = αj; and ii) there exists no non-empty proper subset of {
       * α1, ..., αn } with this property. Resolution proceeds by generating an
       * instantiation for each of α1, ..., αn based on the bounds in the bound set:
       */
      Set<InferenceVariable> minimalSet = new HashSet<>(variables);
      for (InferenceVariable variable : variables) {
        Set<InferenceVariable> remainingOnVariable = bounds
            .getBoundsOn(variable)
            .getRemainingDependencies()
            .collect(toSet());

        if (remainingOnVariable.size() < minimalSet.size()) {
          minimalSet = remainingOnVariable;
        } else if (remainingOnVariable.size() == minimalSet.size()
            && !bounds.getRelatedCaptureConversions(remainingOnVariable).findAny().isPresent()) {
          minimalSet = remainingOnVariable;
        }
      }

      resolveMinimalIndepdendentSet(minimalSet);

      bounds
          .getInferenceVariableBounds()
          .filter(i -> i.getInstantiation().isPresent())
          .map(InferenceVariableBounds::getInferenceVariable)
          .forEach(variables::remove);
    }
  }

  private void resolveMinimalIndepdendentSet(Set<InferenceVariable> minimalSet) {
    if (!bounds.getRelatedCaptureConversions(minimalSet).findAny().isPresent()) {
      /*
       * If the bound set does not contain a bound of the form G<..., αi, ...> =
       * capture(G<...>) for all i (1 ≤ i ≤ n), then a candidate instantiation Ti is
       * defined for each αi:
       */
      Map<InferenceVariable, Type> instantiationCandidates = new HashMap<>();

      try {
        for (InferenceVariable variable : minimalSet) {
          IdentityProperty<Boolean> hasThrowableBounds = new IdentityProperty<>(false);

          Type instantiationCandidate;
          List<Type> properLowerBounds = bounds
              .getBoundsOn(variable)
              .getLowerBounds()
              .filter(InferenceVariable::isProperType)
              .collect(toList());
          if (!properLowerBounds.isEmpty()) {
            /*
             * If αi has one or more proper lower bounds, L1, ..., Lk, then Ti = lub(L1,
             * ..., Lk) (§4.10.4).
             */
            instantiationCandidate = new TypeBounds().leastUpperBound(properLowerBounds);
          } else if (hasThrowableBounds.get()) {
            /*
             * Otherwise, if the bound set contains throws αi, and the proper upper bounds
             * of αi are, at most, Exception, Throwable, and Object, then Ti =
             * RuntimeException.
             */
            throw new AssertionError();
          } else {
            /*
             * Otherwise, where αi has proper upper bounds U1, ..., Uk, Ti = glb(U1, ...,
             * Uk) (§5.1.10).
             */
            instantiationCandidate = new TypeBounds()
                .greatestLowerBound(
                    bounds
                        .getBoundsOn(variable)
                        .getUpperBounds()
                        .filter(InferenceVariable::isProperType)
                        .collect(toList()));
          }

          instantiationCandidates.put(variable, instantiationCandidate);
        }

        bounds = bounds.withInstantiations(instantiationCandidates);

        return;
      } catch (ReflectionException e) {}
    }

    /*
     * the bound set contains a bound of the form G<..., αi, ...> = capture(G<...>)
     * for some i (1 ≤ i ≤ n), or;
     * 
     * If the bound set produced in the step above contains the bound false;
     * 
     * then let Y1, ..., Yn be fresh type variables whose bounds are as follows:
     * 
     * ...
     * 
     * Otherwise, for all i (1 ≤ i ≤ n), all bounds of the form G<..., αi, ...> =
     * capture(G<...>) are removed from the current bound set, and the bounds α1 =
     * Y1, ..., αn = Yn are incorporated.
     * 
     * If the result does not contain the bound false, then the result becomes the
     * new bound set, and resolution proceeds by selecting a new set of variables to
     * instantiate (if necessary), as described above.
     * 
     * Otherwise, the result contains the bound false, and resolution fails.
     */
    bounds = TypeVariableCapture.captureInferenceVariables(minimalSet, getBounds());
  }

  /**
   * Derive a new type from the type given, with any mentioned instances of
   * {@link InferenceVariable} substituted with their proper instantiations where
   * available, as per {@link #getInstantiation(InferenceVariable)}.
   * 
   * @param type
   *          The type we wish to resolve.
   * @return The resolved type.
   */
  public Type substituteInstantiations(Type type) {
    return new TypeSubstitution()
        .where(InferenceVariable.class::isInstance, t -> getInstantiation((InferenceVariable) t))
        .resolve(type);
  }

  /**
   * Resolve the proper instantiation of a given {@link InferenceVariable} if one
   * exists.
   * 
   * @param variable
   *          The inference variable whose proper instantiation we wish to
   *          determine.
   * @return The proper instantiation of the given {@link InferenceVariable} if
   *         one exists, otherwise the {@link InferenceVariable} itself.
   */
  public Type getInstantiation(InferenceVariable variable) {
    if (bounds.getInferenceVariables().anyMatch(variable::equals))
      return bounds.getBoundsOn(variable).getInstantiation().orElse(variable);
    else
      return variable;
  }

  /**
   * Incorporate the given bounds into the bounds of the resolver.
   * 
   * @param bounds
   *          the bounds to incorporate
   * @return this resolver
   */
  public TypeResolver incorporateBounds(BoundSet bounds) {
    this.bounds = this.bounds.withBounds(bounds);
    return this;
  }

  /**
   * Reduce the given constraint formula into the bound set of the resolver.
   * 
   * @param constraintFormula
   *          the constraint formula to reduce
   * @return this resolver
   */
  public TypeResolver reduceConstraint(ConstraintFormula constraintFormula) {
    bounds = constraintFormula.reduce(bounds);
    return this;
  }
}
