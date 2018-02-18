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
package uk.co.strangeskies.mathematics.graph.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.collection.EquivalenceComparator;
import uk.co.strangeskies.collection.SetDecorator;
import uk.co.strangeskies.collection.multimap.MultiHashMap;
import uk.co.strangeskies.collection.multimap.MultiMap;
import uk.co.strangeskies.collection.multimap.MultiTreeMap;
import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.GraphListeners.ChangeEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgeEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgesEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VertexEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VerticesEvent;
import uk.co.strangeskies.property.IdentityProperty;
import uk.co.strangeskies.mathematics.graph.GraphTransformer;

class GraphImpl<V, E> implements Graph<V, E> {
	private class VerticesImpl implements SetDecorator<V>, Vertices<V, E> {
		@Override
		public Set<V> getComponent() {
			return adjacencyMatrix.keySet();
		}

		@Override
		public boolean add(V e) {
			return addVertex(e);
		}

		private boolean addAllImpl(Collection<? extends V> c) {
			boolean changed = false;
			for (V v : c)
				changed = add(v) || changed;
			return changed;
		}

		@Override
		public boolean addAll(Collection<? extends V> c) {
			IdentityProperty<Boolean> added = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<V> vertexSet = vertices.createSet();
				vertexSet.addAll(c);
				vertexSet.removeAll(this);

				added.set(vertices.addAllImpl(vertexSet));

				internalListeners.verticesAdded().next(VerticesEvent.over(GraphImpl.this, vertexSet));
			});
			return added.get();
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean remove(Object vertex) {
			boolean removed = adjacencyMatrix.containsKey(vertex);

			if (removed) {
				atomicInternal(() -> {
					adjacencyMatrix.remove(vertex).values().forEach(s -> s.forEach(e -> edges.remove(e)));

					adjacencyMatrix.values().forEach(toMap -> {
						for (E edge : toMap.remove(vertex))
							edges.remove(edge);
					});

					changeSet.verticesRemoved().add((V) vertex);
					changeSet.verticesAdded().remove(vertex);

					internalListeners.vertexRemoved().next(VertexEvent.over(GraphImpl.this, (V) vertex));
				});
			}

			return removed;
		}

