/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
 *
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.values;

import java.util.function.Function;

import uk.co.strangeskies.utilities.Factory;

public class NumberToValueFunction<V extends Value<V>> implements
		Function<Number, V> {
	private final Factory<V> valueFactory;

	public <X extends V> NumberToValueFunction(Factory<V> valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public V apply(Number input) {
		return valueFactory.create().setValue(input);
	}
}
