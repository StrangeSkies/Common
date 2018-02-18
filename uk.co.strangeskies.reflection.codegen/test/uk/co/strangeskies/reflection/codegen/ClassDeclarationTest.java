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

import static uk.co.strangeskies.reflection.codegen.ClassSignature.classSignature;

import java.util.Set;

import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.ClassRegister;
import uk.co.strangeskies.reflection.codegen.ClassSignature;
import uk.co.strangeskies.reflection.token.TypeToken;

@SuppressWarnings("javadoc")
public class ClassDeclarationTest {
	private static final ClassSignature<?> TEST_CLASS = classSignature()
			.packageName("test.package")
			.simpleName("TestClass");

	private interface StringMethod {
		String method(String parameter);
	}

	public interface ObjectMethod {
		String method(Object parameter);
	}

	private interface NumberMethod<N> {
		Number method(N parameter);
	}

	private interface NumberMethodSubType extends NumberMethod<String> {}

	@Test(expected = ReflectionException.class)
	public void supertypesWithIncompatibleParameterizationsTest() {
		new ClassRegister().withClassSignature(
				TEST_CLASS
						.extending(new TypeToken<Set<String>>() {}, new TypeToken<Iterable<Number>>() {}));
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodFromParameterizedTypeCollisionTest() {
		new ClassRegister().withClassSignature(
				TEST_CLASS
						.extending(new TypeToken<StringMethod>() {}, new TypeToken<NumberMethod<String>>() {}));
	}

	@Test(expected = ReflectionException.class)
	public void inheritedMethodCollisionTest() {
		new ClassRegister().withClassSignature(
				TEST_CLASS
						.extending(new TypeToken<ObjectMethod>() {}, new TypeToken<NumberMethod<String>>() {}));
	}

	@Test(expected = ReflectionException.class)
	public void indirectlyInheritedMethodCollisionTest() {
		new ClassRegister().withClassSignature(
				TEST_CLASS
						.extending(new TypeToken<ObjectMethod>() {}, new TypeToken<NumberMethodSubType>() {}));
	}

	@Test
	public void simpleOverrideMethodTest() {
		ClassSignature<?> signature = TEST_CLASS.extending(ObjectMethod.class);

		ClassRegister classSpace = new ClassRegister(ObjectMethod.class.getClassLoader())
				.withClassSignature(signature)
				.getRegister();

		classSpace.getClassDefinition(signature).get().getDeclaration().getMethodDeclaration(
				"method",
				Object.class);
	}
}
