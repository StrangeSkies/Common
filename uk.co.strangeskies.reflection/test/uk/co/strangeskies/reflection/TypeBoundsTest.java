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
 * This file is part of uk.co.strangeskies.reflection.
 *
 * uk.co.strangeskies.reflection is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

interface StandAloneClass {}

interface SimpleSubclassA extends StandAloneClass {}

interface SimpleSubclassB extends StandAloneClass {}

interface SelfBound<T extends SelfBound<T>> {}

interface Tag {}

interface SelfBoundedA extends SelfBound<SelfBoundedA> {}

interface SelfBoundedB extends SelfBound<SelfBoundedB> {}

interface SelfBoundedMultiplyInheritingA extends SelfBound<SelfBoundedMultiplyInheritingA>, Tag {}

interface SelfBoundedMultiplyInheritingB extends SelfBound<SelfBoundedMultiplyInheritingB>, Tag {}

public class TypeBoundsTest {
  @Test
  public void lubSingleClass() {
    Type lub = new TypeBounds().leastUpperBound(StandAloneClass.class);

    assertThat(lub).isEqualTo(StandAloneClass.class);
  }

  @Test
  public void lubSingleParameterizedType(@Mocked ParameterizedType type) {
    Type lub = new TypeBounds().leastUpperBound(type);

    assertThat(lub).isEqualTo(type);
  }

  @Test
  public void lubDirectSupertypeRecursiveBoundTest() {
    Type lub = new TypeBounds().leastUpperBound(SelfBoundedA.class, SelfBoundedB.class);

    assertThat(lub)
        .as("least upper bound")
        .isInstanceOfSatisfying(ParameterizedType.class, parameterizedType -> {
          assertThat(parameterizedType.getOwnerType()).as("owner type").isNull();
          assertThat(parameterizedType.getRawType()).as("raw type").isEqualTo(SelfBound.class);
          assertThat(asList(parameterizedType.getActualTypeArguments()))
              .as("type arguments")
              .hasSize(1)
              .first()
              .isInstanceOfSatisfying(WildcardType.class, wildcard -> {
                assertThat(wildcard.getLowerBounds()).as("lower bounds").isEmpty();
                assertThat(wildcard.getUpperBounds())
                    .as("upper bounds")
                    .hasSize(1)
                    .allSatisfy(bound -> assertThat(bound).isEqualTo(lub));
              });
        });

    Type lubReorder = new TypeBounds().leastUpperBound(SelfBoundedB.class, SelfBoundedA.class);
    assertThat(lub).isEqualTo(lubReorder);
  }

  @Test
  public void lubDirectSupertypeRecursiveIntersectingBoundTest() {
    Type lub = new TypeBounds()
        .leastUpperBound(
            SelfBoundedMultiplyInheritingA.class,
            SelfBoundedMultiplyInheritingB.class);

    assertThat(lub)
        .as("least upper bound")
        .isInstanceOfSatisfying(IntersectionType.class, intersection -> {
          assertThat(intersection.getTypes())
              .hasSize(2)
              .contains(Tag.class)
              .anySatisfy(
                  element -> assertThat(element)
                      .isInstanceOfSatisfying(ParameterizedType.class, parameterizedType -> {
                        assertThat(parameterizedType.getOwnerType()).as("owner type").isNull();
                        assertThat(parameterizedType.getRawType())
                            .as("raw type")
                            .isEqualTo(SelfBound.class);
                        assertThat(asList(parameterizedType.getActualTypeArguments()))
                            .as("type arguments")
                            .hasSize(1)
                            .first()
                            .isInstanceOfSatisfying(WildcardType.class, wildcard -> {
                              assertThat(wildcard.getLowerBounds()).as("lower bounds").isEmpty();
                              assertThat(wildcard.getUpperBounds())
                                  .as("upper bounds")
                                  .hasSize(2)
                                  .contains(Tag.class)
                                  .anySatisfy(el -> assertThat(el).isEqualTo(element));
                            });
                      }));
        });

    Type lubReorder = new TypeBounds()
        .leastUpperBound(
            SelfBoundedMultiplyInheritingB.class,
            SelfBoundedMultiplyInheritingA.class);
    assertThat(lub).isEqualTo(lubReorder);
  }

  @Test
  public void lubWithContainingWildcard(
      @Mocked ParameterizedType selfBoundType,
      @Mocked WildcardType tagWildcardType) {
    new Expectations() {
      {
        selfBoundType.getRawType();
        result = SelfBound.class;
        selfBoundType.getOwnerType();
        result = null;
        selfBoundType.getActualTypeArguments();
        result = new Type[] { tagWildcardType };

        tagWildcardType.getUpperBounds();
        result = new Type[] { Tag.class };
      }
    };

    Type lub = new TypeBounds()
        .leastUpperBound(
            SelfBoundedMultiplyInheritingA.class,
            SelfBoundedMultiplyInheritingB.class,
            selfBoundType);

    assertThat(lub).isInstanceOfSatisfying(ParameterizedType.class, parameterizedType -> {
      assertThat(parameterizedType.getOwnerType()).as("owner type").isNull();
      assertThat(parameterizedType.getRawType()).as("raw type").isEqualTo(SelfBound.class);
      assertThat(asList(parameterizedType.getActualTypeArguments()))
          .as("type arguments")
          .hasSize(1)
          .first()
          .isInstanceOfSatisfying(WildcardType.class, wildcardType -> {
            assertThat(wildcardType.getUpperBounds()).as("upper bounds").containsExactly(Tag.class);
            assertThat(wildcardType.getLowerBounds()).isEmpty();
          });
    });

    Type lubReorder1 = new TypeBounds()
        .leastUpperBound(
            SelfBoundedMultiplyInheritingA.class,
            selfBoundType,
            SelfBoundedMultiplyInheritingB.class);
    assertThat(lub).isEqualTo(lubReorder1);

    Type lubReorder2 = new TypeBounds()
        .leastUpperBound(
            selfBoundType,
            SelfBoundedMultiplyInheritingA.class,
            SelfBoundedMultiplyInheritingB.class);
    assertThat(lub).isEqualTo(lubReorder2);
  }

