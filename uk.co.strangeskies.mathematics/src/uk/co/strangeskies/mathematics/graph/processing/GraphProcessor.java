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
package uk.co.strangeskies.mathematics.graph.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import uk.co.strangeskies.mathematics.graph.Graph;

public class GraphProcessor<V, E> implements Iterable<V> {
	private final Graph<V, E> graph;
	private Consumer<? super V> process;

	private Collection<? extends V> initial;
	private boolean parallel;

	public class Process {
		private final Set<V> processed;
		private final Set<V> ready;

		public Process() {
			processed = graph.vertices().createSet();

			if (initial == null) {
				ready = graph.vertices().stream().filter(v -> graph.vertices().predecessorsOf(v).isEmpty())
						.collect(Collectors.toCollection(graph.vertices()::createSet));
			} else {
				ready = graph.vertices().createSet(initial);
			}
		}

		public Set<V> processedVertices() {
			Set<V> processedCopy = graph.vertices().createSet();

			synchronized (processed) {
				processedCopy.addAll(processed);
			}

			return processedCopy;
		}

		public Set<V> preparedVertices() {
			Set<V> readyCopy = graph.vertices().createSet();

			synchronized (processed) {
				readyCopy.addAll(ready);
			}

			return readyCopy;
		}

		public Set<V> process(V nextVertex) {
			synchronized (processed) {
				if (!ready.remove(nextVertex))
					throw new IllegalStateException();
			}

			process.accept(nextVertex);

			Set<V> readied = graph.vertices().createSet();

			synchronized (processed) {
				processed.add(nextVertex);

				graph.vertices().successorsOf(nextVertex).stream()
						.filter(v -> processed.containsAll(graph.vertices().predecessorsOf(v))).forEach(readied::add);
				readied.removeAll(ready);

				ready.addAll(readied);
			}

			return readied;
		}

		protected Iterator<V> iterator() {
			return new Iterator<V>() {
				@Override
				public boolean hasNext() {
					return !ready.isEmpty();
				}

				@Override
				public V next() {
					V processed;

					synchronized (Process.this.processed) {
						processed = ready.iterator().next();
						process(processed);
					}

					return processed;
				}
			};
		}
	}

	protected GraphProcessor(Graph<V, E> graph) {
		this.graph = graph;
		process = v -> {};
		initial = null;
		parallel = false;
	}

	public static <V, E> GraphProcessor<V, E> over(Graph<V, E> graph) {
		return new GraphProcessor<>(graph);
	}

	public GraphProcessor<V, E> from(Collection<? extends V> initial) {
		this.initial = initial;
		return this;
	}

	public GraphProcessor<V, E> process(Consumer<? super V> process) {
		this.process = process;
		return this;
	}

	public GraphProcessor<V, E> parallel() {
		this.parallel = true;
		return this;
	}

	public List<V> processEager() {
		List<V> processedList = new ArrayList<>();

		for (V processed : this)
			processedList.add(processed);

		return processedList;
	}

	public List<V> processEagerParallel() {
		// TODO actually parallelize this...
		return processEager();
	}

	@Override
	public Iterator<V> iterator() {
		return new Process().iterator();
	}
}
