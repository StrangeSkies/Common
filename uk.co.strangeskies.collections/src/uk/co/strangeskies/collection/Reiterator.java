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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A buffer to decouple the delivery of events with their sequential
 * consumption, such that events consumed will be queued for later supply.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of event message to consume
 * @param <R>
 *          the type of event message to produce
 */
public interface Reiterator<T, R> extends Consumer<T>, Supplier<R>, Iterable<R> {
  static <T> Reiterator<T, T> queueReiterator() {
    return new Reiterator<T, T>() {
      private final Deque<T> queue = new ArrayDeque<>();

      @Override
      public synchronized void accept(T t) {
        queue.push(t);
        notifyAll();
      }

      @Override
      public synchronized T get() {
        try {
          while (queue.isEmpty()) {
            wait();
          }
          return queue.pop();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  static <T> Reiterator<T, T> latestReiterator() {
    return new Reiterator<T, T>() {
      private T latest;

      @Override
      public synchronized void accept(T t) {
        latest = t;
        notifyAll();
      }

      @Override
      public synchronized T get() {
        try {
          while (latest == null) {
            wait();
          }
          T latest = this.latest;
          this.latest = null;
          return latest;
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  static <T> Reiterator<T, Stream<T>> aggregatingReiterator() {
    return new Reiterator<T, Stream<T>>() {
      private List<T> aggregate;

      @Override
      public synchronized void accept(T t) {
        aggregate.add(t);
        notifyAll();
      }

      @Override
      public synchronized Stream<T> get() {
        try {
          while (aggregate.isEmpty()) {
            wait();
          }
          List<T> latest = aggregate;
          aggregate = null;
          return latest.stream();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  default <U> Reiterator<T, U> map(Function<R, U> map) {
    Reiterator<T, R> source = this;

    return new Reiterator<T, U>() {
      @Override
      public void accept(T t) {
        source.accept(t);
      }

      @Override
      public U get() {
        return map.apply(source.get());
      }
    };
  }

  @Override
  default Iterator<R> iterator() {
    return new Iterator<R>() {
      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public R next() {
        return get();
      }
    };
  }

  /**
   * @return A never-ending, blocking stream of events
   */
  default Stream<R> stream() {
    return StreamSupport.stream(spliterator(), false);
  }
}
