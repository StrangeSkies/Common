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
package uk.co.strangeskies.collection.multimap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * TODO system for returning proxy collections that automatically add/remove the
 * entry from the map when they become non-empty/empty.
 * 
 * @author Elias N Vasylenko
 *
 * @param <K>
 *          key type
 * @param <V>
 *          element value type
 * @param <C>
 *          element collection type
 */
public interface MultiMap<K, V, C extends Collection<V>> extends Map<K, C> {
	public C createCollection();

	public default C getCollection(K key) {
		C values = get(key);

		if (values == null) {
			values = createCollection();
			put(key, values);
		}

		return values;
	}

	public default boolean add(K key, V value) {
		return getCollection(key).add(value);
	}

	public default boolean addAll(K key, Collection<? extends V> values) {
		return getCollection(key).addAll(values);
	}

	public default boolean addAll(Map<? extends K, ? extends V> values) {
		boolean added = false;

		for (Map.Entry<? extends K, ? extends V> entry : values.entrySet()) {
			added = add(entry.getKey(), entry.getValue()) || added;
		}

		return added;
	}

	public default boolean addAll(MultiMap<? extends K, ? extends V, ?> values) {
		boolean added = false;

		for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry : values.entrySet()) {
			added = addAll(entry.getKey(), entry.getValue()) || added;
		}

		return added;
	}

	public default boolean addAll(K key, @SuppressWarnings("unchecked") V... values) {
		return addAll(key, Arrays.asList(values));
	}

	public default boolean addToAll(V value) {
		return addToAll(keySet(), value);
	}

	public default boolean addAllToAll(Collection<? extends V> values) {
		return addAllToAll(keySet(), values);
	}

	public default boolean addAllToAll(@SuppressWarnings("unchecked") V... values) {
		return addAllToAll(Arrays.asList(values));
	}

	public default boolean addToAll(Collection<? extends K> keys, V value) {
		boolean added = false;

		for (K key : keys) {
			added = add(key, value) || added;
		}

		return added;
	}

	public default boolean addAllToAll(Collection<? extends K> keys, Collection<? extends V> values) {
		boolean added = false;

		for (K key : keys) {
			added = addAll(key, values) || added;
		}

		return added;
	}

	public default boolean removeValue(K key, V value) {
		C values = get(key);

		boolean removed = values != null && values.remove(value);

		if (removed && values.isEmpty()) {
			remove(key);
		}

		return removed;
	}

	public default boolean removeAll(K key, Collection<? extends V> values) {
		C currentValues = get(key);

		boolean removed = currentValues != null && currentValues.removeAll(values);

		if (removed && currentValues.isEmpty()) {
			remove(key);
		}

		return removed;
	}

	public default boolean removeAll(K key, @SuppressWarnings("unchecked") V... values) {
		return addAll(key, Arrays.asList(values));
	}

	public default boolean removeAll(Map<K, V> values) {
		boolean removed = false;

		for (Map.Entry<K, V> entry : values.entrySet()) {
			removed = remove(entry.getKey(), entry.getValue()) || removed;
		}

		return removed;
	}

	public default boolean removeAll(MultiMap<K, V, ?> values) {
		boolean removed = false;

		for (Map.Entry<K, ? extends Collection<V>> entry : values.entrySet()) {
			removed = removeAll(entry.getKey(), entry.getValue()) || removed;
		}

		return removed;
	}

	public default boolean removeFromAll(V value) {
		return removeFromAll(keySet(), value);
	}

	public default boolean removeAllFromAll(Collection<? extends V> values) {
		return removeAllFromAll(keySet(), values);
	}

	public default boolean removeAllFromAll(@SuppressWarnings("unchecked") V... values) {
		return removeAllFromAll(Arrays.asList(values));
	}

	public default boolean removeFromAll(Collection<? extends K> keys, V value) {
		boolean removed = false;

		for (K key : new HashSet<>(keys))
			removed = removeValue(key, value) || removed;

		return removed;
	}

	public default boolean removeAllFromAll(Collection<? extends K> keys, Collection<? extends V> values) {
		boolean removed = false;

		for (K key : keys) {
			removed = removeAll(key, values) || removed;
		}

		return removed;
	}

	public default boolean contains(K key, V value) {
		C values = get(key);
		return values != null && values.contains(value);
	}

	public default C getAll() {
		C allValues = createCollection();

		for (C values : values()) {
			allValues.addAll(values);
		}

		return allValues;
	}

	public default C getAll(Collection<? extends K> keys) {
		C allValues = createCollection();

		for (K key : keys) {
			C values = get(key);
			if (values != null) {
				allValues.addAll(values);
			}
		}

		return allValues;
	}

	public default C getAll(@SuppressWarnings("unchecked") K... keys) {
		return getAll(Arrays.asList(keys));
	}
}
