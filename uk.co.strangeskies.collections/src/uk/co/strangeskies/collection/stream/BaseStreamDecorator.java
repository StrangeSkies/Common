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
package uk.co.strangeskies.collection.stream;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A decorator for a {@link BaseStream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * <p>
 * Intermediate operations will return a wrapper over the result of applying
 * that operation to the component, such that raw references to component
 * streams cannot escape their wrapping.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the elements of the stream
 * @param <S>
 *          the type of the stream
 */
public interface BaseStreamDecorator<T, S extends BaseStream<T, S>> extends BaseStream<T, S> {
	/**
	 * @return the component reference
	 */
	S getComponent();

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	S decorateIntermediate(Function<? super S, S> transformation);

	/**
	 * @param <U>
	 *          the type of the resulting stream elements
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	<U> StreamDecorator<U> decorateIntermediateReference(Function<? super S, Stream<U>> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	IntStream decorateIntermediateInt(Function<? super S, IntStream> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	LongStream decorateIntermediateLong(Function<? super S, LongStream> transformation);

	/**
	 * @param transformation
	 *          the intermediate operation as a function on a stream
	 * @return a decorated instance of the result of application of the
	 *         transformation to the component
	 */
	DoubleStream decorateIntermediateDouble(Function<? super S, DoubleStream> transformation);

	/**
	 * @param <U>
	 *          the type of the termination result
	 * @param termination
	 *          the terminal operation as a function on a stream
	 * @return the result of application of the termination to the component
	 *         stream
	 */
	default <U> U decorateTerminal(Function<? super S, ? extends U> termination) {
		return termination.apply(getComponent());
	}

	/**
	 * @param termination
	 *          the terminal operation as a function on a stream
	 */
	default void decorateVoidTerminal(Consumer<? super S> termination) {
		termination.accept(getComponent());
	}

	@Override
	default S sequential() {
		return decorateIntermediate(BaseStream::sequential);
	}

	@Override
	default S parallel() {
		return decorateIntermediate(BaseStream::parallel);
	}

	@Override
	default S unordered() {
		return decorateIntermediate(BaseStream::unordered);
	}

	@Override
	default S onClose(Runnable closeHandler) {
		return decorateIntermediate(s -> s.onClose(closeHandler));
	}

	@Override
	default Iterator<T> iterator() {
		return decorateTerminal(BaseStream::iterator);
	}

	@Override
	default Spliterator<T> spliterator() {
		return decorateTerminal(BaseStream::spliterator);
	}

	@Override
	default boolean isParallel() {
		return getComponent().isParallel();
	}

	@Override
	default void close() {
		getComponent().close();
	}
}
