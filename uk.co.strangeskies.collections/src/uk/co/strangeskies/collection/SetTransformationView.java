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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.function.InvertibleFunction;

/**
 * A view of a set which will be automatically updated along with the original,
 * but who's elements will be a transformation of the original associated
 * elements by way of the function passed to the constructor. The implementation
 * employs lazy evaluation, so try to use get() as little as possible by reusing
 * the result.
 * 
 * <p>
 * Unlike the {@link Stream#map(Function)} function, which can provide similar
 * functionality in certain circumstances, this class provides a view which is
 * reusable and backed by the original collection, such that it will reflect
 * changes.
 * 
 * <p>
 * If the constructor is supplied with an {@link InvertibleFunction}, it will be
 * possible to add elements to this collection.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          The type of the elements of this list.
 * @param <F>
 *          The type of the elements of the backing list.
 */
public class SetTransformationView<F, T> extends AbstractSet<T> {
	private final Collection<F> backingCollection;
	private final InvertibleFunction<F, T> function;

	/**
	 * @param backingCollection
	 *          The backing collection this class presents a view over.
	 * @param function
	 *          The function which transforms elements into the form in which they
	 *          are represented in by this view.
	 */
	public SetTransformationView(Collection<F> backingCollection,
			Function<? super F, ? extends T> function) {
		this(backingCollection, InvertibleFunction.over(function, i -> {
			throw new UnsupportedOperationException();
		}));
	}

	/**
	 * @param backingCollection
	 *          The backing collection this class presents a view over.
	 * @param function
	 *          The function which transforms elements into the form in which they
	 *          are represented in by this view.
	 */
	public SetTransformationView(Collection<F> backingCollection,
			InvertibleFunction<F, T> function) {
		this.backingCollection = backingCollection;
		this.function = function;
	}

	@Override
	public boolean add(T e) {
		return backingCollection.add(function.getInverse().apply(e));
	}

	/**
	 * @return The function which transforms elements into the form in which they
	 *         are represented in by this view.
	 */
	public final InvertibleFunction<F, T> getFunction() {
		return function;
	}

	/**
	 * @return The backing collection this class presents a view over.
	 */
	public final Collection<F> getBackingCollection() {
		return Collections.unmodifiableCollection(backingCollection);
	}

	protected final Collection<F> getModifiablebackingCollection() {
		return backingCollection;
	}

	@Override
	public final int size() {
		return backingCollection.size();
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<? extends F> backingIterator = backingCollection.iterator();
		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return backingIterator.hasNext();
			}

			@Override
			public T next() {
				return function.apply(backingIterator.next());
			}

			@Override
			public void remove() {
				backingIterator.remove();
			}
		};
	}
}
