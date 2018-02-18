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
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import uk.co.strangeskies.reflection.token.TypeParameter;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * A type of contribution for items in a {@link ModularTreeView}. Tree
 * contributions may optionally extend interfaces such as
 * {@link TreeTextContribution}, {@link TreeCellContribution}, and
 * {@link TreeChildContribution} to provide behavior and information for the
 * tree items which the contribution applies to.
 * 
 * <p>
 * Tree items may apply to all items in a tree of the appropriate
 * {@link #getDataType() type}, and which also satisfy any condition specified
 * via implementation of {@link #appliesTo(TreeItemData)}.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of data item to apply to
 */
public interface TreeContribution<T> {
  /**
   * Get the type of item which the contribution applies to. It is possible to
   * include wildcards in the type, and any tree item whose type is assignable to
   * this type are suitable for application of this contribution.
   * 
   * @return the type of data item to apply to
   */
  default TypeToken<T> getDataType() {
    return TypeToken
        .forClass(getClass())
        .resolveSupertype(TreeContribution.class)
        .resolveTypeArgument(new TypeParameter<T>() {})
        .getTypeToken();
  }

  /**
   * Determine whether the contribution should be applied to the given data item.
   * This method will only be invoked <em>after</em> {@link #getDataType()} has
   * checked against the exact item type.
   * 
   * @param <U>
   *          the specific type of the tree item
   * @param data
   *          a data item in the tree
   * @return true if the contribution is applicable, false otherwise
   */
  default <U extends T> boolean appliesTo(TreeItemData<U> data) {
    return true;
  }
}
