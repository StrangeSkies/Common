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
 * This file is part of uk.co.strangeskies.collections.observable.
 *
 * uk.co.strangeskies.collections.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.collections.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.collection.observable;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * @author Elias N Vasylenko
 */
@SuppressWarnings("javadoc")
public class ObservableSetTest {
  private static final String ONE = "one";
  private static final String TWO = "two";
  private static final String THREE = "three";
  private static final String FOUR = "four";
  private static final String FIVE = "five";

  private static final Set<String> STRINGS = unmodifiableSet(of(ONE, TWO, THREE).collect(toSet()));

  private <E> List<E> addedList(ObservableSet<E> set) {
    List<E> list = new ArrayList<>();
    set.changes().observe(c -> list.addAll(c.added()));
    return list;
  }

  private <E> List<E> removedList(ObservableSet<E> set) {
    List<E> list = new ArrayList<>();
    set.changes().observe(c -> list.addAll(c.removed()));
    return list;
  }

  private <E> Set<E> addedSet(ObservableSet<E> set) {
    Set<E> list = new HashSet<>();
    set.changes().observe(c -> list.addAll(c.added()));
    return list;
  }

  private <E> Set<E> removedSet(ObservableSet<E> set) {
    Set<E> list = new HashSet<>();
    set.changes().observe(c -> list.addAll(c.removed()));
    return list;
  }

  @Test
  public void sortedTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new TreeSet<>());
    strings.addAll(STRINGS);

    assertTrue(strings.add(FOUR));

    assertEquals(asList(FOUR, ONE, THREE, TWO), new ArrayList<>(strings));
  }

  @Test
  public void addItemTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new HashSet<>(STRINGS));
    List<String> added = addedList(strings);

    assertTrue(strings.add(FOUR));

    assertEquals(asList(FOUR), added);
    assertNotEquals(STRINGS, strings);
  }

  @Test
  public void addItemsTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new HashSet<>(STRINGS));
    List<String> added = addedList(strings);

    assertTrue(strings.add(FOUR));
    assertTrue(strings.add(FIVE));

    assertEquals(asList(FOUR, FIVE), added);
    assertNotEquals(STRINGS, strings);
  }

  @Test
  public void addExistingItemTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new HashSet<>(STRINGS));
    List<String> added = addedList(strings);

    assertFalse(strings.add(ONE));

    assertEquals(asList(), added);
    assertEquals(STRINGS, strings);
  }

  @Test
  public void removeItemTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new HashSet<>(STRINGS));
    Set<String> removed = removedSet(strings);

    assertTrue(strings.remove(ONE));

    assertEquals(new HashSet<>(asList(ONE)), removed);
  }

  @Test
  public void removeMissingItemTest() {
    ObservableSet<String> strings = new ObservableSetDecorator<>(new HashSet<>(STRINGS));
    List<String> removed = removedList(strings);

    assertFalse(strings.remove(FOUR));

    assertEquals(asList(), removed);
  }

  @Test
  public void addToParentScope() {
    ScopedObservableSet<String> strings = new ScopedObservableSet<>(HashSet::new);
    ScopedObservableSet<String> stringsChildren = strings.nestChildScope();
    Set<String> added = addedSet(stringsChildren);

    assertTrue(strings.addAll(STRINGS));

    assertEquals(STRINGS, added);

    assertTrue(stringsChildren.containsAll(STRINGS));
  }

  @Test
  public void addToChildScope() {
    ScopedObservableSet<String> strings = new ScopedObservableSet<>(HashSet::new);
    ScopedObservableSet<String> stringsChildren = strings.nestChildScope();
    Set<String> added = addedSet(strings);

    assertTrue(stringsChildren.addAll(STRINGS));

    assertEquals(emptySet(), added);
  }

  @Test
  public void promoteToParentScope() {
    ScopedObservableSet<String> strings = new ScopedObservableSet<>(HashSet::new);
    ScopedObservableSet<String> stringsChildren = strings.nestChildScope();

    // add all to children and assert status
    assertTrue(stringsChildren.addAll(STRINGS));

    assertEquals(STRINGS, stringsChildren);
    assertEquals(emptySet(), strings);

    // listen for changes in the child
    List<String> added = addedList(stringsChildren);
    List<String> removed = addedList(stringsChildren);

    // promote to parent and assert status
    assertTrue(strings.addAll(STRINGS));

    assertEquals(STRINGS, stringsChildren);
    assertEquals(STRINGS, strings);

    assertTrue(added.isEmpty());
    assertTrue(removed.isEmpty());

    assertFalse(stringsChildren.localIterator().hasNext());
  }
}
