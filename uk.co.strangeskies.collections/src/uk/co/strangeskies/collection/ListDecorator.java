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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public interface ListDecorator<E> extends CollectionDecorator<List<E>, E>, List<E> {
	public static <E> List<E> over(List<E> component) {
		return new ListDecorator<E>() {
			@Override
			public List<E> getComponent() {
				return component;
			}

			@Override
			public String toString() {
				return getComponent().toString();
			}

			@Override
			public int hashCode() {
				return getComponent().hashCode();
			}

			@Override
			public boolean equals(Object obj) {
				return getComponent().equals(obj);
			}
		};
	}

	@Override
	List<E> getComponent();

	@Override
	default void add(int index, E element) {
		getComponent().add(index, element);
	}

	@Override
	default boolean addAll(int index, Collection<? extends E> c) {
		return getComponent().addAll(index, c);
	}

	@Override
	default E get(int index) {
		return getComponent().get(index);
	}

	@Override
	default int indexOf(Object o) {
		return getComponent().indexOf(o);
	}

	@Override
	default int lastIndexOf(Object o) {
		return getComponent().lastIndexOf(o);
	}

	@Override
	default ListIterator<E> listIterator() {
		return getComponent().listIterator();
	}

	@Override
	default ListIterator<E> listIterator(int index) {
		return getComponent().listIterator(index);
	}

	@Override
	default E remove(int index) {
		return getComponent().remove(index);
	}

	@Override
	default E set(int index, E element) {
		return getComponent().set(index, element);
	}

	@Override
	default List<E> subList(int fromIndex, int toIndex) {
		return new SubList<>(this, fromIndex, toIndex);
	}

	@Override
	default boolean add(E e) {
		return CollectionDecorator.super.add(e);
	}

	@Override
	default boolean addAll(Collection<? extends E> c) {
		return CollectionDecorator.super.addAll(c);
	}

	@Override
	default void clear() {
		CollectionDecorator.super.clear();
	}

	@Override
	default boolean contains(Object o) {
		return CollectionDecorator.super.contains(o);
	}

	@Override
	default boolean containsAll(Collection<?> c) {
		return CollectionDecorator.super.containsAll(c);
	}

	@Override
	default boolean isEmpty() {
		return CollectionDecorator.super.isEmpty();
	}

	@Override
	default Iterator<E> iterator() {
		return CollectionDecorator.super.iterator();
	}

	@Override
	default boolean remove(Object o) {
		return CollectionDecorator.super.remove(o);
	}

	@Override
	default boolean removeAll(Collection<?> c) {
		return CollectionDecorator.super.removeAll(c);
	}

	@Override
	default boolean retainAll(Collection<?> c) {
		return CollectionDecorator.super.retainAll(c);
	}

	@Override
	default int size() {
		return CollectionDecorator.super.size();
	}

	@Override
	default Object[] toArray() {
		return CollectionDecorator.super.toArray();
	}

	@Override
	default <A> A[] toArray(A[] a) {
		return CollectionDecorator.super.toArray(a);
	}
}
