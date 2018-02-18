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

import static java.util.stream.Collectors.toList;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Every.everyItem;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static uk.co.strangeskies.reflection.token.ExecutableToken.staticMethods;
import static uk.co.strangeskies.reflection.token.MethodMatcher.anyMethod;
import static uk.co.strangeskies.reflection.token.OverloadResolver.resolveOverload;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeArgument;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.ExecutableTokenTest.Outer.Inner;
import uk.co.strangeskies.reflection.token.TypeToken.Infer;

@SuppressWarnings("javadoc")
public class ExecutableTokenTest {
	static class Outer<T> {
		class Inner<U extends T> {
			public Inner(U parameter) {}
		}

		public <U> void method() {}
	}

	@Test
	public void methodWithDefaultTypeArguments() throws NoSuchMethodException, SecurityException {
		ExecutableToken<?, ?> method = ExecutableToken
				.forMethod(Outer.class.getMethod("method"))
				.parameterize();

		List<Type> typeArguments = method.getAllTypeArguments().map(TypeArgument::getType).collect(
				toList());

		assertThat(typeArguments, everyItem(instanceOf(TypeVariable.class)));
	}

	@Test(expected = ReflectionException.class)
	public void constructorWithWrongEnclosingTypeTest() {
		ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.parameterize()
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer<Integer>.Inner<Integer>>() {});
	}

	@SuppressWarnings({ "rawtypes" })
	@Test
	public void constructorWithRawEnclosingTypeTest() {
		ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer.Inner>() {});
	}

	@Test(expected = ReflectionException.class)
	public void overInnerConstructorWithoutSpecifying() {
		ExecutableToken.forConstructor(Inner.class.getConstructors()[0]);
	}

	@Test
	public void constructorWithSameReceiverTypeTest() {
		ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withTargetType(new TypeToken<Outer<Number>.Inner<Integer>>() {})
				.withReceiverType(new TypeToken<Outer<Number>>() {});
	}

	@Test
	public void constructorWithMoreSpecificReceiverTypeTest() {
		ExecutableToken<?, ?> constructor = ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withTargetType(new @Infer TypeToken<Outer<? super Number>.Inner<Integer>>() {})
				.withReceiverType(new TypeToken<Outer<Number>>() {});

		assertThat(
				constructor.getReturnType().substituteInstantiations(),
				equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void constructorWithLessSpecificReceiverTypeTest() {
		ExecutableToken<?, ?> constructor = ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withTargetType(new TypeToken<Outer<Number>.Inner<Integer>>() {})
				.withReceiverType(new @Infer TypeToken<Outer<? super Number>>() {});

		assertThat(
				constructor.getReturnType().substituteInstantiations(),
				equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void constructorWithSameEnclosingTargetTypeTest() {
		ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new TypeToken<Outer<Number>.Inner<Integer>>() {});
	}

	@Test
	public void constructorWithMoreSpecificEnclosingTargetTypeTest() {
		ExecutableToken<?, ?> constructor = ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new @Infer TypeToken<Outer<? super Number>>() {})
				.withTargetType(new TypeToken<Outer<Number>.Inner<Integer>>() {});

		assertThat(
				constructor.getReturnType().substituteInstantiations(),
				equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void constructorWithLessSpecificEnclosingTargetTypeTest() {
		ExecutableToken<?, ?> constructor = ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {})
				.withTargetType(new @Infer TypeToken<Outer<? super Number>.Inner<Integer>>() {});

		assertThat(
				constructor.getReturnType().substituteInstantiations(),
				equalTo(new TypeToken<Outer<Number>.Inner<Integer>>() {}));
	}

	@Test
	public void constructorWithReceiverTypeTest() {
		ExecutableToken<?, ?> constructor = ExecutableToken
				.forInnerConstructor(Inner.class.getConstructors()[0])
				.withReceiverType(new TypeToken<Outer<Number>>() {});

		assertThat(
				constructor.getReceiverType().substituteInstantiations(),
				equalTo(new TypeToken<Outer<Number>>() {}));
	}

	@Test
	public void emptyVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.forStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null);

		assertThat(list, empty());
	}

	@Test
	public void singleVarargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.forStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "");

		assertThat(list, contains(""));
	}

	@Test
	public void varargsInvocationTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.forStaticMethod(Arrays.class.getMethod("asList", Object[].class))
				.asVariableArityInvocation();

		List<?> list = (List<?>) asList.invoke(null, "A", "B", "C");

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void emptyVarargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class)
				.filter(anyMethod().named("asList"))
				.collect(resolveOverload());

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void singleVarargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class)
				.filter(anyMethod().named("asList"))
				.collect(resolveOverload(String.class));

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class)
				.filter(anyMethod().named("asList"))
				.collect(resolveOverload(String.class, String.class, String.class));

		assertThat(asList.isVariableArityInvocation(), is(true));
	}

	@Test
	public void varargsDefinitionTest() throws NoSuchMethodException, SecurityException {
		ExecutableToken<Void, ?> asList = ExecutableToken
				.forStaticMethod(Arrays.class.getMethod("asList", Object[].class));

		List<?> list = (List<?>) asList.invoke(null, new Object[] { new Object[] { "A", "B", "C" } });

		assertThat(list, contains("A", "B", "C"));
	}

	@Test
	public void varargsDefinitionResolutionTest() {
		ExecutableToken<Void, ?> asList = staticMethods(Arrays.class)
				.filter(anyMethod().named("asList"))
				.collect(resolveOverload(String[].class));

		assertThat(asList.isVariableArityInvocation(), is(false));
	}
}
