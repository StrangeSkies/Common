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

/**
 * Conceptually, this map behaves much like an ordinary map other than the
 * semantics of putting a key value pair into the map. A ComputingMap is
 * associated with a particular computation - which effectively is equivalent to
 * a function from type {@code K} to type {@code V}. This means that we may
 * simply add a key to the map and have the associated value be calculated and
 * added automatically.
 *
 * This interface may form the basis of a caching system, for example.
 *
 * @author Elias N Vasylenko
 *
 * @param <V>
 *          The type of values
 * @param <K>
 *          The type of keys
 */
public interface ComputingMap<K, V> extends ReadOnlyMap<K, V> {
	/**
	 * For the given key, this method should return the value which has been
	 * computed.
	 *
	 * This interface, by contract, provides no guarantees that the value for a
	 * previously entered key will still be contained within the map. Despite
	 * this, for keys which have not yet been entered into the map, an invocation
	 * of this method should simply return null, rather than attempting to compute
	 * the given value and then return it.
	 *
	 * Implementations are, of course, free to provide this guarantee.
	 *
	 * @param key
	 *          The key object for which to return the mapped computed value
	 * @return The computed value associated with a given key
	 */
	@Override
	V get(K key);

	/**
	 * Enters the key into the map such that a value of type {@code V} will at
	 * some point be computed which will be then returned by any subsequent calls
	 * to {@link ComputingMap#get}.
	 *
	 * Generally keys should be immutable, and the computation associated with the
	 * map will be expected to produce identical results each time, so in the case
	 * that the key has already been added to the map most implementations should
	 * just return instantly and do no work. Because of this it also makes little
	 * sense to return the previous value for a key entered more than once.
	 *
	 * @param key
	 *          The key object to be mapped to a new value
	 * @return True if the associated value was not already contained in the map,
	 *         false otherwise
	 */
	boolean put(K key);

	default boolean putAll(Collection<? extends K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = put(key) | changed;
		return changed;
	}

	/**
	 * This method simply makes sure the value for the given key has been computed
	 * and added to the map, then returns it.
	 *
	 * @param key
	 *          The key object to be mapped to a new value
	 * @return The computed value associated with a given key.
	 */
	default V putGet(K key) {
		return putGet(key, v -> {}, v -> {});
	}

	V putGet(K key, Consumer<V> wasPresent, Consumer<V> wasMissing);

	@Override
	Set<K> keySet();

	Collection<V> values();

	/**
	 * Remove the given key and it's associated computed value from the map.
	 * <p>
	 * Use of this method may result in worse performance that
	 * {@link #remove(Object)}, as some implementations may cancel value
	 * computation for keys removed by the latter.
	 *
	 * @param key
	 *          The key to remove.
	 * @return The value removed.
	 */
	V removeGet(K key);

	/**
	 * Remove the given key and it's associated computed value from the map.
	 *
	 * @param key
	 *          The key to remove.
	 * @return True if the key was in the map, false otherwise.
	 */
	default boolean remove(K key) {
		return removeGet(key) != null;
	}

	default boolean removeAll(Collection<? extends K> keys) {
		boolean changed = false;
		for (K key : keys)
			changed = remove(key) | changed;
		return changed;
	}

	/**
	 * Remove all keys and values from the map.
	 *
	 * @return True if the map contained keys, false if it was empty.
	 */
	boolean clear();

	@Override
	default boolean isEmpty() {
		return keySet().isEmpty();
	}
}