  @Test
  public void lcaWildcardsWithUpperBounds(
      @Mocked WildcardType wildcardWithUpperBoundA,
      @Mocked WildcardType wildcardWithUpperBoundB) {
    new Expectations() {
      {
        wildcardWithUpperBoundA.getUpperBounds();
        result = new Type[] { SimpleSubclassA.class };

        wildcardWithUpperBoundB.getUpperBounds();
        result = new Type[] { SimpleSubclassB.class };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithUpperBoundA, wildcardWithUpperBoundB);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds()).isEmpty();
      assertThat(wildcard.getUpperBounds()).containsExactly(StandAloneClass.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(wildcardWithUpperBoundB, wildcardWithUpperBoundA);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaWildcardsWithLowerBounds(
      @Mocked WildcardType wildcardWithLowerBoundA,
      @Mocked WildcardType wildcardWithLowerBoundB) {
    new Expectations() {
      {
        wildcardWithLowerBoundA.getLowerBounds();
        result = new Type[] { SimpleSubclassA.class };

        wildcardWithLowerBoundB.getLowerBounds();
        result = new Type[] { SimpleSubclassB.class };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithLowerBoundA, wildcardWithLowerBoundB);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds())
          .containsExactlyInAnyOrder(SimpleSubclassA.class, SimpleSubclassB.class);
      assertThat(wildcard.getUpperBounds()).containsExactly(Object.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(wildcardWithLowerBoundB, wildcardWithLowerBoundA);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaWildcardsWithUnrelatedUpperAndLowerBounds(
      @Mocked Type boundA,
      @Mocked Type boundB,
      @Mocked WildcardType wildcardWithUpperBound,
      @Mocked WildcardType wildcardWithLowerBound) {
    new Expectations() {
      {
        wildcardWithUpperBound.getUpperBounds();
        result = new Type[] { boundA };

        wildcardWithLowerBound.getUpperBounds();
        result = new Type[] {};
        wildcardWithLowerBound.getLowerBounds();
        result = new Type[] { boundB };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithLowerBound, wildcardWithUpperBound);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds()).isEmpty();
      assertThat(wildcard.getUpperBounds()).containsExactly(Object.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(wildcardWithUpperBound, wildcardWithLowerBound);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaWildcardsWithEqualUpperAndLowerBounds(
      @Mocked Type bound,
      @Mocked WildcardType wildcardWithUpperBound,
      @Mocked WildcardType wildcardWithLowerBound) {
    new Expectations() {
      {
        wildcardWithUpperBound.getUpperBounds();
        result = new Type[] { bound };

        wildcardWithLowerBound.getUpperBounds();
        result = new Type[] {};
        wildcardWithLowerBound.getLowerBounds();
        result = new Type[] { bound };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithLowerBound, wildcardWithUpperBound);

    assertThat(lca).isEqualTo(bound);

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(wildcardWithUpperBound, wildcardWithLowerBound);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaClassAndWildcardWithUpperBound(@Mocked WildcardType wildcardWithUpperBound) {
    new Expectations() {
      {
        wildcardWithUpperBound.getUpperBounds();
        result = new Type[] { SimpleSubclassA.class };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithUpperBound, SimpleSubclassB.class);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds()).isEmpty();
      assertThat(wildcard.getUpperBounds()).containsExactly(StandAloneClass.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(SimpleSubclassB.class, wildcardWithUpperBound);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaClassAndWildcardWithLowerBound(@Mocked WildcardType wildcardWithLowerBound) {
    new Expectations() {
      {
        wildcardWithLowerBound.getUpperBounds();
        result = new Type[] {};
        wildcardWithLowerBound.getLowerBounds();
        result = new Type[] { SimpleSubclassA.class };
      }
    };

    Type lca = new TypeBounds()
        .leastContainingArgument(wildcardWithLowerBound, SimpleSubclassB.class);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds())
          .containsExactlyInAnyOrder(SimpleSubclassA.class, SimpleSubclassB.class);
      assertThat(wildcard.getUpperBounds()).containsExactly(Object.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(SimpleSubclassB.class, wildcardWithLowerBound);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaClassAndUnequalClass() {
    Type lca = new TypeBounds()
        .leastContainingArgument(SimpleSubclassA.class, SimpleSubclassB.class);

    assertThat(lca).isInstanceOfSatisfying(WildcardType.class, wildcard -> {
      assertThat(wildcard.getLowerBounds()).isEmpty();
      assertThat(wildcard.getUpperBounds()).containsExactly(StandAloneClass.class);
    });

    Type lcaReorder = new TypeBounds()
        .leastContainingArgument(SimpleSubclassB.class, SimpleSubclassA.class);
    assertThat(lca).isEqualTo(lcaReorder);
  }

  @Test
  public void lcaClassAndEqualClass() {
    Type lca = new TypeBounds()
        .leastContainingArgument(StandAloneClass.class, StandAloneClass.class);

    assertThat(lca).isEqualTo(StandAloneClass.class);
  }
}
