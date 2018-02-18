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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * A capture of a type variable, with all of the reflective functionality
 * provided by {@link TypeToken}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type variable we wish to capture.
 */
public class TypeParameter<T> extends TypeToken<T> {
	/**
	 * Capture the type variable provided as an argument to the type parameter of
	 * this constructor. This should only ever be parameterized with an
	 * uninstantiated type variable.
	 */
	protected TypeParameter() {
		if (!(super.getType() instanceof TypeVariable))
			throw new IllegalArgumentException();
	}

	private TypeParameter(TypeVariable<?> type) {
		super(type);
	}

	@Override
	public TypeVariable<?> getType() {
		return (TypeVariable<?>) super.getType();
	}

	/**
	 * Capture the given type variable in a TypeToken.
	 * 
	 * @param type
	 *          The type variable to capture.
	 * @return A type token instance over the given type.
	 */
	public static TypeParameter<?> forTypeVariable(TypeVariable<?> type) {
		return new TypeParameter<>(type);
	}

	public TypeArgument<T> asType(TypeToken<T> type) {
		return new TypeArgument<T>(this, type) {};
	}

	public TypeArgument<T> asClass(Class<T> type) {
		return new TypeArgument<T>(this, forClass(type)) {};
	}

	@SuppressWarnings("unchecked")
	public TypeArgument<?> asType(Type type) {
		return new TypeArgument<T>(this, (TypeToken<T>) TypeToken.forType(type)) {};
	}
}
