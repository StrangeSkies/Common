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

import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * A partial {@link TreeCellContribution} implementation which provides the
 * default behavior of setting the state of the pseudo-class named by
 * {@link #getPseudoClassName(TreeItemData)} to true on applicable tree item
 * nodes.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the tree cell contribution target
 */
public interface PseudoClassTreeCellContribution<T> extends TreeCellContribution<T> {
	@Override
	default <U extends T> Node configureCell(TreeItemData<U> data, Node content) {
		content.pseudoClassStateChanged(PseudoClass.getPseudoClass(getPseudoClassName(data)), true);
		return content;
	}

	/**
	 * @param <U>
	 *          the specific type of the tree item
	 * @param data
	 *          the data contents of the tree item
	 * @return the string for the pseudo-class to apply to the node
	 */
	default <U extends T> String getPseudoClassName(TreeItemData<U> data) {
		return getClass().getSimpleName();
	}
}
