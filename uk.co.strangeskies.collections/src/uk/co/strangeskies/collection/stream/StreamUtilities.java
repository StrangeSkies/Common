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

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.strangeskies.function.ThrowingSupplier;

/**
 * A collection of static utility methods for working with streams.
 * 
 * @author Elias N Vasylenko
 */
public class StreamUtilities {
  private StreamUtilities() {}

  public static <T> BinaryOperator<T> throwingSerialCombiner() {
    return throwingReduce(
        (a, b) -> new IllegalArgumentException(
            "Cannot combine parallel execution of serial stream"));
  }

  public static <T> BinaryOperator<T> throwingReduce() {
    return throwingReduce(
        (a, b) -> new IllegalArgumentException("Cannot reduce items " + a + ", " + b));
  }

  public static <T> BinaryOperator<T> throwingReduce(BiFunction<T, T, RuntimeException> exception) {
    return (a, b) -> {
      throw exception.apply(a, b);
    };
  }

  public static boolean equals(Stream<?> first, Stream<?> second) {
    Iterator<?> firstIterator = first.iterator();
    Iterator<?> secondIterator = second.iterator();

    while (firstIterator.hasNext() && secondIterator.hasNext()) {
      if (!Objects.equals(firstIterator.next(), secondIterator.next())) {
        return false;
      }
    }

    return !firstIterator.hasNext() && !secondIterator.hasNext();
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> upcastStream(Stream<? extends T> stream) {
    return (Stream<T>) stream;
  }

  @SuppressWarnings("unchecked")
  public static <T> Optional<T> upcastOptional(Optional<? extends T> stream) {
    return (Optional<T>) stream;
  }

  public static <T> Optional<T> tryOptional(ThrowingSupplier<? extends T, ?> attempt) {
    return tryOptional(attempt, e -> {}, e -> {});
  }

  @SuppressWarnings("unchecked")
  public static <T, E extends Exception> Optional<T> tryOptional(
      ThrowingSupplier<? extends T, E> attempt,
      Consumer<? super Exception> runtimeExceptions,
      Consumer<? super E> checkedExceptions) {
    try {
      return Optional.of(attempt.get());
    } catch (RuntimeException e) {
      runtimeExceptions.accept(e);
      return Optional.empty();
    } catch (Exception e) {
      checkedExceptions.accept((E) e);
      return Optional.empty();
    }
  }

  public static <T> Optional<T> tryOptional(
      Supplier<T> attempt,
      Consumer<? super RuntimeException> exceptions) {
    try {
      return Optional.of(attempt.get());
    } catch (RuntimeException e) {
      exceptions.accept(e);
      return Optional.empty();
    }
  }

  public static <A, B> Collector<Entry<? extends A, ? extends B>, ?, Map<A, B>> entriesToMap() {
    return Collectors.toMap(Entry::getKey, Entry::getValue, throwingReduce(), LinkedHashMap::new);
  }

  public static <A, B> Stream<Entry<A, B>> zip(Stream<A> first, Stream<B> second) {
    return zip(first, second, (Supplier<RuntimeException>) null);
  }

  public static <A, B> Stream<Entry<A, B>> zip(
      Stream<A> first,
      Stream<B> second,
      Supplier<RuntimeException> mismatchedStreams) {
    return zip(first, second, SimpleEntry<A, B>::new, mismatchedStreams);
  }

  public static <A, B, R> Stream<R> zip(
      Stream<A> first,
      Stream<B> second,
      BiFunction<A, B, R> combiner) {
    return zip(first, second, combiner, null);
  }

  public static <A, B, R> Stream<R> zip(
      Stream<A> first,
      Stream<B> second,
      BiFunction<A, B, R> combiner,
      Supplier<RuntimeException> mismatchedStreams) {
    Iterator<A> firstIterator = first.iterator();
    Iterator<B> secondIterator = second.iterator();

    Iterable<R> i = () -> new Iterator<R>() {
      @Override
      public boolean hasNext() {
        if (firstIterator.hasNext() != secondIterator.hasNext()) {
          if (mismatchedStreams != null) {
            throw mismatchedStreams.get();
          } else {
            return false;
          }
        } else {
          return firstIterator.hasNext();
        }
      }

      @Override
      public R next() {
        return combiner.apply(firstIterator.next(), secondIterator.next());
      }
    };

    return StreamSupport.stream(i.spliterator(), false);
  }

  /**
   * @param stream
   *          an ordered stream
   * @param <T>
   *          the type of the stream elements
   * @return a new stream over the elements contained in the given stream in
   *         reverse order
   */
  public static <T> Stream<T> reverse(Stream<? extends T> stream) {
    List<T> collection = stream.collect(Collectors.toList());

    Iterator<T> iterator = new Iterator<T>() {
      private int index = collection.size();

      @Override
      public boolean hasNext() {
        return index > 0;
      }

      @Override
      public T next() {
        return collection.get(--index);
      }
    };

    return StreamSupport
        .stream(
            Spliterators
                .spliterator(
                    iterator,
                    collection.size(),
                    Spliterator.ORDERED | Spliterator.IMMUTABLE),
            false);
  }

  /**
   * A bit like {@link Stream#iterate(Object, UnaryOperator)} but not
   * <em>completely and utterly useless</em> because it actually supports
   * termination.
   * 
   * TODO should hopefully be made redundant by takeWhile in Java 9
   * 
   * @param <T>
   *          the type of the stream elements
   * @param root
   *          the root element
   * @param mapping
   *          a mapping from an element to the next element
   * @return a stream over each element in sequence
   */
  public static <T> Stream<T> iterate(T root, Function<? super T, ? extends T> mapping) {
    return iterateOptional(root, t -> Optional.ofNullable(mapping.apply(t)));
  }

  /**
   * A bit like {@link Stream#iterate(Object, UnaryOperator)} but not
   * <em>completely and utterly useless</em> because it actually supports
   * termination.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param root
   *          the root element
   * @param mapping
   *          a mapping from an element to the next element
   * @return a stream over each element in sequence
   */
  public static <T> Stream<T> iterateOptional(
      T root,
      Function<? super T, Optional<? extends T>> mapping) {
    return iterateOptional(Optional.ofNullable(root), mapping);
  }

  /**
   * A bit like {@link Stream#iterate(Object, UnaryOperator)} but not
   * <em>completely and utterly useless</em> because it actually supports
   * termination.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param root
   *          the root element
   * @param mapping
   *          a mapping from an element to the next element
   * @return a stream over each element in sequence
   */
  public static <T> Stream<T> iterateOptional(
      Optional<? extends T> root,
      Function<? super T, Optional<? extends T>> mapping) {
    Iterator<T> iterator = new Iterator<T>() {
      private Optional<? extends T> item = root;

      @Override
      public boolean hasNext() {
        return item.isPresent();
      }

      @Override
      public T next() {
        T result = item.get();

        item = mapping.apply(result);

        return result;
      }
    };
    return StreamSupport
        .stream(
            Spliterators
                .spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.IMMUTABLE),
            false);
  }

