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
 * This file is part of uk.co.strangeskies.reflection.codegen.
 *
 * uk.co.strangeskies.reflection.codegen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.codegen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.codegen;

import static java.util.Objects.hash;

import java.lang.reflect.Modifier;

import uk.co.strangeskies.reflection.Visibility;

public class Modifiers {
	private final int modifiers;
	private final boolean isDefault;

	private Modifiers() {
		modifiers = 0;
		isDefault = false;
	}

	public Modifiers(int modifiers, boolean isDefault) {
		this.modifiers = modifiers;
		this.isDefault = isDefault;
	}

	public static Modifiers modifiers(int modifiers) {
		return new Modifiers(modifiers, false);
	}

	public static Modifiers emptyModifiers() {
		return new Modifiers();
	}

	private Modifiers with(int modifier, boolean present) {
		if (present) {
			return new Modifiers(modifiers | modifier, isDefault);
		} else {
			return new Modifiers(modifiers & ~modifier, isDefault);
		}
	}

	public Visibility getVisibility() {
		return Visibility.forModifiers(modifiers);
	}

	public Modifiers withVisibility(Visibility visibility) {
		return new Modifiers(visibility.withModifiers(modifiers), isDefault);
	}

	public boolean isStatic() {
		return Modifier.isStatic(modifiers);
	}

	public Modifiers withStatic(boolean isStatic) {
		return with(Modifier.STATIC, isStatic);
	}

	public boolean isFinal() {
		return Modifier.isFinal(modifiers);
	}

	public Modifiers withFinal(boolean isFinal) {
		return with(Modifier.FINAL, isFinal);
	}

	public boolean isTransient() {
		return Modifier.isTransient(modifiers);
	}

	public Modifiers withTransient(boolean isTransient) {
		return with(Modifier.TRANSIENT, isTransient);
	}

	public boolean isVolatile() {
		return Modifier.isVolatile(modifiers);
	}

	public Modifiers withVolatile(boolean isVolatile) {
		return with(Modifier.VOLATILE, isVolatile);
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(modifiers);
	}

	public Modifiers withAbstract(boolean isAbstract) {
		return with(Modifier.ABSTRACT, isAbstract);
	}

	public boolean isSynchronized() {
		return Modifier.isSynchronized(modifiers);
	}

	public Modifiers withSynchronized(boolean isSynchronized) {
		return with(Modifier.SYNCHRONIZED, isSynchronized);
	}

	public boolean isStrict() {
		return Modifier.isStrict(modifiers);
	}

	public Modifiers withStrict(boolean isStrict) {
		return with(Modifier.STRICT, isStrict);
	}

	public boolean isNative() {
		return Modifier.isNative(modifiers);
	}

	public Modifiers withNative(boolean isNative) {
		return with(Modifier.NATIVE, isNative);
	}

	public boolean isInterface() {
		return Modifier.isInterface(modifiers);
	}

	public Modifiers withInterface(boolean isInterface) {
		return with(Modifier.INTERFACE, isInterface);
	}

	public boolean isDefault() {
		return isDefault;
	}

	public Modifiers withDefault(boolean isDefault) {
		return new Modifiers(modifiers, isDefault);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Modifiers))
			return false;

		Modifiers that = (Modifiers) obj;

		return this.isDefault == that.isDefault && this.modifiers == that.modifiers;
	}

	@Override
	public int hashCode() {
		return hash(isDefault, modifiers);
	}

	public int toInt() {
		return modifiers;
	}
}
