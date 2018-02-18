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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Provides an arbitrary total ordering over a type of object from an equality
 * relation. Guaranteed to be a consistent ordering for a particular
 * {@link EquivalenceComparator}, but not necessarily between different
 * instances of {@link EquivalenceComparator}.
 *
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          The type of object to compare.
 */
public class EquivalenceComparator<T> implements Comparator<T> {
  private final BiPredicate<? super T, ? super T> equality;

  private final Map<Integer, List<IDReference>> collisionMap;

  private final ReferenceQueue<Object> referenceQueue;

  /**
   * Create a fresh identity comparator.
   * 
   * @param equality
   *          The equality predicate with respect to which we wish to create a
   *          consistent ordering.
   */
  public EquivalenceComparator(BiPredicate<? super T, ? super T> equality) {
    this.equality = equality;

    collisionMap = new HashMap<>();

    referenceQueue = new ReferenceQueue<>();
  }

  /**
   * Create a new {@link EquivalenceComparator} over the identity operation.
   * 
   * @param <T>
   *          The type of the items to compare
   * @return The new equality comparator instance.
   */
  public static <T> EquivalenceComparator<T> identityComparator() {
    return new EquivalenceComparator<>((a, b) -> a == b);
  }

  /**
   * Create a new {@link EquivalenceComparator} over the {@link Object#equals}
   * equality operation.
   * 
   * @param <T>
   *          The type of the items to compare
   * @return The new equality comparator instance.
   */
  public static <T> EquivalenceComparator<T> naturalComparator() {
    return new EquivalenceComparator<>(Objects::equals);
  }

  @Override
  public int compare(EquivalenceComparator<T>this,T first, T second) {
    clean();

    if (equality.test(first, second)) {
      return 0;
    }

    int firstHash = System.identityHashCode(first);
    int secondHash = System.identityHashCode(second);

    if (firstHash != secondHash) {
      return secondHash - firstHash;
    }

    IDReference firstReference = new IDReference(first, firstHash, referenceQueue);
    IDReference secondReference = new IDReference(second, secondHash, referenceQueue);

    List<IDReference> collisions = collisionMap.get(firstHash);
    if (collisions == null) {
      collisions = new ArrayList<>();

      collisions.add(firstReference);
      collisions.add(secondReference);

      collisionMap.put(firstHash, collisions);

      return 1;
    } else {
      int firstIndex = collisions.indexOf(firstReference);
      if (firstIndex < 0) {
        collisions.add(0, firstReference);
        firstIndex = 0;
      }

      int secondIndex = collisions.indexOf(secondReference);
      if (secondIndex < 0) {
        collisions.add(0, secondReference);
        secondIndex = 0;
        firstIndex++;
      }

      return secondIndex - firstIndex;
    }
  }

  /**
   * This method can be called to prune stale references from the hash-collision
   * map. It is also called automatically
   */
  @SuppressWarnings("unchecked")
  public void clean(EquivalenceComparator<T>this) {
    IDReference oldReference;
    while ((oldReference = (IDReference) referenceQueue.poll()) != null) {
      List<IDReference> collisions = collisionMap.get(oldReference.getId());

      if (collisions.size() > 1) {
        collisions.remove(oldReference);
      } else {
        collisionMap.remove(oldReference.getId());
      }
    }
  }

  protected class IDReference extends WeakReference<T> {
    private final int id;

    public IDReference(T referent, int id, ReferenceQueue<? super T> q) {
      super(referent, q);

      this.id = id;
    }

    public int getId() {
      return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof Reference<?>)) {
        return false;
      }

      try {
        return equality.test((T) ((Reference<?>) obj).get(), get());
      } catch (Exception e) {
        return false;
      }
    }

    @Override
    public int hashCode() {
      return get().hashCode();
    }
  }
}
