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
 * This file is part of uk.co.strangeskies.reflection.resource.
 *
 * uk.co.strangeskies.reflection.resource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.resource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.resource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

public class Jar {
	private static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";

	private final FileSystem fileSystem;

	protected Jar(URI jarLocation) {
		try {
			if (new File(jarLocation).isFile() && !jarLocation.toString().startsWith("jar:")) {
				jarLocation = new URI("jar:" + jarLocation);
			}
			FileSystem fileSystem;
			try {
				fileSystem = FileSystems.getFileSystem(jarLocation);
			} catch (Exception e) {
				fileSystem = FileSystems.newFileSystem(jarLocation, Collections.emptyMap());
			}
			this.fileSystem = fileSystem;
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException("Creating file system for jar " + jarLocation + " failed", e);
		}
	}

	public static Jar getContainingJar(Class<?> clazz) {
		return getJar(getContainingJarLocation(clazz));
	}

	public static Jar getJar(URI location) {
		return new Jar(location);
	}

	public static URI getContainingJarLocation(Class<?> clazz) {
		try {
			return clazz.getProtectionDomain().getCodeSource().getLocation().toURI();
		} catch (Exception e) {
			throw new RuntimeException("Loading MANIFEST for class " + clazz + " failed", e);
		}
	}

	public FileSystem getFileSystem() {
		return fileSystem;
	}

	public Path getRootPath() {
		return fileSystem.getRootDirectories().iterator().next();
	}

	public Path getPackagePath(Package jarPackage) {
		return getFileSystem().getPath(jarPackage.getName().replace('.', '/'));
	}

	public JarManifest getManifest() {
		return new JarManifest(fileSystem.getPath(MANIFEST_PATH));
	}

	public JarManifest getManifest(ManifestAttributeParser parser) {
		return new JarManifest(fileSystem.getPath(MANIFEST_PATH), parser);
	}
}
