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
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
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

import uk.co.strangeskies.utility.Self;

/**
 * 
 * A decorator for a {@link Stream} which wraps intermediate and terminal
 * operations such that they can be easily extended.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the elements of the stream
 * @param <S>
 *          the final implementing class
 */
public interface SelfStreamDecorator<T, S extends SelfStreamDecorator<T, S>>
    extends Stream<T>, Self<S> {
  Stream<T> getComponent();

  S decorateIntermediate(Function<? super Stream<T>, Stream<T>> transformation);

  @Override
  default S filter(Predicate<? super T> predicate) {
    return decorateIntermediate(s -> s.filter(predicate));
  }

  @Override
  default Iterator<T> iterator() {
    return getComponent().iterator();
  }

  @Override
  default Spliterator<T> spliterator() {
    return getComponent().spliterator();
  }

  @Override
  default boolean isParallel() {
    return getComponent().isParallel();
  }

  @Override
  default S sequential() {
    return decorateIntermediate(Stream::sequential);
  }

  @Override
  default S parallel() {
    return decorateIntermediate(Stream::parallel);
  }

  @Override
  default S unordered() {
    return decorateIntermediate(Stream::unordered);
  }

  @Override
  default S onClose(Runnable closeHandler) {
    return decorateIntermediate(d -> d.onClose(closeHandler));
  }

  @Override
  default void close() {
    getComponent().close();
  }

  @Override
  default <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
    return getComponent().map(mapper);
  }

  @Override
  default IntStream mapToInt(ToIntFunction<? super T> mapper) {
    return getComponent().mapToInt(mapper);
  }

  @Override
  default LongStream mapToLong(ToLongFunction<? super T> mapper) {
    return getComponent().mapToLong(mapper);
  }

  @Override
  default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
    return getComponent().mapToDouble(mapper);
  }

  @Override
  default <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
    return getComponent().flatMap(mapper);
  }

  @Override
  default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
    return getComponent().flatMapToInt(mapper);
  }

  @Override
  default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
    return getComponent().flatMapToLong(mapper);
  }

  @Override
  default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
    return getComponent().flatMapToDouble(mapper);
  }

  @Override
  default S distinct() {
    return decorateIntermediate(Stream::distinct);
  }

  @Override
  default S sorted() {
    return decorateIntermediate(Stream::sorted);
  }

  @Override
  default S sorted(Comparator<? super T> comparator) {
    return decorateIntermediate(s -> s.sorted(comparator));
  }

  @Override
  default S peek(Consumer<? super T> action) {
    return decorateIntermediate(s -> s.peek(action));
  }

  @Override
  default S limit(long maxSize) {
    return decorateIntermediate(s -> s.limit(maxSize));
  }

  @Override
  default S skip(long n) {
    return decorateIntermediate(s -> s.skip(n));
  }

  @Override
  default void forEach(Consumer<? super T> action) {
    getComponent().forEach(action);
  }

  @Override
  default void forEachOrdered(Consumer<? super T> action) {
    getComponent().forEachOrdered(action);
  }

  @Override
  default Object[] toArray() {
    return getComponent().toArray();
  }

  @Override
  default <A> A[] toArray(IntFunction<A[]> generator) {
    return getComponent().toArray(generator);
  }

  @Override
  default T reduce(T identity, BinaryOperator<T> accumulator) {
    return getComponent().reduce(identity, accumulator);
  }

  @Override
  default Optional<T> reduce(BinaryOperator<T> accumulator) {
    return getComponent().reduce(accumulator);
  }

  @Override
  default <U> U reduce(
      U identity,
      BiFunction<U, ? super T, U> accumulator,
      BinaryOperator<U> combiner) {
    return getComponent().reduce(identity, accumulator, combiner);
  }

  @Override
  default <R> R collect(
      Supplier<R> supplier,
      BiConsumer<R, ? super T> accumulator,
      BiConsumer<R, R> combiner) {
    return getComponent().collect(supplier, accumulator, combiner);
  }

  @Override
  default <R, A> R collect(Collector<? super T, A, R> collector) {
    return getComponent().collect(collector);
  }

  @Override
  default Optional<T> min(Comparator<? super T> comparator) {
    return getComponent().min(comparator);
  }

  @Override
  default Optional<T> max(Comparator<? super T> comparator) {
    return getComponent().max(comparator);
  }

  @Override
  default long count() {
    return getComponent().count();
  }

  @Override
  default boolean anyMatch(Predicate<? super T> predicate) {
    return getComponent().anyMatch(predicate);
  }

  @Override
  default boolean allMatch(Predicate<? super T> predicate) {
    return getComponent().allMatch(predicate);
  }

  @Override
  default boolean noneMatch(Predicate<? super T> predicate) {
    return getComponent().noneMatch(predicate);
  }

  @Override
  default Optional<T> findFirst() {
    return getComponent().findFirst();
  }

  @Override
  default Optional<T> findAny() {
    return getComponent().findAny();
  }
}
