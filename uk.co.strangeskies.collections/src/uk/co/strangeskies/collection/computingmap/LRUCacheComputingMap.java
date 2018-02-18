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

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class LRUCacheComputingMap<K, V> extends CacheComputingMap<K, V> {
	protected class LinkedEntry extends ReferenceEntry {
		private LinkedEntry previous;
		private LinkedEntry next;

		public LinkedEntry() {
			previous = this;
			next = this;
		}

		public LinkedEntry(K key) {
			super(key);

			insert();
		}

		@Override
		public V getValue() {
			uninsert();
			insert();

			return super.getValue();
		}

		private void insert() {
			previous = bounds;
			next = bounds.next;

			next.previous = previous.next = this;
		}

		private void uninsert() {
			previous.next = next;
			next.previous = previous;
		}

		@Override
		public void remove() {
			uninsert();
			super.remove();
		}
	}

	private final int maximumSize;
	private final LinkedEntry bounds;

	public LRUCacheComputingMap(Function<K, V> computation, int maximumSize, boolean softReferences) {
		super(computation, softReferences);

		this.maximumSize = maximumSize;
		this.bounds = new LinkedEntry();
	}

	protected LRUCacheComputingMap(LRUCacheComputingMap<K, V> other) {
		super(other);

		maximumSize = other.maximumSize;
		bounds = other.bounds;
	}

	@Override
	protected synchronized Entry<K, V> createEntry(K key) {
		return new LinkedEntry(key);
	}

	public synchronized int cacheSize() {
		return maximumSize;
	}

	@Override
	public synchronized V get(K key) {
		return super.get(key);
	}

	@Override
	public synchronized boolean put(K key) {
		boolean added = super.put(key);

		if (size() > maximumSize)
			remove(bounds.previous.getKey());

		return added;
	}

	@Override
	public synchronized boolean putAll(Collection<? extends K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = super.put(key) | changed;

		while (size() > maximumSize)
			remove(bounds.previous.getKey());

		return changed;
	}

	@Override
	public synchronized V putGet(K key, Consumer<V> wasPresent, Consumer<V> wasMissing) {
		V value = super.putGet(key, wasPresent, wasMissing);

		if (size() > maximumSize)
			remove(bounds.previous.getKey());

		return value;
	}

	public synchronized boolean removeAll(Set<K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = remove(key) | changed;
		return changed;
	}

	@Override
	public synchronized boolean clear() {
		bounds.previous = bounds.next = bounds;
		return super.clear();
	}

	@Override
	public synchronized boolean isEmpty() {
		return bounds.previous == bounds;
	}
}
