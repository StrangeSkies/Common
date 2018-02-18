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

import org.osgi.annotation.versioning.ProviderType;

import uk.co.strangeskies.mathematics.graph.GraphListeners;
import uk.co.strangeskies.observable.HotObservable;

@ProviderType
public class GraphListenersImpl<V, E> implements GraphListeners<V, E> {
	private final HotObservable<ChangeEvent<V, E>> change = new HotObservable<>();
	private final HotObservable<EdgeEvent<V, E>> edgeAdded = new HotObservable<>();
	private final HotObservable<EdgesEvent<V, E>> edgesAdded = new HotObservable<>();
	private final HotObservable<EdgeEvent<V, E>> edgeRemoved = new HotObservable<>();
	private final HotObservable<EdgesEvent<V, E>> edgesRemoved = new HotObservable<>();
	private final HotObservable<VertexEvent<V, E>> vertexAdded = new HotObservable<>();
	private final HotObservable<VerticesEvent<V, E>> verticesAdded = new HotObservable<>();
	private final HotObservable<VertexEvent<V, E>> vertexRemoved = new HotObservable<>();
	private final HotObservable<VerticesEvent<V, E>> verticesRemoved = new HotObservable<>();

	@Override
	public HotObservable<ChangeEvent<V, E>> change() {
		return change;
	}

	@Override
	public HotObservable<EdgeEvent<V, E>> edgeAdded() {
		return edgeAdded;
	}

	@Override
	public HotObservable<EdgesEvent<V, E>> edgesAdded() {
		return edgesAdded;
	}

	@Override
	public HotObservable<EdgeEvent<V, E>> edgeRemoved() {
		return edgeRemoved;
	}

	@Override
	public HotObservable<EdgesEvent<V, E>> edgesRemoved() {
		return edgesRemoved;
	}

	@Override
	public HotObservable<VertexEvent<V, E>> vertexAdded() {
		return vertexAdded;
	}

	@Override
	public HotObservable<VerticesEvent<V, E>> verticesAdded() {
		return verticesAdded;
	}

	@Override
	public HotObservable<VertexEvent<V, E>> vertexRemoved() {
		return vertexRemoved;
	}

	@Override
	public HotObservable<VerticesEvent<V, E>> verticesRemoved() {
		return verticesRemoved;
	}
}
