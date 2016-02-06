/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * Utilities for safely running code under a different thread context class
 * loader.
 * 
 * @author Elias N Vasylenko
 */
public class ContextClassLoaderRunner {
	private static final String PROTOCOL_HANDLER = "java.protocol.handler.pkgs";

	private final ClassLoader classLoader;

	public ContextClassLoaderRunner(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public ContextClassLoaderRunner(URL... jars) {
		this(new URLClassLoader(jars, Thread.currentThread().getContextClassLoader()));
	}

	public ContextClassLoaderRunner(Collection<URL> jars) {
		this(jars.toArray(new URL[jars.size()]));
	}

	/**
	 * Invoke a {@link Runnable} under this runners classloader, making sure the
	 * current context class loader is reinstated upon termination.
	 * 
	 * @param runnable
	 *          The runnable to be invoked under the given classloader
	 */
	public <T> T run(Supplier<T> runnable) {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(classLoader);

		try {
			return runnable.get();
		} finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	public void run(Runnable runnable) {
		run(() -> {
			runnable.run();
			return null;
		});
	}

	public void runLater(Runnable runnable) {
		new Thread(() -> run(runnable)).start();
	}
}