  /**
   * Generate a stream which recursively traverses depth-first over the elements
   * of some nested data structure starting from its root.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param root
   *          the root element
   * @param mapping
   *          a mapping from an element to a stream of its direct children
   * @return a stream over the root and each of its children, as well as each of
   *         their children, in a depth first manner
   */
  public static <T> Stream<T> flatMapRecursive(
      T root,
      Function<? super T, ? extends Stream<? extends T>> mapping) {
    return flatMapRecursive(Stream.of(root), mapping);
  }

  /**
   * Generate a stream which recursively traverses depth-first over the elements
   * of some nested data structure starting from those in a given stream.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param stream
   *          the stream of initial elements
   * @param mapping
   *          a mapping from an element to a stream of its direct children
   * @return a stream over elements in a tree and each of their children, as well
   *         as each of their children, in a depth first manner
   */
  public static <T> Stream<T> flatMapRecursive(
      Stream<? extends T> stream,
      Function<? super T, ? extends Stream<? extends T>> mapping) {
    return stream.flatMap(s -> concat(of(s), flatMapRecursive(mapping.apply(s), mapping)));
  }

  /**
   * Generate a stream which recursively traverses depth-first over the elements
   * of some nested data structure starting from its root.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param root
   *          the root element
   * @param mapping
   *          a mapping from an element to a stream of its direct children
   * @return a stream over the root and each of its children, as well as each of
   *         their children, in a depth first manner
   */
  public static <T> Stream<T> flatMapRecursiveDistinct(
      T root,
      Function<? super T, ? extends Stream<? extends T>> mapping) {
    return flatMapRecursiveDistinct(Stream.of(root), mapping);
  }

  /**
   * Generate a stream which recursively traverses depth-first over the elements
   * of some nested data structure starting from those in a given stream.
   * <p>
   * Repeated elements will be ignored.
   * 
   * @param <T>
   *          the type of the stream elements
   * @param stream
   *          the stream of initial elements
   * @param mapping
   *          a mapping from an element to a stream of its direct children
   * @return a stream over elements in a tree and each of their children, as well
   *         as each of their children, in a depth first manner
   */
  public static <T> Stream<T> flatMapRecursiveDistinct(
      Stream<? extends T> stream,
      Function<? super T, ? extends Stream<? extends T>> mapping) {
    return flatMapRecursiveDistinct(stream, mapping, new HashSet<>());
  }

  protected static <T> Stream<T> flatMapRecursiveDistinct(
      Stream<? extends T> stream,
      Function<? super T, ? extends Stream<? extends T>> mapping,
      Set<T> visited) {
    return stream
        .filter(visited::add)
        .flatMap(s -> concat(of(s), flatMapRecursiveDistinct(mapping.apply(s), mapping, visited)));
  }
}
