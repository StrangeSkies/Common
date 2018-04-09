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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.lang.annotation.Annotation;

import org.junit.Assume;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import uk.co.strangeskies.reflection.annotations.ClassProperty;
import uk.co.strangeskies.reflection.annotations.Plain;
import uk.co.strangeskies.reflection.annotations.StringProperty;

@SuppressWarnings("javadoc")
@RunWith(Theories.class)
public class AnnotationsTest {
  @DataPoint
  public static AnnotationToken PLAIN = new @Plain AnnotationToken(
      "@uk.co.strangeskies.reflection.annotations.Plain") {};

  @DataPoint
  public static AnnotationToken PLAIN_IMPORTED = new @Plain AnnotationToken(
      "@Plain",
      Plain.class) {};

  @DataPoint
  public static AnnotationToken CLASS_PROPERTY = new @ClassProperty(
      property = Object.class) AnnotationToken(
          "@uk.co.strangeskies.reflection.annotations.ClassProperty(property = java.lang.Object.class)") {};

  @DataPoint
  public static AnnotationToken CLASS_PROPERTY_IMPORTED = new @ClassProperty(
      property = Object.class) AnnotationToken(
          "@ClassProperty(property = Object.class)",
          ClassProperty.class,
          Object.class) {};

  @DataPoint
  public static AnnotationToken STRING_PROPERTY = new @StringProperty(
      property = "string") AnnotationToken(
          "@uk.co.strangeskies.reflection.annotations.StringProperty(property = \"string\")") {};

  @DataPoint
  public static AnnotationToken STRING_PROPERTY_IMPORTED = new @StringProperty(
      property = "string") AnnotationToken(
          "@StringProperty(property = \"string\")",
          StringProperty.class,
          Object.class) {};

  @DataPoint
  public static AnnotationToken STRING_PROPERTY_SYMBOLS1 = new @StringProperty(
      property = "\n") AnnotationToken(
          "@StringProperty(property = \"\\n\")",
          StringProperty.class,
          Object.class) {};

  @DataPoint
  public static AnnotationToken STRING_PROPERTY_SYMBOLS2 = new @StringProperty(
      property = "\"<>()[]{}\"") AnnotationToken(
          "@StringProperty(property = \"\\\"<>()[]{}\\\"\")",
          StringProperty.class,
          Object.class) {};

  @Theory
  public void toStringWithoutPackageImport(AnnotationToken token) {
    Assume.assumeTrue("Assuming no package imports", token.getPackages().isEmpty());
    Annotation annotation = assumeSingleAnnotation(token);

    assertThat(Annotations.toString(annotation)).isEqualTo(token.getStringRepresentation());
  }

  @Theory
  public void toStringWithPackageImport(AnnotationToken token) {
    assumeThat(token.getPackages()).describedAs("package imports").isNotEmpty();

    Annotation annotation = assumeSingleAnnotation(token);

    assertThat(
        Annotations.toString(annotation, Imports.empty().withPackageImports(token.getPackages())))
            .isEqualTo(token.getStringRepresentation());
  }

  private Annotation assumeSingleAnnotation(AnnotationToken token) {
    assumeThat(token.getAnnotations().length).describedAs("number of annotations").isEqualTo(1);
    return token.getAnnotations()[0];
  }
}
