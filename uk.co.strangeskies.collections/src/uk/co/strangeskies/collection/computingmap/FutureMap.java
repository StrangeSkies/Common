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
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.co.strangeskies.collection.EquivalenceComparator;

public class FutureMap<K, V> implements ComputingMap<K, V> {
	public class PreparationThread extends Thread {
		private final K key;
		private V value;
		private boolean cancellable = true;
		private RuntimeException exception;

		public PreparationThread(K key) {
			synchronized (FutureMap.this) {
				this.key = key;
				preparationThreads.put(key, this);
				start();
			}
		}

		@Override
		public void run() {
			try {
				value = mapping.apply(key);
			} catch (RuntimeException e) {
				exception = e;
			} finally {
				synchronized (FutureMap.this) {
					if (preparationThreads.containsKey(key)) {
						if (value != null) {
							values.put(key, value);
						}
						preparationThreads.remove(key);
						FutureMap.this.notifyAll();
					}
				}
			}
		}

		public void setUncancellable() {
			synchronized (FutureMap.this) {
				cancellable = false;
			}
		}

		public void cancel() {
			synchronized (FutureMap.this) {
				if (cancellable && preparationThreads.containsKey(key)) {
					preparationThreads.remove(key);
					FutureMap.this.notifyAll();
					interrupt();
				}
			}
		}

		public V waitForValue() {
			synchronized (FutureMap.this) {
				while (preparationThreads.containsKey(key)) {
					try {
						preparationThreads.wait();
					} catch (InterruptedException e) {}
				}
				return getValue();
			}
		}

		public V getValue() {
			if (exception != null) {
				throw exception;
			}
			return value;
		}
	}

	private final Map<K, PreparationThread> preparationThreads;
	private final Map<K, V> values;
	private final Function<K, V> mapping;

	public FutureMap(Function<K, V> function) {
		this(function, EquivalenceComparator.identityComparator());
	}

	public FutureMap(Function<K, V> function, Comparator<K> comparator) {
		preparationThreads = new TreeMap<>(comparator);
		values = new TreeMap<>(comparator);
		mapping = values::get;
	}

	private boolean isPending(K key) {
		synchronized (this) {
			return preparationThreads.containsKey(key) || values.containsKey(key);
		}
	}

	@Override
	public boolean put(final K key) {
		synchronized (this) {
			if (isPending(key)) {
				return false;
			} else {
				new PreparationThread(key);
				return true;
			}
		}
	}

	@Override
	public V get(K key) {
		return get(key, true);
	}

	public V get(K key, boolean cancellable) {
		synchronized (this) {
			if (preparationThreads.containsKey(key)) {
				if (!cancellable) {
					preparationThreads.get(key).setUncancellable();
				}
				return preparationThreads.get(key).waitForValue();
			} else if (values.containsKey(key)) {
				return values.get(key);
			} else {
				return null;
			}
		}
	}

	@Override
	public V putGet(K key, Consumer<V> wasPresent, Consumer<V> wasMissing) {
		synchronized (this) {
			boolean added = put(key);
			V value = get(key, false);

			if (added) {
				wasMissing.accept(value);
			} else {
				wasPresent.accept(value);
			}

			return value;
		}
	}

	@Override
	public Set<K> keySet() {
		return new AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				Iterator<K> baseIterator = preparationThreads.keySet().iterator();

				return new Iterator<K>() {
					private K last;

					@Override
					public boolean hasNext() {
						return baseIterator.hasNext();
					}

					@Override
					public K next() {
						return last = baseIterator.next();
					}

					@Override
					public void remove() {
						FutureMap.this.remove(last);
					}
				};
			}

			@Override
			public int size() {
				return preparationThreads.keySet().size();
			}
		};
	}

	@Override
	public Collection<V> values() {
		return new AbstractCollection<V>() {
			Set<K> baseSet = keySet();

			@Override
			public Iterator<V> iterator() {
				Iterator<K> baseIterator = baseSet.iterator();

				return new Iterator<V>() {
					private K last;

					@Override
					public boolean hasNext() {
						return baseIterator.hasNext();
					}

					@Override
					public V next() {
						return get(last = baseIterator.next());
					}

					@Override
					public void remove() {
						FutureMap.this.remove(last);
					}
				};
			}

			@Override
			public int size() {
				return baseSet.size();
			}
		};
	}

	@Override
	public boolean remove(K key) {
		synchronized (this) {
			if (preparationThreads.containsKey(key)) {
				preparationThreads.get(key).cancel();
				preparationThreads.remove(key);
				notifyAll();
				return true;
			} else if (values.containsKey(key)) {
				values.remove(key);
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public V removeGet(K key) {
		return removeGet(key, true);
	}

	public V removeGet(K key, boolean cancellable) {
		synchronized (this) {
			if (preparationThreads.containsKey(key)) {
				if (!cancellable) {
					preparationThreads.get(key).setUncancellable();
					V value = preparationThreads.remove(key).waitForValue();
					notifyAll();
					return value;
				} else {
					preparationThreads.get(key).cancel();
					return null;
				}
			} else if (values.containsKey(key)) {
				return values.remove(key);
			} else {
				return null;
			}
		}
	}

	@Override
	public boolean clear() {
		boolean changed = false;

		synchronized (this) {
			for (K key : preparationThreads.keySet()) {
				changed = remove(key) || changed;
			}
			for (K key : values.keySet()) {
				changed = remove(key) || changed;
			}
		}

		return changed;
	}

	public void waitForAll() {
		synchronized (this) {
			while (!preparationThreads.isEmpty()) {
				preparationThreads.values().stream().findAny().get().waitForValue();
			}
		}
	}

	public synchronized Set<K> getKeys() {
		return preparationThreads.keySet();
	}
}
