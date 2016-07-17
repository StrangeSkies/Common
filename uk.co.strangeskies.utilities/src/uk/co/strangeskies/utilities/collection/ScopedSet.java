/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.utilities.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utilities.collection;

import static java.util.Collections.emptyListIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import uk.co.strangeskies.utilities.Factory;
import uk.co.strangeskies.utilities.Scoped;

public abstract class ScopedSet<S extends ScopedSet<S, T>, T> implements SetDecorator<T>, Scoped<S> {
	static class ScopedSetImpl<T> extends ScopedSet<ScopedSetImpl<T>, T> {
		private final Factory<Set<T>> componentFactory;

		ScopedSetImpl(Factory<Set<T>> componentFactory) {
			super(componentFactory.create());
			this.componentFactory = componentFactory;
		}

		private ScopedSetImpl(ScopedSetImpl<T> parent, Factory<Set<T>> componentFactory) {
			super(parent, componentFactory.create());
			this.componentFactory = componentFactory;
		}

		@Override
		public ScopedSetImpl<T> nestChildScope() {
			return new ScopedSetImpl<>(this, componentFactory);
		}

		@Override
		public ScopedSetImpl<T> copy() {
			ScopedSetImpl<T> copy = new ScopedSetImpl<>(componentFactory);
			copy.addAll(this);
			return copy;
		}
	}

	private final S parent;
	private final Set<T> component;

	public ScopedSet(Set<T> component) {
		this(null, component);
	}

	protected ScopedSet(S parent, Set<T> component) {
		this.parent = parent;
		this.component = component;
	}

	@Override
	public Set<T> getComponent() {
		return component;
	}

	public static <T> ScopedSet<?, T> over(Factory<Set<T>> componentFactory) {
		return new ScopedSetImpl<>(componentFactory);
	}

	@Override
	public boolean add(T e) {
		if (getParentScope().map(p -> p.contains(e)).orElse(false))
			return false;

		return SetDecorator.super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;

		for (T e : c) {
			changed = add(e) || changed;
		}

		return changed;
	}

	@Override
	public Iterator<T> iterator() {
		Iterator<T> iterator = SetDecorator.super.iterator();
		Iterator<T> parentIterator = getParentScope().map(Collection::iterator).orElse(emptyListIterator());

		return new Iterator<T>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext() || parentIterator.hasNext();
			}

			@Override
			public T next() {
				return iterator.hasNext() ? iterator.next() : parentIterator.next();
			}
		};
	}

	@Override
	public boolean isEmpty() {
		return SetDecorator.super.isEmpty() && getParentScope().map(Collection::isEmpty).orElse(true);
	}

	@Override
	public Optional<S> getParentScope() {
		return Optional.ofNullable(parent);
	}

	@Override
	public void collapseIntoParentScope() {
		getParentScope().get().addAll(this);
		clear();
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
}
