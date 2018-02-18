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

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.collection.EquivalenceComparator;
import uk.co.strangeskies.mathematics.graph.EdgeVertices;
import uk.co.strangeskies.mathematics.graph.Graph;
import uk.co.strangeskies.mathematics.graph.GraphConfigurator;
import uk.co.strangeskies.mathematics.graph.GraphException;
import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.mathematics.graph.GraphProperties;

public class GraphConfiguratorImpl<V, E> implements GraphConfigurator<V, E> {
	private final GraphProperties text;

	private List<V> vertices;
	private boolean unmodifiableVertices;
	private BiPredicate<? super V, ? super V> vertexEquality;

	private List<EdgeVertices<V>> edgeVertices;
	private TreeMap<E, EdgeVertices<V>> edgeMap;
	private boolean unmodifiableEdges;
	private BiPredicate<? super E, ? super E> edgeEquality;

	private boolean acyclic;
	private boolean multigraph;

	private Comparator<V> lowToHighDirection;
	private Function<E, Comparator<V>> lowToHighDirectionFunction;

	private Function<EdgeVertices<V>, E> edgeFactory;
	private Function<EdgeVertices<V>, ? extends Set<? extends E>> edgeMultiFactory;
	private Function<E, Double> edgeWeight;
	private final GraphListenersImpl<V, E> internalListeners;

	public GraphConfiguratorImpl() {
		internalListeners = new GraphListenersImpl<>();
		text = getDefaultProperties(GraphProperties.class);
	}

	private void assertConfigurable(Object reference, Function<GraphProperties, Object> property) {
		if (reference != null)
			throw new GraphException(text.alreadyConfigured(property));
	}

	protected static GraphConfigurator<Object, Object> configure() {
		return new GraphConfiguratorImpl<>();
	}

	@Override
	public Graph<V, E> create() {
		return new GraphImpl<>(this);
	}

	@Override
	public GraphConfigurator<V, E> readOnly() {
		return readOnlyVertices().readOnlyEdges();
	}

	@Override
	public GraphConfigurator<V, E> readOnlyVertices() {
		unmodifiableVertices = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> readOnlyEdges() {
		unmodifiableEdges = true;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <W extends V> GraphConfigurator<W, E> vertices(Collection<W> vertices) {
		if (this.vertices == null)
			this.vertices = new ArrayList<>(vertices);
		else
			this.vertices.addAll(vertices);

		return (GraphConfigurator<W, E>) this;
	}

	@Override
	public GraphConfigurator<V, E> edges(Collection<? extends EdgeVertices<V>> edges) {
		assertConfigurable(edgeMap, GraphProperties::edgeMap);

		if (edgeVertices == null)
			edgeVertices = new ArrayList<>(edges);
		else
			edgeVertices.addAll(edges);

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edges(Map<F, EdgeVertices<V>> edges) {
		assertConfigurable(edgeVertices, GraphProperties::edgeVertices);

		if (edgeMap == null)
			edgeMap = new TreeMap<>(EquivalenceComparator.identityComparator());

		edgeMap.putAll(edges);

		return (GraphConfigurator<V, F>) this;
	}

	@Override
	public <F extends E> GraphConfigurator<V, F> edgeType() {
		return edges(Collections.emptyMap());
	}

	@Override
	public GraphConfigurator<V, E> edge(V from, V to) {
		if (edgeVertices == null)
			edges(Collections.emptySet());
		edgeVertices.add(EdgeVertices.between(from, to));
		return this;
	}

	@Override
	public GraphConfigurator<V, E> edge(E edge, V from, V to) {
		edgeMap.put(edge, EdgeVertices.between(from, to));
		return this;
	}

	@Override
	public GraphConfigurator<V, E> acyclic() {
		acyclic = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> multigraph() {
		multigraph = true;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> direction(Comparator<V> lowToHigh) {
		assertConfigurable(lowToHighDirection, GraphProperties::direction);
		assertConfigurable(lowToHighDirectionFunction, GraphProperties::directionFunction);

		lowToHighDirection = lowToHigh;

		return this;
	}

	@Override
	public GraphConfigurator<V, E> direction(Function<E, Comparator<V>> lowToHigh) {
		assertConfigurable(lowToHighDirection, GraphProperties::direction);
		assertConfigurable(lowToHighDirectionFunction, GraphProperties::directionFunction);

		lowToHighDirectionFunction = lowToHigh;

		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edgeFactory(Function<EdgeVertices<V>, F> factory) {
		assertConfigurable(edgeFactory, GraphProperties::edgeFactory);
		assertConfigurable(edgeMultiFactory, GraphProperties::edgeMultiFactory);

		edgeFactory = (Function<EdgeVertices<V>, E>) factory;
		return (GraphConfigurator<V, F>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <F extends E> GraphConfigurator<V, F> edgeMultiFactory(
			Function<EdgeVertices<V>, Set<F>> factory) {
		assertConfigurable(edgeFactory, GraphProperties::edgeFactory);
		assertConfigurable(edgeMultiFactory, GraphProperties::edgeMultiFactory);

		edgeMultiFactory = (Function<EdgeVertices<V>, Set<E>>) (Object) factory;
		return (GraphConfigurator<V, F>) multigraph();
	}

	@Override
	public GraphConfigurator<V, E> edgeWeight(Function<E, Double> weight) {
		assertConfigurable(edgeWeight, GraphProperties::edgeWeight);

		edgeWeight = weight;

		return this;
	}

	@Override
	public GraphConfigurator<V, E> vertexEquality(BiPredicate<? super V, ? super V> equality) {
		assertConfigurable(edgeWeight, GraphProperties::vertexEquality);

		this.vertexEquality = equality;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> edgeEquality(BiPredicate<? super E, ? super E> equality) {
		assertConfigurable(edgeEquality, GraphProperties::edgeEquality);

		this.edgeEquality = equality;
		return this;
	}

	@Override
	public GraphConfigurator<V, E> internalListeners(
			Consumer<GraphListeners<V, E>> internalListeners) {
		internalListeners.accept(this.internalListeners);
		return this;
	}

	boolean isMultigraph() {
		return multigraph;
	}

	Comparator<V> getLowToHighDirection() {
		return lowToHighDirection;
	}

	Function<E, Comparator<V>> getLowToHighDirectionFunction() {
		return lowToHighDirectionFunction;
	}

	Function<E, Double> getEdgeWeight() {
		return edgeWeight;
	}

	BiPredicate<? super V, ? super V> getVertexEquality() {
		return vertexEquality;
	}

	BiPredicate<? super E, ? super E> getEdgeEquality() {
		return edgeEquality;
	}

	Function<EdgeVertices<V>, E> getEdgeFactory() {
		return edgeFactory;
	}

	Collection<V> getVertices() {
		return vertices;
	}

	Map<E, EdgeVertices<V>> getEdgeMap() {
		return edgeMap;
	}

	Collection<EdgeVertices<V>> getEdgeVertices() {
		return edgeVertices;
	}

	GraphListenersImpl<V, E> getInternalListeners() {
		return internalListeners;
	}
}
