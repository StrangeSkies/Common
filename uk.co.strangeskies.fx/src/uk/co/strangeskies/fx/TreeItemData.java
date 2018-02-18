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

import java.util.Optional;
import java.util.stream.Stream;

import javafx.scene.control.TreeItem;
import uk.co.strangeskies.reflection.token.TypeToken;
import uk.co.strangeskies.reflection.token.TypedObject;

/**
 * This interface defines the type of a {@link TreeItem} for a
 * {@link ModularTreeView}. It provides access to the actual typed data of each
 * node, as well as to the contributions which apply to that node, and the
 * {@link TreeItemData item data} of the parent.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the data
 */
public interface TreeItemData<T> {
	/**
	 * @return the typed data of a tree node
	 */
	TypedObject<T> typedData();

	/**
	 * @return the actual data of a tree node
	 */
	default T data() {
		return typedData().getObject();
	}

	/**
	 * @return the type of the actual data of a tree node
	 */
	default TypeToken<?> type() {
		return typedData().getTypeToken();
	}

	/**
	 * @return a {@link TreeItemData} interface over the parent node
	 */
	Optional<TreeItemData<?>> parent();

	/**
	 * @return the tree view which contains this item
	 */
	ModularTreeView treeView();

	/**
	 * Get all the contributions which should be applied to a tree item, in order
	 * from most to least specific.
	 * 
	 * @return the contributions which apply to this tree item
	 */
	Stream<TreeContribution<? super T>> contributions();

	/**
	 * Get all contributions which should be applied to a tree item and which
	 * match a given type, in order from most to least specific.
	 * 
	 * @param <U>
	 *          the type of contribution
	 * @param type
	 *          the type of contribution
	 * @return the matching contributions which apply to this tree item
	 */
	<U extends TreeContribution<? super T>> Stream<U> contributions(TypeToken<U> type);

	/**
	 * Refresh the tree cell associated with this tree item.
	 * 
	 * @param recursive
	 *          true if the item's children should also be refreshed recursively,
	 *          false otherwise
	 */
	void refresh(boolean recursive);
}
