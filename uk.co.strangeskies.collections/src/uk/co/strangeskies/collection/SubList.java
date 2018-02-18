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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class SubList<T> extends AbstractList<T> {
	private final List<T> backingList;

	private int startPosition = 0;
	private int endPosition = 0;

	public SubList(List<T> backingList, int startPosition, int endPosition) {
		this.backingList = backingList;

		this.startPosition = startPosition;
		this.endPosition = endPosition;
	}

	@Override
	public int size() {
		return endPosition - startPosition;
	}

	@Override
	public T get(int index) {
		if (index + startPosition >= endPosition) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		return backingList.get(index + startPosition);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return backingList.subList(this.startPosition + fromIndex, this.startPosition + toIndex);
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			Iterator<T> iterator = backingList.iterator();
			int i = 0;

			{
				consumeStart();
			}

			@Override
			public boolean hasNext() {
				return i < endPosition;
			}

			@Override
			public T next() {
				i++;
				return iterator.next();
			}

			private void consumeStart() {
				while (i < startPosition) {
					iterator.next();
					i++;
				}
			}
		};
	}
}
