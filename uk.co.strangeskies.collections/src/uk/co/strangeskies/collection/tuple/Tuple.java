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
package uk.co.strangeskies.collection.tuple;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * A tuple entry, parameterised recursively with type information for each
 * subsequent entry. Currently entries must be non-null.
 * 
 * @author Elias N Vasylenko
 *
 * @param <H>
 *          The type of the head item.
 * @param <T>
 *          The fully qualified type of the tail {@link Tuple}.
 */
public class Tuple<H, T extends Tuple<?, ?>> {
	private H head;
	private T tail;

	/**
	 * Create a Tuple instance with the given values for its head and tail
	 * entries.
	 * 
	 * @param head
	 *          The head item.
	 * @param tail
	 *          The tail {@link Tuple}.
	 */
	public Tuple(H head, T tail) {
		this.head = head;
		this.tail = tail;
	}

	/**
	 * Return the value of the head entry.
	 * 
	 * @return Head entry/
	 */
	public H getHead() {
		return head;
	}

	/**
	 * Set the value of the head entry.
	 * 
	 * @param head
	 *          New head value. This parameter should be non-null.
	 * 
	 */
	public void setHead(H head) {
		this.head = head;
	}

	/**
	 * Map the value of the head entry.
	 * 
	 * @param headMap
	 *          A mapping to a new head value. The result should be non-null.
	 * @param <I>
	 *          The type of the head of the new Tuple.
	 * 
	 * @return A new derived tuple with the head value transformed by the given
	 *         mapping.
	 * 
	 */
	public <I> Tuple<I, T> mapHead(Function<? super H, ? extends I> headMap) {
		return new Tuple<>(headMap.apply(head), tail);
	}

	/**
	 * Map the value of the tail entry.
	 * 
	 * @param tailMap
	 *          A mapping to a new tail value. The result should be non-null.
	 * @param <U>
	 *          The type of the tail of the new Tuple.
	 * 
	 * @return A new derived tuple with the tail value transformed by the given
	 *         mapping.
	 * 
	 */
	public <U extends Tuple<?, ?>> Tuple<H, U> mapTail(Function<T, U> tailMap) {
		return new Tuple<>(head, tailMap.apply(tail));
	}

	/**
	 * Return the value of the tail entry.
	 * 
	 * @return Tail value, which is itself a tuple.
	 */
	public T getTail() {
		if (tail.equals(EmptyTuple.get())) {
			throw new NoSuchElementException();
		}
		return tail;
	}

	/**
	 * Set the tuple entry which represents the tail of this tuple.
	 * 
	 * @param tail
	 *          A new tuple of the exact expected type of the tail of this tuple.
	 */
	public void setTail(T tail) {
		if (this.tail.equals(EmptyTuple.get())) {
			throw new NoSuchElementException();
		}

		this.tail = tail;
	}

	/**
	 * Does this tuple have a tail, or is it terminating.
	 * 
	 * @return True if the tuple has a tail, false otherwise.
	 */
	public boolean hasTail() {
		return !tail.equals(EmptyTuple.get());
	}

	/**
	 * The number of entries in this tuple, including the head and every entry in
	 * the tail.
	 * 
	 * @return The number of entries.
	 */
	public int getSize() {
		return tailCounter(1);
	}

	protected int tailCounter(int size) {
		if (hasTail()) {
			return getTail().tailCounter(++size);
		} else {
			return size;
		}
	}

	/**
	 * This method prepends the tuple with another tuple entry with the given
	 * value, resulting in a tuple one extra in size. The receiving tuple remains
	 * unchanged.
	 * 
	 * @param <P>
	 *          The type of the item to prepend.
	 * @param prepend
	 *          The head value of the new tuple.
	 * @return A new tuple instance with the given value as its head, and this
	 *         tuple as its tail.
	 */
	public <P> Tuple<P, Tuple<H, T>> prepend(P prepend) {
		return new Tuple<P, Tuple<H, T>>(prepend, this);
	}

	/**
	 * @param head
	 *          The value to set head with.
	 * @return The tail of the tuple, ready to be chained with another invocation
	 *         of {@link Tuple#next(Object)} such that every value can be set.
	 */
	public T next(H head) {
		setHead(head);

		return tail;
	}

	@Override
	public String toString() {
		return "{ " + toSubStrings() + " }";
	}

	protected String toSubStrings() {
		if (hasTail()) {
			return getHead().toString() + ", " + getTail().toSubStrings();
		} else {
			return getHead().toString();
		}
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Tuple)) {
			return false;
		}

		Tuple<?, ?> otherTuple = (Tuple<?, ?>) other;

		if (getSize() != otherTuple.getSize()) {
			return false;
		}

		if (!Objects.equals(getHead(), otherTuple.getHead())) {
			return false;
		}

		if (hasTail()) {
			return getTail().equals(otherTuple.getTail());
		}

		return true;
	}
}
