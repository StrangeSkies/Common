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

import java.util.Map;
import java.util.Set;

import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.GraphListeners.ChangeEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgeEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.EdgesEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VertexEvent;
import uk.co.strangeskies.mathematics.graph.GraphListeners.VerticesEvent;

class ChangeSetImpl<V, E> implements GraphListeners.ChangeSet<V, E> {
	private final Graph<V, E> graph;

	private final Set<V> verticesAdded;
	private final Set<V> verticesRemoved;
	private final Map<E, EdgeVertices<V>> edgesAdded;
	private final Map<E, EdgeVertices<V>> edgesRemoved;

	public ChangeSetImpl(Graph<V, E> graph) {
		this.graph = graph;

		verticesAdded = graph.vertices().createSet();
		verticesRemoved = graph.vertices().createSet();
		edgesAdded = graph.edges().createMap();
		edgesRemoved = graph.edges().createMap();
	}

	private ChangeSetImpl(ChangeSetImpl<V, E> changeSet) {
		graph = changeSet.graph;

		verticesAdded = graph.vertices().createSet(changeSet.verticesAdded);
		verticesRemoved = graph.vertices().createSet(changeSet.verticesRemoved);
		edgesAdded = graph.edges().createMap(changeSet.edgesAdded);
		edgesRemoved = graph.edges().createMap(changeSet.edgesRemoved);
	}

	public ChangeSetImpl<V, E> copy() {
		return new ChangeSetImpl<>(this);
	}

	@Override
	public Set<V> verticesAdded() {
		return verticesAdded;
	}

	@Override
	public Set<V> verticesRemoved() {
		return verticesRemoved;
	}

	@Override
	public Map<E, EdgeVertices<V>> edgesAdded() {
		return edgesAdded;
	}

	@Override
	public Map<E, EdgeVertices<V>> edgesRemoved() {
		return edgesRemoved;
	}

	public void tryTriggerListeners(GraphListenersImpl<V, E> listeners) {
		/*
		 * Vertices added and removed:
		 */
		for (V vertex : verticesAdded())
			listeners.vertexAdded().next(VertexEvent.over(graph, vertex));

		for (V vertex : verticesRemoved())
			listeners.vertexRemoved().next(VertexEvent.over(graph, vertex));

		listeners.verticesAdded().next(VerticesEvent.over(graph, graph.vertices().createSet(verticesAdded())));

		listeners.verticesRemoved().next(VerticesEvent.over(graph, graph.vertices().createSet(verticesRemoved())));

		/*
		 * Edges added and removed:
		 */
		for (E edge : edgesAdded().keySet())
			listeners.edgeAdded().next(EdgeEvent.over(graph, edge, edgesAdded().get(edge)));

		for (E edge : edgesRemoved().keySet())
			listeners.edgeRemoved().next(EdgeEvent.over(graph, edge, edgesRemoved().get(edge)));

		listeners.edgesAdded().next(EdgesEvent.over(graph, graph.edges().createMap(edgesAdded())));

		listeners.edgesRemoved().next(EdgesEvent.over(graph, graph.edges().createMap(edgesRemoved())));

		/*
		 * Change sets:
		 */
		listeners.change().next(ChangeEvent.over(graph, this));
	}
}
