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

import org.junit.Assert;
import org.junit.Test;

import uk.co.strangeskies.property.IdentityProperty;

/**
 * It is very difficult or impossible to test this class properly, as the
 * important aspects of its behavior either only occur in extremely rare
 * circumstances, or change slightly with implementation details of the VM, or
 * both.
 * 
 * @author Elias N Vasylenko
 *
 */
public class IdentityPropertyTest {
	/**
	 * Confirm default constructor initializes with null value
	 */
	@Test
	public void testNullInitialisation() {
		Assert.assertEquals(null, new IdentityProperty<>().get());
	}

	/**
	 * Confirm value constructor initializes properly
	 */
	@Test
	public void testValueInitialisation() {
		Assert.assertEquals("value", new IdentityProperty<>("value").get());
	}

	/**
	 * Confirm property setting works
	 */
	@Test
	public void testSet() {
		IdentityProperty<String> property = new IdentityProperty<>();
		property.set("set");
		Assert.assertEquals("set", property.get());
	}

	/**
	 * Confirm property setting works multiple times
	 */
	@Test
	public void testMultipleSet() {
		IdentityProperty<String> property = new IdentityProperty<>();
		property.set("set");
		property.set("again");
		Assert.assertEquals("again", property.get());
	}
}
