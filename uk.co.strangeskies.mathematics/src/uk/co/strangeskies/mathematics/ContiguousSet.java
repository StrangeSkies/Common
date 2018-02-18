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
package uk.co.strangeskies.mathematics;

import static uk.co.strangeskies.mathematics.Interval.bounded;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

import uk.co.strangeskies.mathematics.operation.Incrementable;
import uk.co.strangeskies.mathematics.operation.Incrementor;
import uk.co.strangeskies.mathematics.operation.NaturalIncrementor;

public class ContiguousSet<T> implements Iterable<T>, Collection<T>, NavigableSet<T>, SortedSet<T> {
	private final Interval<T> range;
	private final Incrementor<T> incrementor;

	public ContiguousSet(Interval<T> range, Incrementor<T> incrementor) {
		this.range = range;
		this.incrementor = incrementor;
	}

	public static <T extends Incrementable<? extends T>> ContiguousSet<T> create(Interval<T> range) {
		return new ContiguousSet<>(range, new NaturalIncrementor<T>());
	}

	public final Interval<T> getRange() {
		return range;
	}

	public final Incrementor<T> getIncrementor() {
		return incrementor;
	}

	public final Comparator<? super T> getComparator() {
		return range.getComparator();
	}

	@Override
	public final Comparator<? super T> comparator() {
		return getComparator();
	}

	@Override
	public T first() {
		T first = range.getLeftEndpoint();
		if (!range.isLeftClosed()) {
			first = incrementor.getIncremented(first);
		}
		return first;
	}

	@Override
	public T last() {
		T last = range.getRightEndpoint();
		if (!range.isRightClosed()) {
			last = incrementor.getDecremented(last);
		}
		return last;
	}

	@Override
	public T lower(T e) {
		return null;
	}

	@Override
	public T floor(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T ceiling(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T higher(T e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollFirst() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T pollLast() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			T on = getRange().isLeftClosed()
					? getRange().getLeftEndpoint()
					: incrementor.increment(getRange().getLeftEndpoint());

			@Override
			public boolean hasNext() {
				return on != null;
			}

			@Override
			public T next() {
				T was = on;
				on = incrementor.increment(on);
				int compare = comparator().compare(on, getRange().getRightEndpoint());
				if (getRange().isRightClosed() ? compare < 0 : compare <= 0)
					on = null;
				return was;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public NavigableSet<T> descendingSet() {
		return new ContiguousSet<>(getRange(), getIncrementor().reversed());
	}

	@Override
	public Iterator<T> descendingIterator() {
		return descendingSet().iterator();
	}

	@Override
	public ContiguousSet<T> subSet(T from, boolean fromInclusive, T to, boolean toInclusive) {
		T fromRounded = ceiling(from);
		T toRounded = floor(to);
		return new ContiguousSet<>(
				bounded(fromRounded, toRounded, getComparator())
						.withClosedEndpoints(fromInclusive, toInclusive),
				getIncrementor());
	}

	@Override
	public NavigableSet<T> headSet(T toElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> subSet(T fromElement, T toElement) {
		return subSet(fromElement, true, toElement, false);
	}

	@Override
	public SortedSet<T> headSet(T toElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SortedSet<T> tailSet(T fromElement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <A> A[] toArray(A[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
