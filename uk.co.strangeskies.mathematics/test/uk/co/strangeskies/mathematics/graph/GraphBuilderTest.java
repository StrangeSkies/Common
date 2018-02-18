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
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/*
' * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.mathematics.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.graph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.collection.EquivalenceComparator;
import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphBuilder;
import uk.co.strangeskies.mathematics.graph.GraphConfigurator;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.impl.GraphBuilderImpl;

public class GraphBuilderTest {
	private final Supplier<GraphBuilder> graphBuilderSupplier;

	public GraphBuilderTest() {
		graphBuilderSupplier = GraphBuilderImpl::new;
	}

	/*
	 * public GraphBuilderTest(Supplier<GraphBuilder> graphBuilderSupplier) {
	 * this.graphBuilderSupplier = graphBuilderSupplier; }
	 */

	protected GraphConfigurator<Object, Object> graph() {
		return graphBuilderSupplier.get().build();
	}

	@SafeVarargs
	protected final <T> Set<T> set(T... items) {
		return new LinkedHashSet<>(Arrays.asList(items));
	}

	protected <T> Set<T> set(BiPredicate<? super T, ? super T> equality, @SuppressWarnings("unchecked") T... items) {
		Set<T> set = new TreeSet<>(new EquivalenceComparator<T>(equality));
		set.addAll(Arrays.asList(items));
		return set;
	}

	@Test
	public void buildTest() {
		graph().create();
	}

	@Test
	public void buildVerticesTest() {
		Set<String> vertices = set("one", "two", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(set(), graph.edges());
	}

	@Test
	public void buildVerticesExclusionTest() {
		List<String> vertices = Arrays.asList("one", "two", "three", "three");

		Graph<String, Object> graph = graph().vertices(vertices).create();

		Assert.assertEquals(new HashSet<>(vertices), graph.vertices());
		Assert.assertEquals(set(), graph.edges());
	}

	@Test
	public void buildEdgesTest() {
		Set<String> vertices = set("one", "two", "three");
		Set<EdgeVertices<String>> edges = set(EdgeVertices.between("one", "two"), EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices).edgeFactory(Object::new).edges(edges).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().edgeVertices());
	}

	@Test
	public void buildAutoEdgesTest() {
		Set<String> vertices = set("one", "two", "three");
		Set<EdgeVertices<String>> edges = set(EdgeVertices.between("one", "two"));

		Graph<String, Object> graph = graph().vertices(vertices).edges(edges).edgeFactory(Object::new)
				.internalListeners(l -> l.vertexAdded().observe(e -> {
					for (String vertex : e.graph().vertices())
						if (vertex != e.vertex() && vertex.length() == e.vertex().length())
							e.graph().edges().add(vertex, e.vertex());
				})).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().edgeVertices());
	}

	@Test
	public void buildUnmodifiableTest() {
		Set<String> vertices = set("one", "two", "three");
		Set<EdgeVertices<String>> edges = set(EdgeVertices.between("one", "two"), EdgeVertices.between("two", "three"));

		Graph<String, Object> graph = graph().vertices(vertices).edges(edges).edgeFactory(Object::new).readOnly().create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(edges, graph.edges().edgeVertices());
	}

	@Test
	public void buildComparatorDirectedTest() {
		Set<String> vertices = set("one", "two", "three");

		Graph<String, Object> graph = graph().vertices(vertices).internalListeners(l -> l.vertexAdded().observe(e -> {
			for (String vertex : e.graph().vertices())
				if (vertex != e.vertex())
					e.graph().edges().add(vertex, e.vertex());
		})).edgeFactory(Object::new).direction(String::compareTo).create();

		Assert.assertEquals(vertices, graph.vertices());

		Assert.assertEquals(set("two", "three"), graph.vertices().successorsOf("one"));
		Assert.assertEquals(set(), graph.vertices().predecessorsOf("one"));

		Assert.assertEquals(set("two"), graph.vertices().successorsOf("three"));
		Assert.assertEquals(set("one"), graph.vertices().predecessorsOf("three"));

		Assert.assertEquals(set(), graph.vertices().successorsOf("two"));
		Assert.assertEquals(set("one", "three"), graph.vertices().predecessorsOf("two"));
	}

	@Test
	public void buildInsertionDirectedTest() {
		Set<String> vertices = set("one", "two", "three");

		Graph<String, Object> graph = graph().<String> vertices().internalListeners(l -> l.vertexAdded().observe(e -> {
			for (String vertex : e.graph().vertices())
				if (vertex != e.vertex())
					e.graph().edges().add(vertex, e.vertex());
		})).edgeFactory(Object::new).directed().create();

		vertices.stream().forEach(graph.vertices()::add);

		Assert.assertEquals(vertices, graph.vertices());

		Assert.assertEquals(set("two", "three"), graph.vertices().successorsOf("one"));
		Assert.assertEquals(set(), graph.vertices().predecessorsOf("one"));

		Assert.assertEquals(set(), graph.vertices().successorsOf("three"));
		Assert.assertEquals(set("one", "two"), graph.vertices().predecessorsOf("three"));

		Assert.assertEquals(set("three"), graph.vertices().successorsOf("two"));
		Assert.assertEquals(set("one"), graph.vertices().predecessorsOf("two"));
	}

	@Test
	public void buildWithComparatorTest() {
		String one = "one";
		String two = "two";
		String three = new String(one);
		Set<String> vertices = new TreeSet<>(EquivalenceComparator.identityComparator());
		vertices.add(one);
		vertices.add(two);
		vertices.add(three);

		Graph<String, Object> graph = graph().vertices(vertices).vertexEquality((a, b) -> a == b)
				.addInternalListener(GraphListeners::vertexAdded, e -> {
					for (String vertex : e.graph().vertices()) {
						if (vertex != e.vertex())
							e.graph().edges().add(e.vertex(), vertex);
					}
				}).edgeFactory(Object::new).create();

		Assert.assertEquals(vertices, graph.vertices());
		Assert.assertEquals(
				set(EdgeVertices.between(one, two), EdgeVertices.between(two, three), EdgeVertices.between(three, one)),
				graph.edges().edgeVertices());
	}

	@Test
	public void buildWithEdgeFactoryTest() {
		Set<String> vertices = set("one", "two", "three");

		Graph<String, String> graph = graph().vertices(vertices).addInternalListener(GraphListeners::vertexAdded, e -> {
			for (String vertex : e.graph().vertices())
				if (e.vertex() != vertex)
					e.graph().edges().add(e.vertex(), vertex);
		}).direction(String::compareTo).edgeFactory(v -> v.getFrom() + " ->> " + v.getTo()).create();

		Assert.assertEquals(set("one ->> two", "three ->> two", "one ->> three"), graph.edges());

	}

	// TODO edge vertices from edge object
	// TODO arbitrary edge object and edge vertices combinations
	// TODO simple graph fail with multiple edges between same point
	// TODO generate neighbors test
	// TODO incoming/outgoing generation
	// TODO incoming/outgoing generation with neighbor generation
	// TODO multigraph variants where appropriate
}
