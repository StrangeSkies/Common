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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import uk.co.strangeskies.reflection.ReflectionProperties;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

@SuppressWarnings("javadoc")
@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".")
public interface CodeGenerationProperties {
  ReflectionProperties reflection();

  String cannotInstantiateClassDefinition(
      ClassDefinition<?, ?> classDefinition,
      TypeToken<?> superType);

  String cannotOverrideMethod(Method overriddenMethod);

  String incompatibleReturnTypes(Type override, Method inherited);

  String incompatibleParameterTypes(List<Type> parameterTypes, Method inherited);

  String duplicateMethodDeclaration(MethodSignature<?> override);

  String mustOverrideMethods(Collection<Method> classMethod);

  String undefinedVariable(ParameterSignature<?> variable);

  String cannotResolveEnclosingInstance(ClassDeclaration<?, ?> receiverClass);

  String cannotRedeclareVariable(ParameterSignature<?> variable);

  String incompleteStatementExecution();

  String incompleteExpressionEvaluation();

  String cannotFindMethodOn(Class<?> superClass, ErasedMethodSignature erasedMethodSignature);

  String incompatibleReturnType(TypeToken<?> returnType, MethodDeclaration<?, ?> methodDeclaration);

  String mustImplementMethod(MethodDeclaration<?, ?> method);

  String classNameAlreadyRegistered(ClassSignature<?> classSignature);

  String staticMethodCannotBeDefault(MethodSignature<?> methodDeclaration);

  String cannotResolveTypeVariable(String typeVariableName, ParameterizedSignature<?> signature);

  String cannotExtendMultipleClassTypes(AnnotatedType first, AnnotatedType second);

  String classOverridingNotSupported();

  String cannotOverrideExistingClass(String className);
}
