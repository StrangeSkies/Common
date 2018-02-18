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
package uk.co.strangeskies.mathematics.graph;

import java.util.Comparator;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EdgeVertices<V> {
	V getFrom();

	V getTo();

	static <V> EdgeVertices<V> between(V from, V to) {
		return new EdgeVertices<V>() {
			@Override
			public V getFrom() {
				return from;
			}

			@Override
			public V getTo() {
				return to;
			}

			@Override
			public int hashCode() {
				return from.hashCode() ^ to.hashCode();
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof EdgeVertices))
					return false;
				EdgeVertices<?> edge = (EdgeVertices<?>) object;
				return (from == edge.getFrom() && to == edge.getTo()) || (from == edge.getTo() && to == edge.getFrom());
			}

			@Override
			public String toString() {
				return getFrom() + " - " + getTo();
			}
		};
	}

	static <V> EdgeVertices<V> between(V from, V to, Comparator<V> direction) {
		int edgeDirection = direction.compare(from, to);

		if (edgeDirection > 0) {
			V temp = from;
			from = to;
			to = temp;
		} else if (edgeDirection == 0) {
			return null;
		}

		V fromFinal = from;
		V toFinal = to;

		return new EdgeVertices<V>() {
			@Override
			public V getFrom() {
				return fromFinal;
			}

			@Override
			public V getTo() {
				return toFinal;
			}

			@Override
			public int hashCode() {
				return fromFinal.hashCode() ^ toFinal.hashCode() * 7;
			}

			@Override
			public boolean equals(Object object) {
				if (!(object instanceof EdgeVertices))
					return false;
				EdgeVertices<?> edge = (EdgeVertices<?>) object;
				return fromFinal == edge.getFrom() && toFinal == edge.getTo();
			}

			@Override
			public String toString() {
				return getFrom() + " -> " + getTo();
			}
		};
	}
}
