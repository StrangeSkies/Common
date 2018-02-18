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

import static org.junit.Assert.assertNotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE_USE)
@interface TestAnnotation {}

class OuterExtendsNested extends Container.Nested<@TestAnnotation Object> {}

class Container {
	public class Inner<T> {}

	public class InnerExtendsNested extends Nested<@TestAnnotation Object> {}

	public static class Nested<T> {}

	public static class NestedExtendsNested extends Nested<@TestAnnotation Object> {}
}

/**
 * Test for bug JDK-8146167 in javac.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class JavacSupertypeAnnotationsTest {

	@Parameters(name = "{0}")
	public static Object[][] testClasses() {
		class StaticInner<T> {}

		@SuppressWarnings("unused")
		class StaticInnerExtendsStaticInner extends StaticInner<@TestAnnotation Object> {}

		return new Object[][] {

				/*-
				// ecj and javac fail
				{ "outer extends nested", OuterExtendsNested.class },
				
				// ecj and javac fail
				{ "inner extends nested", Container.InnerExtendsNested.class },
				
				// ecj and javac fail
				{ "nested extends nested", Container.NestedExtendsNested.class },
				
				// ecj and javac fail
				{ "anonymous extends nested", new Container.Nested<@TestAnnotation Object>() {}.getClass() },
				
				// ecj fails
				{ "static inner extends static inner", StaticInnerExtendsStaticInner.class },
				
				// ecj fails
				{ "anonymous extends static inner", new StaticInner<@TestAnnotation Object>() {}.getClass() },
				
				// !javac compiler errors!
				{ "anonymous extends inner", new Container().new Inner<@TestAnnotation Object>() {}.getClass() }
				 */
		};
	}

	private final Class<?> testClass;

	public JavacSupertypeAnnotationsTest(String testName, Class<?> testClass) {
		this.testClass = testClass;
	}

	// @Test
	public void supertypeParameterAnnotationPresenceTest() {
		AnnotatedParameterizedType superType = (AnnotatedParameterizedType) testClass.getAnnotatedSuperclass();

		AnnotatedType superTypeParameter = superType.getAnnotatedActualTypeArguments()[0];

		assertNotNull(superTypeParameter.getAnnotation(TestAnnotation.class));
	}
}
