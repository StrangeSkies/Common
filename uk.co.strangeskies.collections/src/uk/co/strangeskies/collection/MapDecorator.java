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
package uk.co.strangeskies.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MapDecorator<K, V> extends Map<K, V> {
	Map<K, V> getComponent();

	@Override
	default int size() {
		return getComponent().size();
	}

	@Override
	default boolean isEmpty() {
		return getComponent().isEmpty();
	}

	@Override
	default boolean containsKey(Object key) {
		return getComponent().containsKey(key);
	}

	@Override
	default boolean containsValue(Object value) {
		return getComponent().containsValue(value);
	}

	@Override
	default V get(Object key) {
		return getComponent().get(key);
	}

	@Override
	default V put(K key, V value) {
		return getComponent().put(key, value);
	}

	@Override
	default V remove(Object key) {
		return getComponent().remove(key);
	}

	@Override
	default void putAll(Map<? extends K, ? extends V> m) {
		getComponent().putAll(m);
	}

	@Override
	default void clear() {
		getComponent().clear();
	}

	@Override
	default Set<K> keySet() {
		return getComponent().keySet();
	}

	@Override
	default Collection<V> values() {
		return getComponent().values();
	}

	@Override
	default Set<Map.Entry<K, V>> entrySet() {
		return getComponent().entrySet();
	}
}
