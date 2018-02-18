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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for property accessor interfaces, and for methods on those
 * interfaces, to specify how property resources should be fetched to back the
 * implementation of the accessor.
 * 
 * @author Elias N Vasylenko
 */
@Target({ METHOD, TYPE })
@Retention(RUNTIME)
public @interface PropertyConfiguration {
	/**
	 * Property key string case setting.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum KeyCase {
		/**
		 * All upper case.
		 */
		UPPER,

		/**
		 * All lower case.
		 */
		LOWER,

		/**
		 * Preserve original case.
		 */
		PRESERVED,

		/**
		 * Inherit the case settings, or {@link #PRESERVED} by default.
		 */
		UNSPECIFIED
	}

	/**
	 * Property value requirement setting.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum Evaluation {
		/**
		 * Verify that a value is available for the property in the root locale
		 * during construction of a properties interface instance, and fail the
		 * request with an exception if it is not.
		 */
		IMMEDIATE,

		/**
		 * Verify that a value is available for the property in the requested locale
		 * upon invocation of the accessing method of a properties interface instance,
		 * and fail the request with an exception if it is not.
		 */
		DEFERRED,

		/**
		 * Inherit the requirement settings, or {@link #DEFERRED} by default.
		 */
		UNSPECIFIED
	}

	/**
	 * Property value requirement setting.
	 * 
	 * @author Elias N Vasylenko
	 */
	enum Defaults {
		/**
		 * If a default value is available for a property, it should be provided as
		 * if it were successfully fetched.
		 */
		ALLOW,

		/**
		 * If a default value is available for a property, it should be ignored.
		 */
		IGNORE,

		/**
		 * Inherit the requirement settings, or {@link #ALLOW} by default.
		 */
		UNSPECIFIED
	}

	/**
	 * Inherit the resource settings, or apply the default behavior of the
	 * {@link #strategy()} by default.
	 */
	String UNSPECIFIED_RESOURCE = "";

	/**
	 * This is an illegal key string due to the space, and so no accidental
	 * intersection with user defined keys or split strings should be possible.
	 */
	String UNSPECIFIED_KEY = "";

	/**
	 * This is an illegal key string due to the space, and so no accidental
	 * intersection with user defined keys or split strings should be possible.
	 */
	String UNSPECIFIED_KEY_SPLIT_STRING = "!UNSPECIFIED KEY SPLIT STRING!";

	/**
	 * A key formatting string describing the form
	 * {@code [class name].[method name]}.
	 */
	String UNQUALIFIED_DOTTED = "%2$s.%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name].[class name].[method name]}.
	 */
	String QUALIFIED_DOTTED = "%1$s.%2$s.%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [class name]::[method name]}.
	 */
	String UNQUALIFIED_SCOPED = "%2$s::%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name].[class name]::[method name]}.
	 */
	String QUALIFIED_SCOPED = "%1$s.%2$s::%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [class name]/[method name]}.
	 */
	String UNQUALIFIED_SLASHED = "%2$s/%3$s";

	/**
	 * A key formatting string describing the form
	 * {@code [package name]/[class name]/[method name]}.
	 */
	String QUALIFIED_SLASHED = "%1$s/%2$s/%3$s";

	/**
	 * @return the property key format string
	 */
	String key() default UNSPECIFIED_KEY;

	/**
	 * @return the split string to break up camel case when generating the key
	 *         arguments
	 */
	String keySplitString() default UNSPECIFIED_KEY;

	/**
	 * @return the treatment of character case when generating the key arguments
	 */
	KeyCase keyCase() default KeyCase.UNSPECIFIED;

	/**
	 * @return the time a properties availability should be evaluated and its
	 *         requirement fulfilled
	 */
	Evaluation evaluation() default Evaluation.UNSPECIFIED;

	/**
	 * @return whether or not default values should be provided if a value cannot
	 *         otherwise be found
	 */
	Defaults defaults() default Defaults.UNSPECIFIED;

	/**
	 * @return the resource locations for a set of localization strings, in
	 *         priority order
	 */
	String resource() default UNSPECIFIED_RESOURCE;

	/**
	 * The class of the {@link PropertyResource} which implements or specifies the
	 * strategy by which to resolve the localization resources.
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends PropertyResourceStrategy> strategy() default PropertyResourceStrategy.class;
}
