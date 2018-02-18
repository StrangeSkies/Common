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

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * An base type for any source declaration objects which are annotated.
 * Implementations should be immutable.
 * 
 * @author Elias N Vasylenko
 *
 * @param <S>
 *          a self-bound over the type of the implementing class
 */
public interface AnnotatedSignature<S extends AnnotatedSignature<S>>
		extends Signature<S> {
	/**
	 * @return the annotations on this declaration
	 */
	Stream<? extends Annotation> getAnnotations();

	/**
	 * Derive a version of this declaration with the given annotations. Users
	 * should take care not to specify annotations which could not be applied to
	 * the declaration due to their {@link Target}, as there is no validation of
	 * applicability.
	 * <p>
	 * 
	 * Annotations already present on the receiving declaration will be replaced
	 * rather than appended.
	 * 
	 * @param annotations
	 *          the annotations with which to annotate this declaration
	 * @return a new declaration of the same type, and with the same content, but
	 *         with the given annotations
	 */
	default S annotated(Annotation... annotations) {
		return annotated(asList(annotations));
	}

	/**
	 * Derive a version of this declaration with the given annotations. Users
	 * should take care not to specify annotations which could not be applied to
	 * the declaration due to their {@link Target}, as there is no validation of
	 * applicability.
	 * <p>
	 * 
	 * Annotations already present on the receiving declaration will be replaced
	 * rather than appended.
	 * 
	 * @param annotations
	 *          the annotations with which to annotate this declaration
	 * @return a new declaration of the same type, and with the same content, but
	 *         with the given annotations
	 */
	S annotated(Collection<? extends Annotation> annotations);
}
