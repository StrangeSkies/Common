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
 * This file is part of uk.co.strangeskies.eclipse.
 *
 * uk.co.strangeskies.eclipse is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.eclipse is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.eclipse;

import uk.co.strangeskies.fx.ModularTreeView;
import uk.co.strangeskies.fx.TreeContribution;

/**
 * A {@link TreeContribution contribution} for the
 * {@link EclipseModularTreeController eclipse modular tree controller}.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of data item to apply to
 */
public interface EclipseTreeContribution<T> extends TreeContribution<T> {
	/**
	 * @param treeId
	 *          the id of the tree to fetch appropriate contribution classes for
	 * @return true if the contribution should apply to a tree of the given ID,
	 *         false otherwise.
	 */
	default boolean appliesToTree(String treeId) {
		return true;
	}

	/**
	 * @return the id of the contribution, available so that any modular tree
	 *         controller contributed to may filter accepted contributions over it
	 */
	default String getContributionId() {
		return getClass().getName();
	}

	/**
	 * @return the ranking of the contribution, available for any modular tree
	 *         controller contributed to to determine
	 *         {@link ModularTreeView#getPrecedence() precedence}
	 */
	default int getContributionRanking() {
		return 0;
	}
}
