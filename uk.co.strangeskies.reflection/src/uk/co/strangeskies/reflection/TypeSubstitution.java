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
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.AnnotatedTypes.annotated;
import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.IntersectionTypes.intersectionOfImpl;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterizeUnchecked;
import static uk.co.strangeskies.reflection.TypeVariables.typeVariableExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.utility.Isomorphism;

/**
 * A TypeSubstitution object is a function mapping Type to Type, which
 * recursively visits each type mentioned by a given type and applies a
 * substitution to those it encounters which match a given condition.
 * 
 * @author Elias N Vasylenko
 *
 */
public class TypeSubstitution {
  private final Isomorphism isomorphism;
  private final Function<? super Type, ? extends Type> mapping;
  private final Supplier<Boolean> empty;
  private final boolean includeTypeVariables;

  /**
   * Create a new TypeSubstitution with no initial substitution rules.
   */
  public TypeSubstitution() {
    isomorphism = new Isomorphism();
    mapping = t -> null;
    empty = () -> true;
    includeTypeVariables = false;
  }

  /**
   * Create a new TypeSubstitution to apply the given mapping function. Typically
   * we do something like create an instance from a {@link Map} of Type instances
   * to other Type instances, then pass the method reference of
   * {@link Map#get(Object)} for that map to this constructor. For this specific
   * example use case though, {@link #TypeSubstitution(Map)} would perform
   * slightly better.
   * 
   * @param mapping
   *          A mapping function for transforming encountered types to their
   *          substitution types.
   */
  public TypeSubstitution(Function<? super Type, ? extends Type> mapping) {
    isomorphism = new Isomorphism();
    this.mapping = mapping;
    empty = () -> false;
    includeTypeVariables = false;
  }

  /**
   * Create a new TypeSubstitution to apply the given mapping. This is more
   * efficient than the more general {@link #TypeSubstitution(Function)}
   * constructor, as it can skip type traversal for empty maps.
   * 
   * @param mapping
   *          A mapping function for transforming encountered types to their
   *          substitution types.
   */
  public TypeSubstitution(Map<? extends Type, ? extends Type> mapping) {
    isomorphism = new Isomorphism();
    this.mapping = mapping::get;
    empty = mapping::isEmpty;
    includeTypeVariables = false;
  }

  private TypeSubstitution(TypeSubstitution substitution) {
    isomorphism = substitution.isomorphism;
    mapping = substitution.mapping;
    empty = substitution.empty;
    includeTypeVariables = true;
  }

  private TypeSubstitution(TypeSubstitution substitution, Isomorphism isomorphism) {
    this.isomorphism = isomorphism;
    this.mapping = substitution.mapping;
    this.empty = () -> false;
    this.includeTypeVariables = substitution.includeTypeVariables;
  }

  /**
   * Create a new TypeSubstitution by adding a specific single substitution rule
   * to the receiver of the invocation. The new rule will be checked and applied
   * before any existing rules. The receiving TypeSubstitution of invocation of
   * this method will remain unchanged.
   * 
   * @param from
   *          The type to match in application of this rule.
   * @param to
   *          The type to substitute for types which match the rule.
   * @return A new TypeSubstitution object with the rule added.
   */
  public TypeSubstitution where(Type from, Type to) {
    return where(t -> Objects.equals(from, t), t -> to);
  }

  /**
   * Create a new TypeSubstitution by adding a specific single substitution rule
   * to the receiver of the invocation. The new rule will be checked and applied
   * before any existing rules. The receiving TypeSubstitution of invocation of
   * this method will remain unchanged.
   * 
   * @param from
   *          The type matching condition of the new rule.
   * @param to
   *          The substitution transformation to apply to types matching the given
   *          condition.
   * @return A new TypeSubstitution object with the rule added.
   */
  public TypeSubstitution where(
      Predicate<? super Type> from,
      Function<? super Type, ? extends Type> to) {
    return new TypeSubstitution(t -> {
      Type result = null;
      if (from.test(t)) {
        result = to.apply(t);
      }
      if (result == null) {
        result = mapping.apply(t);
      }
      return result;
    });
  }

  /**
   * Create a new TypeSubstitution which is the same as the receiver with the
   * additional behavior that type variables are also included for bounds
   * substitution. Normally it makes sense to exclude type variable bounds for
   * substitution, as their type is immutable and baked into their defining class
   * definition, but sometimes we may still want this behavior.
   * 
   * @return a new TypeSubstitution object with the rule added
   */
  public TypeSubstitution withTypeVariables() {
    return new TypeSubstitution(this);
  }

  /**
   * Create a new TypeSubstitution which is the same as the receiver with the
   * additional behavior that it maps types according to the given
   * {@link Isomorphism}.
   * 
   * @param isomorphism
   *          an isomorphism
   * @return a new TypeSubstitution object over the given isomorphism
   */
  public TypeSubstitution withIsomorphism(Isomorphism isomorphism) {
    return new TypeSubstitution(this, isomorphism);
  }

  /**
   * Resolve the result of this substitution as applied to the given type.
   * 
   * @param type
   *          The type for which we want to make a substitution.
   * @return The result of application of this substitution. The result is
   *         <em>not</em> guaranteed to be well formed with respect to bounds.
   */
  public Type resolve(Type type) {
    if (empty.get())
      return type;
    else
      return resolve(type, new IdentityProperty<>(false));
  }

