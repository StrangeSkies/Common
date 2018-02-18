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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterize;
import static uk.co.strangeskies.reflection.ParameterizedTypes.parameterizeUnchecked;
import static uk.co.strangeskies.reflection.codegen.TypeVariableSignature.referenceTypeVariable;
import static uk.co.strangeskies.reflection.codegen.TypeVariableSignature.typeVariableSignature;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.signature.SignatureWriter;

import uk.co.strangeskies.reflection.ReflectionException;
import uk.co.strangeskies.reflection.codegen.ParameterizedDeclaration;
import uk.co.strangeskies.reflection.codegen.ParameterizedSignature;
import uk.co.strangeskies.reflection.codegen.TypeVariableSignature;
import uk.co.strangeskies.utility.Self;

@SuppressWarnings("javadoc")
public class GenericSignatureTest {
	static class ParameterizedSignatureImpl
			implements ParameterizedSignature<ParameterizedSignatureImpl> {
		private final Set<Annotation> annotations;
		private final List<TypeVariableSignature> typeVariables;

		public ParameterizedSignatureImpl() {
			this(emptySet(), emptyList());
		}

		private ParameterizedSignatureImpl(
				Set<Annotation> annotations,
				List<TypeVariableSignature> typeVariables) {
			this.annotations = annotations;
			this.typeVariables = typeVariables;
		}

		@Override
		public Stream<? extends Annotation> getAnnotations() {
			return annotations.stream();
		}

		@Override
		public ParameterizedSignatureImpl annotated(Collection<? extends Annotation> annotations) {
			return new ParameterizedSignatureImpl(new HashSet<>(annotations), typeVariables);
		}

		@Override
		public Stream<? extends TypeVariableSignature> getTypeVariables() {
			return typeVariables.stream();
		}

		@Override
		public ParameterizedSignatureImpl typeVariables(
				Collection<? extends TypeVariableSignature> typeVariables) {
			return new ParameterizedSignatureImpl(annotations, new ArrayList<>(typeVariables));
		}
	}

	@Test
	public void noParametersSignatureTest() {
		SignatureWriter writer = new SignatureWriter();
		new ParameterizedDeclaration<>(new ParameterizedSignatureImpl(), writer);
		writer.visitSuperclass();

		Assert.assertEquals("", writer.toString());
	}

	@Test
	public void unboundedParameterSignatureTest() {
		ParameterizedSignatureImpl signature = new ParameterizedSignatureImpl().typeVariables("A");

		SignatureWriter writer = new SignatureWriter();
		new ParameterizedDeclaration<>(signature, writer);
		writer.visitSuperclass();

		Assert.assertEquals("<A:Ljava/lang/Object;>", writer.toString());
	}

	@Test
	public void parameterNamesTest() {
		ParameterizedSignatureImpl signature = new ParameterizedSignatureImpl()
				.typeVariables("A", "B", "C");

		SignatureWriter writer = new SignatureWriter();
		new ParameterizedDeclaration<>(signature, writer);
		writer.visitSuperclass();

		Assert.assertEquals(
				"<A:Ljava/lang/Object;B:Ljava/lang/Object;C:Ljava/lang/Object;>",
				writer.toString());
	}

	@Test
	public void selfBoundingTypeVariableTest() {
		ParameterizedSignatureImpl signature = new ParameterizedSignatureImpl().typeVariables(
				typeVariableSignature("A")
						.withBounds(parameterizeUnchecked(Self.class, referenceTypeVariable("A"))));

		SignatureWriter writer = new SignatureWriter();
		new ParameterizedDeclaration<>(signature, writer);
		writer.visitSuperclass();

		Assert.assertEquals("<A::Luk/co/strangeskies/utility/Self<TA;>;>", writer.toString());
	}

	@Test(expected = ReflectionException.class)
	public void invalidBoundsTest() {
		ParameterizedSignatureImpl signature = new ParameterizedSignatureImpl().typeVariables(
				typeVariableSignature("A").withBounds(
						parameterize(Set.class, String.class),
						parameterize(Set.class, Number.class)));

		new ParameterizedDeclaration<>(signature, new SignatureWriter());
	}
}
