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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.Manifest;

/**
 * This class provides reflective access to a manifest.
 * 
 * @author Elias N Vasylenko
 */
public class JarManifest {
	private static final ManifestAttributeParser DEFAULT_PARSER = new ManifestAttributeParser();

	private final Manifest manifest;
	private final ManifestAttributeParser parser;

	public JarManifest(Path path) {
		this(getInputStream(path));
	}

	public JarManifest(InputStream inputStream) {
		this(inputStream, DEFAULT_PARSER);
	}

	public JarManifest(Path path, ManifestAttributeParser parser) {
		this(getInputStream(path), parser);
	}

	public JarManifest(InputStream inputStream, ManifestAttributeParser parser) {
		try {
			manifest = new Manifest(inputStream);
		} catch (IOException e) {
			/*
			 * TODO proper exceptions...
			 */
			throw new RuntimeException();
		}
		this.parser = parser;
	}

	private static InputStream getInputStream(Path path) {
		try {
			return Files.newInputStream(path);
		} catch (IOException e) {
			/*
			 * TODO proper exceptions...
			 */
			throw new RuntimeException();
		}
	}

	public String parseValueString(String valueString) {
		return parser.parseValueString(valueString);
	}

	public AttributeProperty<?> parseAttributeProperty(String attribute) {
		return parser.parseAttributeProperty(attribute);
	}

	public Attribute parseAttribute(String entry) {
		return parser.parseAttribute(entry);
	}

	public List<Attribute> parseAttributes(String entry) {
		return parser.parseAttributes(entry);
	}

	public Manifest getBackingManifest() {
		return manifest;
	}
}
