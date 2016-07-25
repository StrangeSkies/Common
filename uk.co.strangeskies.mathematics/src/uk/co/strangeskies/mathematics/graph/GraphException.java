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
package uk.co.strangeskies.mathematics.graph;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.util.function.Function;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.LocalizedRuntimeException;

public class GraphException extends LocalizedRuntimeException {
	public GraphException(Localized<String> message) {
		super(message);
	}

	public GraphException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	public GraphException(Function<GraphProperties, Localized<String>> message) {
		this(message.apply(getDefaultProperties(GraphProperties.class)));
	}

	public GraphException(Function<GraphProperties, Localized<String>> message, Throwable cause) {
		this(message.apply(getDefaultProperties(GraphProperties.class)), cause);
	}
}
