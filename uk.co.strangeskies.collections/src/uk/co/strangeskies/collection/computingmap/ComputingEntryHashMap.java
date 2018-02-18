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
 * This file is part of uk.co.strangeskies.collections.
 *
 * uk.co.strangeskies.collections is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.computingmap;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComputingEntryHashMap<K, V> implements ComputingMap<K, V> {
	protected interface Entry<K, V> {
		K getKey();

		V getValue();

		void remove();
	}

	protected class DeferredEntry implements Entry<K, V> {
		private final K key;
		private V value;

		public DeferredEntry(K key) {
			this.key = key;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public synchronized V getValue() {
			return value != null ? value : (value = computation().apply(getKey()));
		}

		@Override
		public void remove() {}
	}

	protected class ComputingEntry extends DeferredEntry {
		public ComputingEntry(K key) {
			super(key);
			executor.execute(this::getValue);
		}
	}

	private final Map<K, Entry<K, V>> map;
	private final Function<K, V> computation;
	private final Executor executor;

	protected ComputingEntryHashMap(Function<K, V> computation, Executor executor) {
		this.map = new HashMap<>();
		this.computation = computation;
		this.executor = executor;
	}

	protected ComputingEntryHashMap(ComputingEntryHashMap<K, V> other) {
		this.map = new HashMap<>(other.map);
		this.computation = other.computation;
		this.executor = other.executor;
	}

	@Override
	public V get(K key) {
		Entry<?, V> entry = map.get(key);

		return entry == null ? null : entry.getValue();
	}

	@Override
	public boolean put(K key) {
		if (map.containsKey(key))
			return false;

		Entry<K, V> entry = createEntry(key);
		map.put(key, entry);

		return true;
	}

	public V putGetImpl(K key) {
		Entry<K, V> entry = map.get(key);

		if (entry == null) {
			entry = createEntry(key);
			map.put(key, entry);
		}

		return entry.getValue();
	}

	@Override
	public V putGet(K key, Consumer<V> wasPresent, Consumer<V> wasMissing) {
		V value = get(key);

		if (value == null) {
			value = putGetImpl(key);
			wasMissing.accept(value);
		} else {
			wasPresent.accept(value);
		}

		return value;
	}

	protected Entry<K, V> createEntry(K key) {
		return new ComputingEntry(key);
	}

	protected Function<K, V> computation() {
		return computation;
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				Iterator<K> iterator = map.keySet().iterator();

				return new Iterator<K>() {
					private K last;

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public K next() {
						return last = iterator.next();
					}

					@Override
					public void remove() {
						ComputingEntryHashMap.this.remove(last);
					}
				};
			}

			@Override
			public int size() {
				return map.values().size();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				Iterator<Entry<K, V>> iterator = map.values().iterator();

				return new Iterator<V>() {
					private Entry<K, V> last;

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public V next() {
						return (last = iterator.next()).getValue();
					}

					@Override
					public void remove() {
						ComputingEntryHashMap.this.remove(last.getKey());
					}
				};
			}

			@Override
			public int size() {
				return map.values().size();
			}
		};
	}

	@Override
	public V removeGet(K key) {
		Entry<K, V> entry = map.remove(key);
		V value = entry.getValue();
		entry.remove();
		return value;
	}

	@Override
	public boolean clear() {
		if (!map.isEmpty())
			return false;
		map.clear();
		return true;
	}
}
