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

import static java.util.stream.Stream.concat;
import static uk.co.strangeskies.collection.stream.StreamUtilities.streamOptional;
import static uk.co.strangeskies.reflection.codegen.ErasedMethodSignature.erasedMethodSignature;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.TypeHierarchy;
import uk.co.strangeskies.reflection.token.ExecutableParameter;
import uk.co.strangeskies.reflection.token.ExecutableToken;
import uk.co.strangeskies.reflection.token.TypeToken;

class MethodOverrides<T> {
	private final ClassSignature<T> classSignature;
	private final Map<Method, ExecutableToken<?, ?>> inheritedMethods;
	private final Map<ErasedMethodSignature, MethodOverride<T>> overrides;

	public MethodOverrides(ClassSignature<T> classSignature) {
		this.classSignature = classSignature;
		this.inheritedMethods = new HashMap<>();
		this.overrides = new HashMap<>();

		inheritMethods();
		overrideMethods();
	}

	private void inheritMethods() {
		concat(inheritInterfaceMethods(), inheritClassMethods()).filter(m -> !m.isStatic()).forEach(
				m -> {
					inheritMethod((Method) m.getMember(), m);
				});
	}

	private Stream<ExecutableToken<?, ?>> inheritInterfaceMethods() {
		return classSignature
				.getSuperInterfaces()
				.map(AnnotatedType::getType)
				.map(TypeToken::forType)
				.flatMap(TypeToken::methods);
	}

	private Stream<ExecutableToken<?, ?>> inheritClassMethods() {
		return classSignature
				.getSuperClass()
				.map(AnnotatedType::getType)
				.map(TypeHierarchy::new)
				.map(TypeHierarchy::resolveSuperClasses)
				.orElse(Stream.of(Object.class))
				.map(TypeToken::forType)
				.flatMap(TypeToken::declaredMethods);
	}

	protected void inheritMethod(Method method, ExecutableToken<?, ?> token) {
		if (inheritedMethods.put(method, token) == null) {
			ErasedMethodSignature overridingSignature = erasedMethodSignature(
					method.getName(),
					token
							.getParameters()
							.map(ExecutableParameter::getTypeToken)
							.map(TypeToken::getErasedType)
							.toArray(Class<?>[]::new));

			MethodOverride<T> override = overrides
					.computeIfAbsent(overridingSignature, k -> new MethodOverride<>(this));

			override.inherit(method);

			MethodOverride<T> mergeOverride = overrides
					.put(erasedMethodSignature(method.getName(), method.getParameterTypes()), override);
			if (mergeOverride != null && mergeOverride != override) {
				for (Method mergeMethod : mergeOverride.getMethods()) {
					override.inherit(mergeMethod);
				}
			}
		}
	}

	private void overrideMethods() {
		classSignature.getMethods().filter(s -> !s.getModifiers().isStatic()).forEach(
				this::overrideMethod);

		overrides.values().stream().forEach(MethodOverride::overrideIfNecessary);
	}

	protected ExecutableToken<?, ?> getInvocable(Method method) {
		return inheritedMethods.get(method);
	}

	protected void overrideMethod(MethodSignature<?> methodSignature) {
		MethodOverride<T> override = overrides
				.computeIfAbsent(methodSignature.erased(), k -> new MethodOverride<>(this));

		override.override(methodSignature);
	}

	public Stream<MethodSignature<?>> getSignatures() {
		return overrides.values().stream().flatMap(d -> streamOptional(d.getOverride())).distinct();
	}
}
