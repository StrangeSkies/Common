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
 * This file is part of uk.co.strangeskies.osgi.
 *
 * uk.co.strangeskies.osgi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.osgi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.osgi.servicewrapper.impl;

import uk.co.strangeskies.collection.EquivalenceComparator;

class ManagedServiceWrapperComparator extends
		EquivalenceComparator<ManagedServiceWrapper<?>> {
	public ManagedServiceWrapperComparator() {
		super((a, b) -> a == b);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public int compare(ManagedServiceWrapper<?> first,
			ManagedServiceWrapper<?> second) {
		int serviceRankingDifference = first.getServiceRanking()
				- second.getServiceRanking();
		if (serviceRankingDifference != 0) {
			return serviceRankingDifference;
		}

		switch (first.getHideServices()) {
		case ALWAYS:
			switch (second.getHideServices()) {
			case WHEN_WRAPPED:
			case NEVER:
				return 1;
			}
			break;
		case WHEN_WRAPPED:
			switch (second.getHideServices()) {
			case ALWAYS:
				return -1;
			case NEVER:
				return 1;
			}
			break;
		case NEVER:
			switch (second.getHideServices()) {
			case ALWAYS:
			case WHEN_WRAPPED:
				return -1;
			}
			break;
		}

		return super.compare(first, second);
	}
}
