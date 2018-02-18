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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.Types.isAssignable;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.overrideMethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;

class MethodOverride<T> {
  private final MethodOverrides<T> methodOverrides;

  private final Set<Method> interfaceMethods;
  private Method classMethod;

  private MethodSignature<?> override;

  public MethodOverride(MethodOverrides<T> methodOverrides) {
    this.methodOverrides = methodOverrides;
    interfaceMethods = new HashSet<>();
  }

  public void inherit(Method method) {
    if (method.getDeclaringClass().isInterface()) {
      interfaceMethods.add(method);
    } else {
      classMethod = method;
    }
  }

  public void validate() {
    if (classMethod == null) {
      Set<Method> defaultMethods = interfaceMethods.stream().filter(m -> m.isDefault()).collect(
          Collectors.toSet());
      if (defaultMethods.size() > 1) {
        throw new CodeGenerationException(CODEGEN_PROPERTIES.mustOverrideMethods(interfaceMethods));
      } else if (defaultMethods.isEmpty()) {
        for (Method interfaceMethod : interfaceMethods) {
          try {
            validateOverride(interfaceMethod);
            return;
          } catch (Exception e) {}
        }
        throw new CodeGenerationException(CODEGEN_PROPERTIES.mustOverrideMethods(interfaceMethods));
      } else {
        Method defaultMethod = defaultMethods.iterator().next();

        validateOverride(defaultMethod);
      }
    } else {
      if (isAbstract(classMethod.getModifiers())) {
        throw new CodeGenerationException(
            CODEGEN_PROPERTIES.mustOverrideMethods(Arrays.asList(classMethod)));
      }

      validateOverride(classMethod);
    }
  }

  private void validateOverride(Method method) {
    ExecutableToken<?, ?> overriddenToken = methodOverrides.getInvocable(method).parameterize();

    getMethods().stream().filter(m -> m != method).forEach(
        m -> validateOverrideAgainst(
            overriddenToken.getReturnType().getType(),
            overriddenToken.getParameters().map(ExecutableParameter::getType).collect(toList()),
            stream(method.getTypeParameters())
                .map(TypeVariableSignature::typeVariableSignature)
                .collect(toList()),
            m));
  }

  private void validateOverrideAgainst(
      Type returnType,
      List<Type> parameters,
      List<TypeVariableSignature> typeParameters,
      Method inherited) {
    ExecutableToken<?, ?> inheritedToken = methodOverrides.getInvocable(inherited).parameterize();

    if (!isAssignable(returnType, inheritedToken.getReturnType().resolve().getType())) {
      throw new CodeGenerationException(
          CODEGEN_PROPERTIES.incompatibleReturnTypes(returnType, inherited));
    }

    List<ExecutableParameter> overriddenParameters = inheritedToken.getParameters().collect(
        toList());
    for (int i = 0; i < parameters.size(); i++) {
      if (!isAssignable(overriddenParameters.get(i).getType(), parameters.get(i))) {
        throw new CodeGenerationException(
            CODEGEN_PROPERTIES.incompatibleParameterTypes(parameters, inherited));
      }
    }
  }

  private void validateOverride() {
    for (Method inherited : getMethods()) {
      validateOverrideAgainst(
          override.getReturnType().getType(),
          override.getParameters().map(v -> v.getType().getType()).collect(toList()),
          override.getTypeVariables().collect(toList()),
          inherited);
    }
  }

  public void override(MethodSignature<?> override) {
    if (this.override != null) {
      throw new CodeGenerationException(CODEGEN_PROPERTIES.duplicateMethodDeclaration(override));
    }

    if (classMethod != null && (Modifier.isPrivate(classMethod.getModifiers())
        || Modifier.isFinal(classMethod.getModifiers()))) {
      throw new CodeGenerationException(CODEGEN_PROPERTIES.cannotOverrideMethod(classMethod));
    }

    this.override = override;
    validateOverride();
  }

  public void overrideIfNecessary() {
    boolean classMethodMustBeOverridden = classMethod != null
        && isAbstract(classMethod.getModifiers());
    boolean interfaceMethodsMustBeOverridden = classMethod == null
        && interfaceMethods.stream().allMatch(i -> !i.isDefault());

    if (this.override == null) {
      if (classMethodMustBeOverridden || interfaceMethodsMustBeOverridden) {
        Method overrideSignatureMethod = classMethod;
        if (overrideSignatureMethod == null) {
          overrideSignatureMethod = interfaceMethods.iterator().next();
        }

        ExecutableToken<?, ?> executableToken = methodOverrides
            .getInvocable(overrideSignatureMethod)
            .parameterize();

        MethodSignature<?> signature = overrideMethodSignature(executableToken);

        override(signature);
      } else {
        validate();
      }
    }
  }

  public Optional<MethodSignature<?>> getOverride() {
    return Optional.ofNullable(override);
  }

  public Optional<Method> getClassMethod() {
    return Optional.ofNullable(classMethod);
  }

  public Set<Method> getInterfaceMethods() {
    return interfaceMethods;
  }

  public Set<Method> getMethods() {
    HashSet<Method> methods = new HashSet<>(interfaceMethods.size() + 1);
    if (classMethod != null) {
      methods.add(classMethod);
    }
    methods.addAll(interfaceMethods);
    return methods;
  }

  @Override
  public String toString() {
    return getMethods() + " : " + getOverride();
  }
}
