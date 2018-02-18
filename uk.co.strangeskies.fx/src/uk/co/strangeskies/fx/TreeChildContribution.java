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

import java.util.Collection;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * A type of contribution for items in a {@link ModularTreeView}.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the tree item data
 */
public interface TreeChildContribution<T> extends TreeContribution<T> {
	/**
	 * Determine whether children should be contributed to the given data item.
	 * This should given the same result as {@link Collection#isEmpty()} invoked
	 * on the result of {@link #getChildren(TreeItemData)}, but may be more
	 * efficient to implement.
	 * 
	 * @param <U>
	 *          the specific type of the tree item
	 * @param data
	 *          a data item in the tree
	 * @return true if children should be contributed, false otherwise
	 */
	<U extends T> boolean hasChildren(TreeItemData<U> data);

	/**
	 * Determine which children should be contributed to the given data item.
	 * 
	 * @param <U>
	 *          the specific type of the tree item
	 * @param data
	 *          a data item in the tree
	 * @return a list of children to be contributed
	 */
	<U extends T> Stream<TypedObject<?>> getChildren(TreeItemData<U> data);
}
