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
package uk.co.strangeskies.mathematics.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.mathematics.graph.impl.GraphBuilderImpl;
import uk.co.strangeskies.utility.Copyable;

/**
 * @author Elias N Vasylenko
 * @param <V>
 *          The type of vertices in the graph
 * @param <E>
 *          The type of edges in the graph
 */
@ProviderType
public interface Graph<V, E> extends Copyable<Graph<V, E>> {
	static GraphConfigurator<Object, Object> build() {
		return new GraphBuilderImpl().build();
	}

	@ProviderType
	public interface Vertices<V, E> extends Set<V> {
		BiPredicate<? super V, ? super V> equality();

		Set<V> adjacentTo(V vertex);

		EdgeVertices<V> incidentTo(E edge);

		V incidentToHead(E edge);

		V incidentToTail(E edge);

		Set<V> successorsOf(V vertex);

		Set<V> predecessorsOf(V vertex);

		Set<V> createSet();

		Set<V> createSet(Collection<? extends V> vertices);

		<T> Map<V, T> createMap();

		<T> Map<V, T> createMap(Map<? extends V, ? extends T> edges);
	}

	@ProviderType
	public interface Edges<V, E> extends Set<E> {
		BiPredicate<? super E, ? super E> equality();

		double weight(E edge);

		Set<E> incidentTo(V vertex);

		Set<E> incidentToHead(V vertex);

		Set<E> incidentToTail(V vertex);

		default Set<E> between(V from, V to) {
			return between(EdgeVertices.between(from, to));
		}

		Set<E> between(EdgeVertices<V> vertices);

		/**
		 * If there is exactly one edge between the provided vertices, this edge is
		 * returned. If there is no edge between the provided vertices, null is
		 * returned. If there are multiple edges between them, an exception is
		 * thrown.
		 *
		 * @param from
		 *          The tail vertex of the edge
		 * @param to
		 *          The head vertex of the edge
		 * @return The edge between the given head and tail, or null if no such edge
		 *         exists.
		 */
		default E betweenUnique(V from, V to) {
			return betweenUnique(EdgeVertices.between(from, to));
		}

		E betweenUnique(EdgeVertices<V> vertices);

		default E add(V from, V to) {
			return add(EdgeVertices.between(from, to));
		}

		E add(EdgeVertices<V> edge);

		default boolean add(@SuppressWarnings("unchecked") EdgeVertices<V>... edges) {
			return add(Arrays.asList(edges));
		}

		default boolean add(Collection<? extends EdgeVertices<V>> edgeVertices) {
			boolean changed = false;
			for (EdgeVertices<V> edge : edgeVertices)
				changed = add(edge) != null | changed;
			return changed;
		}

		Set<EdgeVertices<V>> edgeVertices();

		Set<E> createSet();

		Set<E> createSet(Collection<? extends E> edges);

		<T> Map<E, T> createMap();

		<T> Map<E, T> createMap(Map<? extends E, ? extends T> edges);
	}

	/**
	 * @return An unmodifiable set of the vertices in this graph.
	 */
	Vertices<V, E> vertices();

	/**
	 * @return An unmodifiable set of the edges in this graph.
	 */
	Edges<V, E> edges();

	boolean isDirected();

	boolean isWeighted();

	boolean isSimple();

	GraphTransformer<V, E> transform();

	GraphListeners<V, E> listeners();

	/**
	 * Perform an operation on the receiving graph atomically.
	 * 
	 * @param action
	 *          The action to perform
	 */
	void atomic(Consumer<? super Graph<V, E>> action);
}
