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

import java.util.DoubleSummaryStatistics;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A decorator for a {@link DoubleStream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * 
 * @author Elias N Vasylenko
 */
public interface DoubleStreamDecorator extends BaseStreamDecorator<Double, DoubleStream>, DoubleStream {
	@Override
	default DoubleStream parallel() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default DoubleStream sequential() {
		return BaseStreamDecorator.super.parallel();
	}

	@Override
	default PrimitiveIterator.OfDouble iterator() {
		return decorateTerminal(DoubleStream::iterator);
	}

	@Override
	default Spliterator.OfDouble spliterator() {
		return decorateTerminal(DoubleStream::spliterator);
	}

	@Override
	DoubleStream getComponent();

	@Override
	default DoubleStream decorateIntermediate(Function<? super DoubleStream, DoubleStream> transformation) {
		return decorateIntermediateDouble(transformation);
	}

	@Override
	default DoubleStream filter(DoublePredicate predicate) {
		return decorateIntermediate(s -> s.filter(predicate));
	}

	@Override
	default DoubleStream map(DoubleUnaryOperator mapper) {
		return decorateIntermediate(s -> s.map(mapper));
	}

	@Override
	default <U> Stream<U> mapToObj(DoubleFunction<? extends U> mapper) {
		return decorateIntermediateReference(s -> s.mapToObj(mapper));
	}

	@Override
	default IntStream mapToInt(DoubleToIntFunction mapper) {
		return decorateIntermediateInt(s -> s.mapToInt(mapper));
	}

	@Override
	default LongStream mapToLong(DoubleToLongFunction mapper) {
		return decorateIntermediateLong(s -> s.mapToLong(mapper));
	}

	@Override
	default DoubleStream flatMap(DoubleFunction<? extends DoubleStream> mapper) {
		return decorateIntermediate(s -> s.flatMap(mapper));
	}

	@Override
	default DoubleStream distinct() {
		return decorateIntermediate(s -> s.distinct());
	}

	@Override
	default DoubleStream sorted() {
		return decorateIntermediate(s -> s.sorted());
	}

	@Override
	default DoubleStream peek(DoubleConsumer action) {
		return decorateIntermediate(s -> s.peek(action));
	}

	@Override
	default DoubleStream limit(long maxSize) {
		return decorateIntermediate(s -> s.limit(maxSize));
	}

	@Override
	default DoubleStream skip(long n) {
		return decorateIntermediate(s -> s.skip(n));
	}

	@Override
	default void forEach(DoubleConsumer action) {
		decorateVoidTerminal(s -> s.forEach(action));
	}

	@Override
	default void forEachOrdered(DoubleConsumer action) {
		decorateVoidTerminal(s -> s.forEachOrdered(action));
	}

	@Override
	default double[] toArray() {
		return decorateTerminal(s -> s.toArray());
	}

	@Override
	default double reduce(double identity, DoubleBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(identity, op));
	}

	@Override
	default OptionalDouble reduce(DoubleBinaryOperator op) {
		return decorateTerminal(s -> s.reduce(op));
	}

	@Override
	default <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> accumulator, BiConsumer<R, R> combiner) {
		return decorateTerminal(s -> s.collect(supplier, accumulator, combiner));
	}

	@Override
	default double sum() {
		return decorateTerminal(s -> s.sum());
	}

	@Override
	default OptionalDouble min() {
		return decorateTerminal(s -> s.min());
	}

	@Override
	default OptionalDouble max() {
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
	default DoubleSummaryStatistics summaryStatistics() {
		return decorateTerminal(s -> s.summaryStatistics());
	}

	@Override
	default boolean anyMatch(DoublePredicate predicate) {
		return decorateTerminal(s -> s.anyMatch(predicate));
	}

	@Override
	default boolean allMatch(DoublePredicate predicate) {
		return decorateTerminal(s -> s.allMatch(predicate));
	}

	@Override
	default boolean noneMatch(DoublePredicate predicate) {
		return decorateTerminal(s -> s.noneMatch(predicate));
	}

	@Override
	default OptionalDouble findFirst() {
		return decorateTerminal(s -> s.findFirst());
	}

	@Override
	default OptionalDouble findAny() {
		return decorateTerminal(s -> s.findAny());
	}

	@Override
	default Stream<Double> boxed() {
		return mapToObj(i -> i);
	}
}
