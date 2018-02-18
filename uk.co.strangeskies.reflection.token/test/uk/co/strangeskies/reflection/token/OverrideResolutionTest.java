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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.token.ExecutableToken.forMethod;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;
import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import org.junit.Test;

import uk.co.strangeskies.reflection.token.ExecutableToken;

interface A {
	Object method();
}

interface B {
	Object method();
}

interface C extends A, B {
	@Override
	Number method();
}

abstract class D implements A {}

abstract class E extends D implements C {}

abstract class F implements C {
	@Override
	public abstract Integer method();
}

public class OverrideResolutionTest {
	public static final String METHOD_NAME = "method";

	@Test
	public void withReceiverTypeDoesNotOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = forClass(A.class)
				.methods()
				.filter(anyMethod().named(METHOD_NAME))
				.collect(resolveOverload());

		method = method.getOverride(forClass(D.class));

		assertThat(method.getMember(), equalTo(A.class.getMethod(METHOD_NAME)));
	}

	@Test
	public void withReceiverThenGetOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = forClass(A.class)
				.methods()
				.filter(anyMethod().named(METHOD_NAME))
				.collect(resolveOverload());

		method = method.getOverride(forClass(C.class));

		assertThat(method.getMember(), equalTo(C.class.getMethod(METHOD_NAME)));
	}

	@Test
	public void getOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = forMethod(A.class.getMethod(METHOD_NAME))
				.getOverride(forClass(C.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(C.class));
	}

	@Test
	public void getIndirectOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = forMethod(A.class.getMethod(METHOD_NAME))
				.getOverride(forClass(F.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(F.class));
	}

	@Test
	public void getInterfaceBeforeClassOverride() throws NoSuchMethodException, SecurityException {
		ExecutableToken<? extends A, ?> method = forMethod(A.class.getMethod(METHOD_NAME))
				.getOverride(forClass(E.class));

		assertThat(method.getMember().getDeclaringClass(), equalTo(C.class));
	}
}
