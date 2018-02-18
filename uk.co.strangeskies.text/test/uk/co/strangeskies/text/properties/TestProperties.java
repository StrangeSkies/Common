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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import static uk.co.strangeskies.text.properties.PropertyConfiguration.UNQUALIFIED_SLASHED;
import static uk.co.strangeskies.text.properties.PropertyConfiguration.KeyCase.LOWER;

import java.util.List;
import java.util.Optional;

import uk.co.strangeskies.text.properties.PropertyConfiguration.Defaults;
import uk.co.strangeskies.text.properties.PropertyConfiguration.Evaluation;

@SuppressWarnings("javadoc")
@PropertyConfiguration(key = UNQUALIFIED_SLASHED, keySplitString = ".", keyCase = LOWER)
public interface TestProperties {
	String simple();

	String substitution(String item);

	String multipleSubstitution(String first, String second);

	default String defaultMethod() {
		return substitution("default");
	}

	@Nested
	NestedTestProperties nesting();

	Optional<String> optional();

	Optional<String> optionalMissing();

	List<String> list();

	@PropertyConfiguration(defaults = Defaults.IGNORE)
	String requiredProperty();

	@PropertyConfiguration(evaluation = Evaluation.IMMEDIATE)
	String immediateDefaultingProperty();

	ImmediateRequirementTestProperties immediateRequirements();
}
