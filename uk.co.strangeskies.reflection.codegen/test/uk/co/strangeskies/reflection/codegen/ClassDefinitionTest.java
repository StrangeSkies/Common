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

import static uk.co.strangeskies.reflection.Visibility.PUBLIC;
import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;
import static uk.co.strangeskies.reflection.codegen.ConstructorSignature.constructorSignature;
import static uk.co.strangeskies.reflection.codegen.MethodImplementations.empty;
import static uk.co.strangeskies.reflection.codegen.MethodImplementations.returningLiteral;
import static uk.co.strangeskies.reflection.codegen.MethodImplementations.returningNull;
import static uk.co.strangeskies.reflection.codegen.MethodImplementations.returningVariable;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;
import static uk.co.strangeskies.reflection.codegen.ParameterSignature.parameterSignature;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyConstructor;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.VariableMatcher.anyVariable;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
@Ignore
public class ClassDefinitionTest {
  public interface Func<A, B> {
    B apply(A value);
  }

  public interface Default {
    default void method() {}
  }

  public static final ClassSignature<?> TEST_CLASS_SIGNATURE = classSignature()
      .packageName(ClassDeclarationTest.class.getPackage().getName())
      .simpleName("SelfSet")
      .withVisibility(PUBLIC)
      .constructor(constructorSignature().withVisibility(PUBLIC));

  public static final TypeToken<String> STRING_TYPE = new TypeToken<String>() {};

  @Test
  public void defineObject() throws InstantiationException, IllegalAccessException {
    Object instance = new ClassRegister(createClassLoader())
        .withClassSignature(TEST_CLASS_SIGNATURE)
        .loadClass()
        .newInstance();

    instance.hashCode();
  }

  @Test
  public void runnableClassInvocation() throws InstantiationException, IllegalAccessException {
    ClassDefinition<Void, ? extends Runnable> classDefinition = new ClassRegister(
        createClassLoader()).withClassSignature(TEST_CLASS_SIGNATURE.extending(Runnable.class));
    classDefinition = classDefinition.withImplementation(anyMethod().named("run"), empty());

    Runnable instance = classDefinition.loadClass().newInstance();

    instance.run();
  }

  @Test
  public void defineWithExplicitMethodDeclaration()
      throws InstantiationException, IllegalAccessException {
    /*
     * A block is something we don't always know the type of until we resolve the
     * type of it's context, e.g. in method overload resolution a lambda body must
     * be reinterpreted according to all applicable target types, and any return
     * statements examined according to that context.
     * 
     * Do we achieve this with a new kind of stand-in type? Or do existing models
     * already fit well (e.g. some sort of ad-hoc type variable)?
     */

    Func<String, String> instance = new ClassRegister(createClassLoader())
        .withClassSignature(
            TEST_CLASS_SIGNATURE.extending(new TypeToken<Func<String, String>>() {}).method(
                methodSignature("apply").withReturnType(STRING_TYPE).withParameters(
                    parameterSignature("value", STRING_TYPE))))
        .withImplementation(
            anyMethod().named("apply"),
            returningVariable(anyVariable().named("value")))
        .loadClass()
        .newInstance();

    String result = instance.apply("string");

    Assert.assertEquals("stringappend", result);
  }

  @Test
  public void defineWithInheritedMethodDeclarationBySignature()
      throws InstantiationException, IllegalAccessException {
    Func<String, String> instance = new ClassRegister(createClassLoader())
        .withClassSignature(
            TEST_CLASS_SIGNATURE.extending(new TypeToken<Func<String, String>>() {}))
        .withImplementation(
            anyMethod().named("apply").returning(String.class),
            returningLiteral(""))
        .loadClass()
        .newInstance();

    String result = instance.apply("string");

    Assert.assertEquals("stringstring", result);
  }

  @Test
  public void defineWithInheritedMethodDeclaration()
      throws InstantiationException, IllegalAccessException {
    Func<String, String> instance = new ClassRegister(createClassLoader())
        .withClassSignature(
            TEST_CLASS_SIGNATURE.extending(new TypeToken<Func<String, String>>() {}))
        .withImplementation(anyMethod().named("apply"), returningNull())
        .loadClass()
        .newInstance();

    String result = instance.apply("string");

    Assert.assertEquals("stringstring", result);
  }

  @Test(expected = ReflectionException.class)
  public void defineWithAbstractMethod() throws InstantiationException, IllegalAccessException {
    new ClassRegister(createClassLoader())
        .withClassSignature(TEST_CLASS_SIGNATURE.extending(Runnable.class))
        .loadClass()
        .newInstance();
  }

  @Test
  public void defineWithDefaultMethod() throws InstantiationException, IllegalAccessException {
    new ClassRegister(createClassLoader())
        .withClassSignature(TEST_CLASS_SIGNATURE.extending(Default.class))
        .withImplementation(anyConstructor(), empty())
        .loadClass()
        .newInstance();
  }

  private ClassLoader createClassLoader() {
    return new ByteArrayClassLoader(getClass().getClassLoader());
  }
}
