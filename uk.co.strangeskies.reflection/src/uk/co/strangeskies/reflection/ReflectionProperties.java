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

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import uk.co.strangeskies.text.properties.PropertyConfiguration;
import uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase;

/**
 * Properties and localized strings relating to types.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@PropertyConfiguration(keyCase = KeyCase.LOWER, keySplitString = ".")
public interface ReflectionProperties {
  String unsupportedType(Type type);

  String invalidAssignmentObject(Object object, Class<?> type);

  default String invalidTypeVariableCaptureBounds(TypeVariableCapture capture) {
    return invalidTypeVariableCaptureBounds(
        capture,
        capture.getLowerBounds(),
        capture.getUpperBounds());
  }

  String invalidTypeVariableCaptureBounds(
      TypeVariableCapture capture,
      Type[] lowerBounds,
      Type[] upperBounds);

  String improperCaptureType(TypeVariableCapture capture);

  String improperUpperBound(Type t, InferenceVariable inferenceVariable, BoundSet bounds);

  String cannotCaptureInferenceVariable(InferenceVariable key, Type value, BoundSet bounds);

  String cannotInstantiateInferenceVariable(InferenceVariable variable, BoundSet bounds);

  String cannotFindSubstitution(Type i);

  String invalidAnnotationValue(Method method, Object propertyValue);

  String invalidAnnotationProperties(
      Class<? extends Annotation> annotationClass,
      Set<String> keySet);

  String invalidAnnotationValue(
      Class<? extends Annotation> annotationClass,
      String name,
      Object propertyValue);

  String invalidEquality(Type first, Type second, BoundSet bounds);

  String invalidSubtype(Type subtype, Type supertype, BoundSet boundSet);

  String invalidCaptureConversion(CaptureConversion captureConversion, BoundSet boundSet);

  String invalidBoundSet(String message, BoundSet boundSet);

  String cannotReduceConstraint(ConstraintFormula constraintFormula, BoundSet bounds);

  String invalidIntersectionTypes(
      Collection<? extends Type> flattenedTypes,
      Type iType,
      Type jType);

  String invalidIntersectionType(Collection<? extends Type> flattenedTypes);

  String incompatibleImports(Class<?> class1, Class<?> class2);

  String invalidUpperBound(WildcardType wildcardType);

  String cannotCopyInferenceVariable(InferenceVariable inferenceVariable, BoundSet boundSet);

  String cannotFilterCapture(CaptureConversion capture);

  String cannotCaptureMultipleTimes(
      InferenceVariable inferenceVariable,
      CaptureConversion capture,
      CaptureConversion captureConversion);

  String invalidStaticMethodArguments(Method method, List<?> a);

  String invalidCastObject(Object object, Type objectType, Type castType);

  String invalidVariableArityInvocation(Executable executableMember);

  String cannotResolveReceiver(Member executableMember, Type type);

  String cannotResolveTarget(Member executableMember, Type type);

  String cannotResolveAmbiguity(Executable firstCandidate, Executable secondCandidate);

  String cannotResolveApplicable(
      Set<? extends Executable> candidates,
      List<? extends Type> parameters);

  String incompatibleArgument(
      Type givenArgumentCaptured,
      Type genericParameterCaptured,
      int i,
      Executable executableMember);

  String incompatibleArgument(
      Object object,
      Type objectType,
      Type genericParameterCaptured,
      int i,
      Executable executableMember);

  String cannotResolveInvocationType(Executable executableMember, List<? extends Type> arguments);

  String cannotGetField(Object target, Field fieldMember);

  String cannotSetField(Object target, Object value, Field fieldMember);

  String cannotFindMethodOn(Type type);

  default String incorrectTypeArgumentCount(
      GenericDeclaration declaration,
      List<Type> typeArguments) {
    return incorrectTypeArgumentCount(
        Arrays.asList(declaration.getTypeParameters()),
        typeArguments);
  }

  String incorrectTypeArgumentCount(List<TypeVariable<?>> parameters, List<Type> typeArguments);

  String duplicateTypeVariable(String n);

  String cannotResolveSupertype(Type type, Class<?> superclass);

  String incorrectEnclosingDeclaration(Type rawType, GenericDeclaration declaration);

  String cannotResolveInvocationOnTypeWithWildcardParameters(Type type);

  String cannotParameterizeMethodOnRawType(Executable executable);

  String cannotResolveTypeVariable(TypeVariable<?> parameter, Object object);

  String memberMustBeStatic(Member member);

  String declaringClassMustBeStatic(Member member);

  String invocationFailed(Executable executable, Type instance, Object[] arguments);

  String cannotParameterizeEnclosingExecutable(Class<?> enclosedClass);

  String noEnclosingDeclaration(Type type);

  String cannotParameterizeWithReplacement(Type type, Type currentType);

  /*
   * The given type variable cannot be found in the context of the given
   * declaration and so cannot be parameterized.
   */
  String cannotParameterizeOnDeclaration(TypeVariable<?> type, GenericDeclaration declaration);

  String cannotOverrideConstructor(Executable member, Type type);

  String cannotParameterizeInference();
}