  protected Type resolve(Type type, IdentityProperty<Boolean> changed) {
    if (isomorphism.byIdentity().getMappedNodes().contains(type)) {
      Type mapping = (Type) isomorphism.byIdentity().getMapping(type);
      if (mapping != type) {
        changed.set(true);
      }
      return mapping;

    } else {
      Type mapping = this.mapping.apply(type);
      if (mapping != null) {
        if (mapping != type) {
          changed.set(true);
        }
        return mapping;

      } else {
        if (changed.get()) {
          changed = new IdentityProperty<>(false);
        }

        if (type == null) {
          return null;

        } else if (type instanceof Class) {
          return resolveType(type);

        } else if (type instanceof TypeVariableCapture) {
          return includeTypeVariables
              ? resolveTypeVariableCapture((TypeVariableCapture) type, changed)
              : type;

        } else if (type instanceof TypeVariable<?>) {
          return includeTypeVariables ? resolveTypeVariable((TypeVariable<?>) type, changed) : type;

        } else if (type instanceof InferenceVariable) {
          return resolveType(type);

        } else if (type instanceof IntersectionType) {
          return resolveIntersectionType((IntersectionType) type, changed);

        } else if (type instanceof WildcardType) {
          return resolveWildcardType((WildcardType) type, changed);

        } else if (type instanceof GenericArrayType) {
          return resolveGenericArrayType((GenericArrayType) type, changed);

        } else if (type instanceof ParameterizedType) {
          return resolveParameterizedType((ParameterizedType) type, changed);
        }
      }

      throw new IllegalArgumentException(
          "Cannot resolve unrecognised type '" + type + "' of class'" + type.getClass() + "'.");
    }
  }

  private Type resolveType(Type type) {
    return isomorphism.byIdentity().getMapping(type, Function.identity());
  }

  private Type resolveGenericArrayType(
      GenericArrayType type,
      IdentityProperty<Boolean> changedScoped) {
    return isomorphism
        .byIdentity()
        .getMapping(
            type,
            t -> arrayFromComponent(resolve(t.getGenericComponentType(), changedScoped)));
  }

  private Type resolveTypeVariableCapture(
      TypeVariableCapture type,
      IdentityProperty<Boolean> changed) {
    return isomorphism.byIdentity().getProxiedMapping(type, TypeVariable.class, i -> {

      List<Type> upperBounds = Collections.emptyList();
      if (type.getUpperBounds().length > 0) {
        upperBounds = resolveTypes(type.getUpperBounds(), changed);
      }

      List<Type> lowerBounds = Collections.emptyList();
      if (type.getLowerBounds().length > 0) {
        upperBounds = resolveTypes(type.getLowerBounds(), changed);
      }

      if (changed.get()) {
        return new TypeVariableCapture(
            (Type[]) upperBounds.toArray(),
            (Type[]) lowerBounds.toArray(),
            type);
      } else {
        return type;
      }
    });
  }

  private Type resolveTypeVariable(TypeVariable<?> type, IdentityProperty<Boolean> changed) {
    return isomorphism.byIdentity().getProxiedMapping(type, TypeVariable.class, i -> {

      if (type.getBounds().length > 0) {
        List<Type> bounds = resolveTypes(type.getBounds(), changed);
        if (changed.get()) {
          return typeVariableExtending(
              type.getGenericDeclaration(),
              type.getName(),
              annotated(bounds));
        } else {
          return type;
        }

      } else
        return type;
    });
  }

  private Type resolveWildcardType(WildcardType type, IdentityProperty<Boolean> changed) {
    return isomorphism.byIdentity().getProxiedMapping(type, WildcardType.class, i -> {

      if (type.getLowerBounds().length > 0) {
        List<Type> bounds = resolveTypes(type.getLowerBounds(), changed);
        if (changed.get()) {
          return wildcardSuper(bounds);
        } else {
          return type;
        }

      } else if (type.getUpperBounds().length > 0) {
        List<Type> bounds = resolveTypes(type.getUpperBounds(), changed);
        if (changed.get()) {
          return wildcardExtending(bounds);
        } else {
          return type;
        }

      } else
        return type;
    });
  }

  private Type resolveIntersectionType(IntersectionType type, IdentityProperty<Boolean> changed) {
    return isomorphism.byIdentity().getPartialMapping(type, (i, partial) -> {

      IdentityProperty<IntersectionType> property = new IdentityProperty<>();
      Type proxy = IntersectionTypes.proxy(property::get);
      partial.accept(() -> proxy);

      IntersectionType result;

      List<Type> types = resolveTypes(type.getTypes(), changed);
      if (changed.get()) {
        result = intersectionOfImpl(types);
      } else {
        result = type;
      }

      property.set(result);

      return result;
    });
  }

  private Type resolveParameterizedType(ParameterizedType type, IdentityProperty<Boolean> changed) {
    return isomorphism.byIdentity().getProxiedMapping(type, ParameterizedType.class, i -> {

      List<Type> arguments = resolveTypes(type.getActualTypeArguments(), changed);
      Type owner = resolve(type.getOwnerType(), changed);

      if (changed.get()) {
        return parameterizeUnchecked(owner, (Class<?>) type.getRawType(), arguments);
      } else {
        return type;
      }
    });
  }

  private List<Type> resolveTypes(Type[] types, IdentityProperty<Boolean> changed) {
    return stream(types).map(t -> resolve(t, changed)).collect(toList());
  }
}
