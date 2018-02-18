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

import java.util.IntSummaryStatistics;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A decorator for an {@link IntStream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * 
 * @author Elias N Vasylenko
 */
public interface IntStreamDecorator extends BaseStreamDecorator<Integer, IntStream>, IntStream {
	@Override
	default IntStream parallel() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default IntStream sequential() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default PrimitiveIterator.OfInt iterator() {
		return decorateTerminal(IntStream::iterator);
	}

	@Override
	default Spliterator.OfInt spliterator() {
		return decorateTerminal(IntStream::spliterator);
	}

	@Override
	IntStream getComponent();

	@Override
	default IntStream decorateIntermediate(Function<? super IntStream, IntStream> transformation) {
		return decorateIntermediateInt(transformation);
	}

	@Override
	default IntStream filter(IntPredicate predicate) {
		return decorateIntermediate(s -> s.filter(predicate));
	}

	@Override
	default IntStream map(IntUnaryOperator mapper) {
		return decorateIntermediate(s -> s.map(mapper));
	}

	@Override
	default <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		return decorateIntermediateReference(s -> s.mapToObj(mapper));
	}

	@Override
	default LongStream mapToLong(IntToLongFunction mapper) {
		return decorateIntermediateLong(s -> s.mapToLong(mapper));
	}

	@Override
	default DoubleStream mapToDouble(IntToDoubleFunction mapper) {
		return decorateIntermediateDouble(s -> s.mapToDouble(mapper));
	}

	@Override
	default IntStream flatMap(IntFunction<? extends IntStream> mapper) {
		return decorateIntermediate(s -> s.flatMap(mapper));
	}

	@Override
	default IntStream distinct() {
		return decorateIntermediate(s -> s.distinct());
	}

	@Override
	default IntStream sorted() {
		return decorateIntermediate(s -> s.sorted());
	}

	@Override
	default IntStream peek(IntConsumer action) {
		return decorateIntermediate(s -> s.peek(action));
	}

	@Override
	default IntStream limit(long maxSize) {
		return decorateIntermediate(s -> s.limit(maxSize));
	}

	@Override
	default IntStream skip(long n) {
		return decorateIntermediate(s -> s.skip(n));
	}

	@Override
	default void forEach(IntConsumer action) {
		decorateVoidTerminal(s -> s.forEach(action));
	}

	@Override
	default void forEachOrdered(IntConsumer action) {
		decorateVoidTerminal(s -> s.forEachOrdered(action));
	}

	@Override
	default int[] toArray() {
		return decorateTerminal(s -> s.toArray());
	}

	@Override
	default int reduce(int identity, IntBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(identity, op));
	}

	@Override
	default OptionalInt reduce(IntBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(op));
	}

	@Override
	default <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
		return decorateTerminal(s -> s.collect(supplier, accumulator, combiner));
	}

	@Override
	default int sum() {
		return decorateTerminal(s -> s.sum());
	}

	@Override
	default OptionalInt min() {
		return decorateTerminal(s -> s.min());
	}

	@Override
	default OptionalInt max() {
		return decorateTerminal(s -> s.max());
	}

	@Override
	default long count() {
		return decorateTerminal(s -> s.count());
	}

	@Override
	default OptionalDouble average() {
		return decorateTerminal(s -> s.average());
	}

	@Override
	default IntSummaryStatistics summaryStatistics() {
		return decorateTerminal(s -> s.summaryStatistics());
	}

	@Override
	default boolean anyMatch(IntPredicate predicate) {
		return decorateTerminal(s -> s.anyMatch(predicate));
	}

	@Override
	default boolean allMatch(IntPredicate predicate) {
		return decorateTerminal(s -> s.allMatch(predicate));
	}

	@Override
	default boolean noneMatch(IntPredicate predicate) {
		return decorateTerminal(s -> s.noneMatch(predicate));
	}

	@Override
	default OptionalInt findFirst() {
		return decorateTerminal(s -> s.findFirst());
	}

	@Override
	default OptionalInt findAny() {
		return decorateTerminal(s -> s.findAny());
	}

	@Override
	default LongStream asLongStream() {
		return decorateIntermediateLong(s -> s.asLongStream());
	}

	@Override
	default DoubleStream asDoubleStream() {
		return decorateIntermediateDouble(s -> s.asDoubleStream());
	}

	@Override
	default Stream<Integer> boxed() {
		return mapToObj(i -> i);
	}
}
