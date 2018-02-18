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

import static uk.co.strangeskies.reflection.ArrayTypes.arrayFromComponent;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.TypeVariables.typeVariableExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardExtending;
import static uk.co.strangeskies.reflection.WildcardTypes.wildcardSuper;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.reflection.TypeSubstitution;

@SuppressWarnings("javadoc")
public class TypeSubstitutionTest {
	private Type createTestType(Class<?> clazz) {
		return parameterize(
				Map.class,
				wildcardExtending(arrayFromComponent(arrayFromComponent(Number.class))),
				parameterize(Map.class, wildcardSuper(clazz), typeVariableExtending(Collection.class, "E")));
	}

	@Test
	public <T extends Number> void noSubstitutionIdentityTest() {
		Type type = createTestType(Number.class);

		Type substitution = new TypeSubstitution().where(t -> false, t -> t).resolve(type);

		Assert.assertTrue(type == substitution);
	}

	@Test
	public <T extends Number> void doublyNestedWildcardSubstitutionTest() {
		Type type = createTestType(Number.class);

		Type expected = createTestType(Serializable.class);

		Type substitution = new TypeSubstitution().where(Number.class, Serializable.class).resolve(type);

		Assert.assertEquals(expected, substitution);
	}
}
