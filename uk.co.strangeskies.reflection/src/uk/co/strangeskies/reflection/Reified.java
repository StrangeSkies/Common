/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@strangeskies.co.uk>
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

import uk.co.strangeskies.utilities.Self;

/**
 * A type whose instances know their own type, including any available generic
 * information.
 * <p>
 * As with the extended {@link Self} interface, generally only the most specific
 * <em>useful</em> type will be considered.
 * 
 * @author Elias N Vasylenko
 */
public interface Reified {
	/**
	 * @return The <em>accurate</em> type of the instance, accounting for
	 *         generics. It is up to the implementation whether the returned type
	 *         is also completely <em>precise</em> - in other words, the type may
	 *         mention wildcards or inference variables if the exact type cannot
	 *         be determined, and may not reflect the most specific possible
	 *         class.
	 */
	TypeToken<?> getThisType();

	/**
	 * @return this object as a {@link TypedObject}
	 */
	default TypedObject<?> asTypedObject() {
		return TypedObject.castUnsafe(getThisType(), this);
	}
}
