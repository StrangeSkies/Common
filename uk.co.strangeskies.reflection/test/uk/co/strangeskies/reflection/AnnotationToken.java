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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * A helper class to help reflect over type annotations. Instances should
 * describe annotations present on them as a string.
 * 
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
public class AnnotationToken {
  private String stringRepresentation;
  private Set<Package> packages;

  protected AnnotationToken(String stringRepresentation) {
    this(stringRepresentation, new Class<?>[0]);
  }

  protected AnnotationToken(String stringRepresentation, Class<?>... importedClassPackages) {
    this.stringRepresentation = stringRepresentation;

    // Make sure current classloader is aware of all annotation packages
    getAnnotations();

    packages = new HashSet<>();
    for (Class<?> importedPackage : importedClassPackages)
      packages.add(importedPackage.getPackage());
  }

  public String getStringRepresentation() {
    return stringRepresentation;
  }

  public Annotation[] getAnnotations() {
    return getClass().getAnnotatedSuperclass().getAnnotations();
  }

  public Set<Package> getPackages() {
    return new HashSet<>(packages);
  }
}