		private boolean removeAllImpl(Collection<?> c) {
			boolean changed = false;
			for (Object v : c)
				changed = remove(v) || changed;
			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			IdentityProperty<Boolean> removed = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<V> vertexSet = vertices.createSet();
				vertexSet.addAll(this);
				vertexSet.retainAll(c);

				removed.set(vertices.removeAllImpl(vertexSet));

				internalListeners.verticesRemoved().next(VerticesEvent.over(GraphImpl.this, vertexSet));

			});
			return removed.get();
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			Set<V> remove = vertices.createSet();
			remove.addAll(this);
			remove.removeAll(c);

			return removeAll(remove);
		}

		@Override
		public void clear() {
			SetDecorator.super.clear();
			edges.clear();
		}

		@Override
		public Set<V> adjacentTo(V vertex) {
			Set<V> adjacent = vertices.createSet();
			adjacent.addAll(adjacencyMatrix.get(vertex).keySet());
			return adjacent;
		}

		@Override
		public Set<V> successorsOf(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return adjacentTo(vertex);
		}

		@Override
		public Set<V> predecessorsOf(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getTo);
			} else
				return adjacentTo(vertex);
		}

		private Set<V> adjacentToDirectional(V vertex, Function<EdgeVertices<V>, V> edgeToMatch) {
			return adjacencyMatrix.get(vertex).entrySet().stream()
					.filter(a -> a.getValue().stream()
							.filter(e -> vertices().equality().test(edgeToMatch.apply(incidentTo(e)), vertex)).findAny().isPresent())
					.map(Map.Entry::getKey).collect(Collectors.toCollection(vertices()::createSet));
		}

		@Override
		public BiPredicate<? super V, ? super V> equality() {
			return vertexComparator == null ? Objects::equals : vertexComparator;
		}

		@Override
		public Stream<V> stream() {
			return SetDecorator.super.stream();
		}

		@Override
		public EdgeVertices<V> incidentTo(E vertex) {
			return edges.edgeVertices.get(vertex);
		}

		@Override
		public V incidentToHead(E vertex) {
			return edges.edgeVertices.get(vertex).getTo();
		}

		@Override
		public V incidentToTail(E vertex) {
			return edges.edgeVertices.get(vertex).getFrom();
		}

		@Override
		public String toString() {
			return getComponent().toString();
		}

		@Override
		public int hashCode() {
			return getComponent().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return getComponent().equals(obj);
		}

		@Override
		public Set<V> createSet() {
			return vertexComparator != null ? new TreeSet<>(new EquivalenceComparator<V>(vertexComparator)) : new HashSet<>();
		}

		@Override
		public Set<V> createSet(Collection<? extends V> vertices) {
			if (vertexComparator != null) {
				Set<V> vertexSet = new TreeSet<>(new EquivalenceComparator<V>(vertexComparator));
				vertexSet.addAll(vertices);
				return vertexSet;
			} else
				return new HashSet<>(vertices);
		}

		@Override
		public <T> Map<V, T> createMap() {
			return vertexComparator != null ? new TreeMap<>(new EquivalenceComparator<V>(vertexComparator)) : new HashMap<>();
		}

		@Override
		public <T> Map<V, T> createMap(Map<? extends V, ? extends T> edges) {
			if (vertexComparator != null) {
				Map<V, T> edgeMap = new TreeMap<>(new EquivalenceComparator<V>(vertexComparator));
				edgeMap.putAll(edges);
				return edgeMap;
			} else
				return new HashMap<>(edges);
		}
	}

	private class EdgesImpl implements SetDecorator<E>, Edges<V, E> {
		private final Map<E, EdgeVertices<V>> edgeVertices;

		public EdgesImpl() {
			this(edgeComparator != null ? new TreeMap<>(new EquivalenceComparator<E>(edgeComparator)) : new HashMap<>());
		}

		private EdgesImpl(Map<E, EdgeVertices<V>> edgeVertices) {
			this.edgeVertices = edgeVertices;
		}

		@Override
		public Set<E> getComponent() {
			return edgeVertices.keySet();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object edge) {
			boolean removed = SetDecorator.super.remove(edge);

			if (removed) {
				atomicInternal(() -> {
					EdgeVertices<V> vertices = edges.edgeVertices.get(edge);

					adjacencyMatrix.get(vertices.getFrom()).get(vertices.getTo()).remove(edge);

					if (!changeSet.edgesRemoved().containsKey(edge))
						changeSet.edgesRemoved().put((E) edge, vertices);
					changeSet.edgesAdded().remove(edge);

					internalListeners.edgeRemoved().next(EdgeEvent.over(GraphImpl.this, (E) edge, vertices));
				});
			}

			return removed;
		}

		@Override
		public E add(EdgeVertices<V> vertices) {
			return addInferredEdge(vertices);
		}

		private boolean addAllImpl(Collection<? extends E> c) {
			boolean changed = false;

			for (E edge : c)
				changed = add(edge) | changed;

			return changed;
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			IdentityProperty<Boolean> added = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<E> edgeSet = edges().createSet();
				edgeSet.addAll(c);
				edgeSet.removeAll(this);

				added.set(edges.addAllImpl(edgeSet));

				Map<E, EdgeVertices<V>> edges = edgeSet.stream()
						.collect(Collectors.toMap(Function.identity(), vertices::incidentTo));

				internalListeners.edgesAdded().next(EdgesEvent.over(GraphImpl.this, edges));
			});
			return added.get();
		}

		private boolean removeAllImpl(Collection<?> c) {
			boolean changed = false;

			for (Object edge : c)
				changed = remove(edge) | changed;

			return changed;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			IdentityProperty<Boolean> removed = new IdentityProperty<>();
			atomicInternal(() -> {
				Set<E> edgeSet = edges().createSet();
				edgeSet.addAll(this);
				edgeSet.retainAll(c);

				removed.set(edges.removeAllImpl(edgeSet));

				Map<E, EdgeVertices<V>> edges = edgeSet.stream()
						.collect(Collectors.toMap(Function.identity(), vertices::incidentTo));

				internalListeners.edgesRemoved().next(EdgesEvent.over(GraphImpl.this, edges));
			});
			return removed.get();
		}

		@Override
		public Set<E> incidentTo(V vertex) {
			return adjacencyMatrix.get(vertex).values().stream().flatMap(Set::stream)
					.collect(Collectors.toCollection(edges::createSet));
		}

		@Override
		public Set<E> incidentToHead(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getTo);
			} else
				return incidentTo(vertex);
		}

		@Override
		public Set<E> incidentToTail(V vertex) {
			if (isDirected()) {
				return adjacentToDirectional(vertex, EdgeVertices::getFrom);
			} else
				return incidentTo(vertex);
		}

		private Set<E> adjacentToDirectional(V vertex, Function<EdgeVertices<V>, V> edgeToMatch) {
			return adjacencyMatrix.get(vertex).entrySet().stream()
					.filter(a -> a.getValue().stream().filter(e -> edgeToMatch.apply(vertices.incidentTo(e)) == vertex).findAny()
							.isPresent())
					.map(Map.Entry::getValue).flatMap(Set::stream).collect(Collectors.toCollection(edges::createSet));
		}

		@Override
		public Set<E> between(EdgeVertices<V> vertices) {
			MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(vertices.getFrom());
			return adjacencyMap == null ? null : adjacencyMap.get(vertices.getTo());
		}

		@Override
		public E betweenUnique(EdgeVertices<V> vertices) {
			MultiMap<V, E, Set<E>> adjacencyMap = adjacencyMatrix.get(vertices.getFrom());
			return adjacencyMap == null ? null : adjacencyMap.get(vertices.getTo()).iterator().next();
		}

		@Override
		public double weight(E edge) {
			return edgeWeight.apply(edge);
		}

		@Override
		public BiPredicate<? super E, ? super E> equality() {
			return edgeComparator == null ? Objects::equals : edgeComparator;
		}

		private void put(E edge, EdgeVertices<V> vertices) {
			edgeVertices.put(edge, vertices);
		}

		@Override
		public Set<EdgeVertices<V>> edgeVertices() {
			return stream().map(vertices()::incidentTo).collect(Collectors.toSet());
		}

		@Override
		public String toString() {
			return getComponent().toString();
		}

		@Override
		public int hashCode() {
			return getComponent().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return getComponent().equals(obj);
		}

		@Override
		public Set<E> createSet() {
			return edgeComparator != null ? new TreeSet<>(new EquivalenceComparator<E>(edgeComparator)) : new HashSet<>();
		}

		@Override
		public Set<E> createSet(Collection<? extends E> edges) {
			if (edgeComparator != null) {
				Set<E> edgeSet = new TreeSet<>(new EquivalenceComparator<E>(edgeComparator));
				edgeSet.addAll(edges);
				return edgeSet;
			} else
				return new HashSet<>(edges);
		}

		@Override
		public <T> Map<E, T> createMap() {
			return edgeComparator != null ? new TreeMap<>(new EquivalenceComparator<E>(edgeComparator)) : new HashMap<>();
		}

		@Override
		public <T> Map<E, T> createMap(Map<? extends E, ? extends T> edges) {
			if (edgeComparator != null) {
				Map<E, T> edgeMap = new TreeMap<>(new EquivalenceComparator<E>(edgeComparator));
				edgeMap.putAll(edges);
				return edgeMap;
			} else
				return new HashMap<>(edges);
		}
	}

	private final BiPredicate<? super V, ? super V> vertexComparator;
	private final BiPredicate<? super E, ? super E> edgeComparator;

	private final VerticesImpl vertices;
	private final EdgesImpl edges;

	private final Map<V, MultiMap<V, E, Set<E>>> adjacencyMatrix;

	private final Function<EdgeVertices<V>, E> edgeFactory;

	/*
	 * Only one of these may be present, but we can't fold them both into the
	 * function form, as lowToHighDirection may be applied before the edge object
	 * is created.
	 */
	private final Function<E, Comparator<V>> lowToHighDirectionFunction;
	private final Comparator<V> lowToHighDirection;

	private final boolean simple;
	private final boolean weighted;
	private final Function<E, Double> edgeWeight;

	private final GraphListenersImpl<V, E> internalListeners;
	private final GraphListenersImpl<V, E> listeners;

	private ChangeSetImpl<V, E> changeSet;

	public GraphImpl(GraphConfiguratorImpl<V, E> configurator) {
		listeners = new GraphListenersImpl<>();
		internalListeners = configurator.getInternalListeners();

		lowToHighDirection = configurator.getLowToHighDirection();
		lowToHighDirectionFunction = configurator.getLowToHighDirectionFunction();
		simple = !configurator.isMultigraph();
		weighted = configurator.getEdgeWeight() != null;
		edgeWeight = weighted ? configurator.getEdgeWeight() : e -> 1d;

		vertexComparator = configurator.getVertexEquality();
		edgeComparator = configurator.getEdgeEquality();

		vertices = new VerticesImpl();
		edges = new EdgesImpl();

		adjacencyMatrix = vertices().createMap();

		edgeFactory = configurator.getEdgeFactory();

		/*
		 * Add initial vertices and edges
		 */

		if (configurator.getVertices() != null)
			for (V vertex : configurator.getVertices())
				addVertex(vertex);

		if (configurator.getEdgeVertices() != null)
			for (EdgeVertices<V> edge : configurator.getEdgeVertices())
				addInferredEdge(edge);

		if (configurator.getEdgeMap() != null)
			for (Map.Entry<E, EdgeVertices<V>> edge : configurator.getEdgeMap().entrySet())
				addEdge(edge.getKey(), edge.getValue());
	}

	private E addInferredEdge(EdgeVertices<V> edgeVertices) {
		if (edgeFactory != null) {
			if (simple && adjacencyMatrix.containsKey(edgeVertices.getFrom())
					&& adjacencyMatrix.get(edgeVertices.getFrom()).containsKey(edgeVertices.getTo())) {
				return null;
			}

			if (lowToHighDirection != null) {
				edgeVertices = EdgeVertices.between(edgeVertices.getFrom(), edgeVertices.getTo(), lowToHighDirection);
			}

			E edge;

			if (edgeVertices != null) {
				edge = edgeFactory.apply(edgeVertices);

				addEdge(edge, edgeVertices, true);
			} else {
				edge = null;
			}

			return edge;
		} else {
			throw new UnsupportedOperationException("Edge instance cannot be inferred, and must be explicitly provided");
		}
	}

	private boolean addEdge(E edge, EdgeVertices<V> edgeVertices) {
		return addEdge(edge, edgeVertices, false);
	}

	private boolean addEdge(E edge, EdgeVertices<V> edgeVertices, boolean ordered) {
		if (simple && adjacencyMatrix.containsKey(edgeVertices.getFrom())
				&& adjacencyMatrix.get(edgeVertices.getFrom()).containsKey(edgeVertices.getTo())) {
			return false;
		} else if (!adjacencyMatrix.containsKey(edgeVertices.getFrom())) {
			throw new IllegalArgumentException("Cannot create edge from vertex '" + edgeVertices.getFrom()
					+ "' as it is not a member of the graph '" + adjacencyMatrix.keySet() + "'");
		} else if (!adjacencyMatrix.containsKey(edgeVertices.getTo())) {
			throw new IllegalArgumentException("Cannot create edge to vertex '" + edgeVertices.getTo()
					+ "' as it is not a member of the graph '" + adjacencyMatrix.keySet() + "'");
		}

		EdgeVertices<V> vertices;
		if (lowToHighDirectionFunction != null) {
			vertices = EdgeVertices.between(edgeVertices.getFrom(), edgeVertices.getTo(),
					lowToHighDirectionFunction.apply(edge));
		} else if (!ordered && lowToHighDirection != null) {
			vertices = EdgeVertices.between(edgeVertices.getFrom(), edgeVertices.getTo(), lowToHighDirection);
		} else {
			vertices = edgeVertices;
		}

		boolean added = vertices != null;

		if (added) {
			atomicInternal(() -> {
				adjacencyMatrix.get(vertices.getFrom()).add(vertices.getTo(), edge);
				adjacencyMatrix.get(vertices.getTo()).add(vertices.getFrom(), edge);
				edges.put(edge, vertices);

				changeSet.edgesRemoved().remove(edge, vertices);
				changeSet.edgesAdded().put(edge, vertices);

				internalListeners.edgeAdded().next(EdgeEvent.over(GraphImpl.this, edge, vertices));
			});
		}

		return added;
	}

	private boolean addVertex(V vertex) {
		if (!adjacencyMatrix.containsKey(vertex)) {
			MultiMap<V, E, Set<E>> map;
			if (vertexComparator != null) {
				map = new MultiTreeMap<>(new EquivalenceComparator<V>(vertexComparator), edges::createSet);
			} else {
				map = new MultiHashMap<>(edges::createSet);
			}

			atomicInternal(() -> {
				adjacencyMatrix.put(vertex, map);

				changeSet.verticesRemoved().remove(vertex);
				changeSet.verticesAdded().add(vertex);

				internalListeners.vertexAdded().next(VertexEvent.over(GraphImpl.this, vertex));
			});

			return true;
		}
		return false;
	}

	@Override
	public Graph<V, E> copy() {
		return new GraphBuilderImpl().build().readOnly().vertices(vertices).edges(edges.edgeVertices)
				.edgeFactory(v -> edges.betweenUnique(v)).create();
	}

	@Override
	public Vertices<V, E> vertices() {
		return vertices;
	}

	@Override
	public Edges<V, E> edges() {
		return edges;
	}

	@Override
	public boolean isDirected() {
		return lowToHighDirection != null || lowToHighDirectionFunction != null;
	}

	@Override
	public boolean isWeighted() {
		return weighted;
	}

	@Override
	public boolean isSimple() {
		return simple;
	}

	@Override
	public GraphTransformer<V, E> transform() {
		return new GraphTransformerImpl<>(this);
	}

	@Override
	public GraphListeners<V, E> listeners() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void atomic(Consumer<? super Graph<V, E>> action) {
		atomicInternal(() -> action.accept(this));
	}

	private void atomicInternal(Runnable action) {
		if (changeSet == null) {
			changeSet = new ChangeSetImpl<>(this);

			try {
				action.run();

				listeners.change().next(ChangeEvent.over(GraphImpl.this, changeSet.copy()));
			} catch (Exception e) {
				/*
				 * TODO discard change set
				 */

				changeSet = null;

				throw e;
			}

			changeSet.tryTriggerListeners(listeners);

			changeSet = null;
		} else {
			action.run();
		}
	}

	@Override
	public String toString() {
		return adjacencyMatrix.toString();
	}
}
