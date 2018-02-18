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

import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * 
 * A decorator for a {@link Stream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the elements of the stream
 */
public interface StreamDecorator<T> extends BaseStreamDecorator<T, Stream<T>>, Stream<T> {
	@Override
	Stream<T> getComponent();

	@Override
	default Stream<T> decorateIntermediate(Function<? super Stream<T>, Stream<T>> transformation) {
		return decorateIntermediateReference(transformation);
	}

	@Override
	default Stream<T> filter(Predicate<? super T> predicate) {
		return decorateIntermediate(s -> s.filter(predicate));
	}

	@Override
	default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
		return decorateIntermediateReference(s -> s.map(mapper));
	}

	@Override
	default IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return decorateIntermediateInt(s -> s.mapToInt(mapper));
	}

	@Override
	default LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return decorateIntermediateLong(s -> s.mapToLong(mapper));
	}

	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return decorateIntermediateDouble(s -> s.mapToDouble(mapper));
	}

	@Override
	default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return decorateIntermediateReference(s -> s.flatMap(mapper));
	}

	@Override
	default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return decorateIntermediateInt(s -> s.flatMapToInt(mapper));
	}

	@Override
	default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return decorateIntermediateLong(s -> s.flatMapToLong(mapper));
	}

	@Override
	default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return decorateIntermediateDouble(s -> s.flatMapToDouble(mapper));
	}

	@Override
	default Stream<T> distinct() {
		return decorateIntermediate(Stream::distinct);
	}

	@Override
	default Stream<T> sorted() {
		return decorateIntermediate(Stream::sorted);
	}

	@Override
	default Stream<T> sorted(Comparator<? super T> comparator) {
		return decorateIntermediate(s -> s.sorted(comparator));
	}

	@Override
	default Stream<T> peek(Consumer<? super T> action) {
		return decorateIntermediate(s -> s.peek(action));
	}

	@Override
	default Stream<T> limit(long maxSize) {
		return decorateIntermediate(s -> s.limit(maxSize));
	}

	@Override
	default Stream<T> skip(long n) {
		return decorateIntermediate(s -> s.skip(n));
	}

	@Override
	default void forEach(Consumer<? super T> action) {
		decorateVoidTerminal(s -> s.forEach(action));
	}

	@Override
	default void forEachOrdered(Consumer<? super T> action) {
		decorateVoidTerminal(s -> s.forEachOrdered(action));
	}

	@Override
	default Object[] toArray() {
		return decorateTerminal(Stream::toArray);
	}

	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		return decorateTerminal(s -> s.toArray(generator));
	}

	@Override
	default T reduce(T identity, BinaryOperator<T> accumulator) {
		return decorateTerminal(s -> s.reduce(identity, accumulator));
	}

	@Override
	default Optional<T> reduce(BinaryOperator<T> accumulator) {
		return decorateTerminal(s -> s.reduce(accumulator));
	}

	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return decorateTerminal(s -> s.reduce(identity, accumulator, combiner));
	}

	@Override
	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return decorateTerminal(s -> s.collect(supplier, accumulator, combiner));
	}

	@Override
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		return decorateTerminal(s -> s.collect(collector));
	}

	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		return decorateTerminal(s -> s.min(comparator));
	}

	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		return decorateTerminal(s -> s.max(comparator));
	}

	@Override
	default long count() {
		return decorateTerminal(Stream::count);
	}

	@Override
	default boolean anyMatch(Predicate<? super T> predicate) {
		return decorateTerminal(s -> s.anyMatch(predicate));
	}

	@Override
	default boolean allMatch(Predicate<? super T> predicate) {
		return decorateTerminal(s -> s.allMatch(predicate));
	}

	@Override
	default boolean noneMatch(Predicate<? super T> predicate) {
		return decorateTerminal(s -> s.noneMatch(predicate));
	}

	@Override
	default Optional<T> findFirst() {
		return decorateTerminal(s -> s.findFirst());
	}

	@Override
	default Optional<T> findAny() {
		return decorateTerminal(s -> s.findAny());
	}
}
