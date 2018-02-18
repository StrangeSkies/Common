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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ByteArrayClassLoader extends ClassLoader {
	private final Map<String, byte[]> injectedClasses = new HashMap<>();

	public ByteArrayClassLoader(ClassLoader parent) {
		super(parent);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (injectedClasses.containsKey(name)) {
			byte[] bytecode = injectedClasses.get(name);
			return defineClass(name, bytecode, 0, bytecode.length);
		} else {
			return super.findClass(name);
		}
	}

	public ByteArrayClassLoader addClass(String name, byte[] bytecode) {
		injectedClasses.put(name, bytecode);
		return this;
	}

	public ByteArrayClassLoader addClasses(Map<String, byte[]> bytecodes) {
		bytecodes.entrySet().stream().forEach(e -> addClass(e.getKey(), e.getValue()));
		return this;
	}

	public Class<?> defineClass(String name, byte[] bytecode) {
		Class<?> injectedClass = defineClass(name, bytecode, 0, bytecode.length);
		injectedClasses.put(name, bytecode);
		return injectedClass;
	}

	public ByteArrayClassLoader defineClasses(Map<String, byte[]> bytecodes) {
		bytecodes.entrySet().stream().forEach(e -> defineClass(e.getKey(), e.getValue()));
		return this;
	}

	public Stream<String> getInjectedClasses() {
		return injectedClasses.keySet().stream();
	}

	public byte[] getInjectedBytes(String name) {
		if (!injectedClasses.containsKey(name))
			throw new IllegalArgumentException();
		return injectedClasses.get(name);
	}
}
