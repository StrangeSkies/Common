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

import static uk.co.strangeskies.collection.EquivalenceComparator.identityComparator;

import java.util.AbstractList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.co.strangeskies.function.InvertibleFunction;

/**
 * A view of a {@link List} which will be automatically updated along with the
 * original, but whose elements will be a transformation of the original
 * associated elements by way of the function passed to the constructor. The
 * implementation employs lazy evaluation, and only evaluates a transformation
 * once for each element of the collection, as distinguished by identity.
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
public class ListTransformOnceView<F, T> extends AbstractList<T> {
	private final List<F> backingCollection;
	private final InvertibleFunction<F, T> function;

	private final Map<F, T> transformations;

	/**
	 * @param backingCollection
	 *          The backing collection this class presents a view over.
	 * @param function
	 *          The function which transforms elements into the form in which they
	 *          are represented in by this view.
	 */
	public ListTransformOnceView(List<F> backingCollection, final Function<? super F, ? extends T> function) {
		this(backingCollection, InvertibleFunction.over(function, i -> {
			throw new UnsupportedOperationException();
		}));
	}

	@Override
	public T set(int index, T element) {
		T previous = get(index);
		F next = function.getInverse().apply(element);
		backingCollection.set(index, next);
		transformations.put(next, element);
		return previous;
	}

	@Override
	public void add(int index, T element) {
		F next = function.getInverse().apply(element);
		backingCollection.add(index, next);
		transformations.put(next, element);
	}

	@Override
	public T remove(int index) {
		T removed = get(index);
		transformations.remove(backingCollection.remove(index));
		return removed;
	}

	/**
	 * @param backingCollection
	 *          The backing collection this class presents a view over.
	 * @param function
	 *          The function which transforms elements into the form in which they
	 *          are represented in by this view.
	 */
	public ListTransformOnceView(List<F> backingCollection, final InvertibleFunction<F, T> function) {
		transformations = new TreeMap<>(identityComparator());

		this.backingCollection = backingCollection;
		this.function = function;
	}

	@Override
	public final T get(int index) {
		F backingElement = backingCollection.get(index);
		T transformation = transformations.get(backingElement);
		if (transformation == null) {
			transformation = function.apply(backingElement);
			transformations.put(backingElement, transformation);

			if (transformations.keySet().size() > backingCollection.size() * 1.5) {
				transformations.keySet().retainAll(backingCollection);
			}
		}
		return transformation;
	}

	/**
	 * @return The backing collection this class presents a view over.
	 */
	public final List<F> getBackingList() {
		return backingCollection;
	}

	/**
	 * @return The function which transforms elements into the form in which they
	 *         are represented in by this view.
	 */
	public final Function<F, T> getFunction() {
		return function;
	}

	@Override
	public final int size() {
		return backingCollection.size();
	}
}
