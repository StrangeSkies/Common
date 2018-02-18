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
 * This file is part of uk.co.strangeskies.reflection.token.
 *
 * uk.co.strangeskies.reflection.token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.reflection.token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.reflection.token;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.Visibility;

/**
 * A type safe wrapper around {@link Member} instances, with proper handling of
 * members on generic classes.
 * 
 * <p>
 * {@link MemberToken type members} may be created over types which mention
 * inference variables, or even over inference variables themselves.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the owner type of the member
 * @param <S>
 *          the type of member token
 */
public interface MemberToken<T, S extends MemberToken<T, S>> extends DeclarationToken<S> {
	/**
	 * @return the name of the member
	 */
	String getName();

	/**
	 * @return the actual {@link Member} object backing the {@link MemberToken}.
	 */
	Member getMember();

	/**
	 * @return the inference bounds involved in this {@link MemberToken}
	 */
	BoundSet getBounds();

	/**
	 * @return true if the wrapped member is static, false otherwise
	 */
	default boolean isStatic() {
		return Modifier.isStatic(getMember().getModifiers());
	}

	/**
	 * @return true if the wrapped member is final, false otherwise
	 */

	default boolean isFinal() {
		return Modifier.isFinal(getMember().getModifiers());
	}

	/**
	 * Determine the visibility of the member
	 * 
	 * @return a {@link Visibility} object describing the member
	 */
	default Visibility getVisibility() {
		int modifiers = getMember().getModifiers();

		if (Modifier.isPrivate(modifiers)) {
			return Visibility.PRIVATE;
		} else if (Modifier.isProtected(modifiers)) {
			return Visibility.PROTECTED;
		} else if (Modifier.isPublic(modifiers)) {
			return Visibility.PUBLIC;
		} else {
			return Visibility.PACKAGE_PRIVATE;
		}
	}

	/**
	 * @return The exact declaring class of this member.
	 */
	default Class<?> getDeclaringClass() {
		return getMember().getDeclaringClass();
	}

	/**
	 * This is the exact receiver type which this member should be accessed from
	 * or invoked upon.
	 * <p>
	 * For non-static members, this type will be identical to the
	 * {@link #getOwningDeclaration() owning type}.
	 * <p>
	 * For constructors and static members, if they are declared on a non-static
	 * inner class then the receiver type should be a subtype of the enclosing
	 * class, otherwise the receiver type should be void.
	 * 
	 * @return a type token over the receiver type for invocation or field access,
	 *         or over <code>void.class</code> if the member has no receiver type
	 */
	TypeToken<? super T> getReceiverType();

	/**
	 * Derive a new {@link MemberToken} instance, with the given bounds
	 * incorporated into the bounds of the underlying resolver. The original
	 * {@link MemberToken} will remain unmodified.
	 * 
	 * @param bounds
	 *          the new bounds to incorporate
	 * @return the newly derived {@link MemberToken}
	 */
	S withBounds(BoundSet bounds);

	/**
	 * Derive a new instance of {@link MemberToken} with the given owner type.
	 * 
	 * <p>
	 * The new {@link MemberToken} will always have a owner type which is as or
	 * more specific than both the current receiver type <em>and</em> the given
	 * type. This means that the new owner will be assignment compatible with the
	 * given type, but if the given type contains wildcards or inference variables
	 * which are less specific that those implied by the <em>current</em> receiver
	 * type, new type arguments will be inferred in their place, or further bounds
	 * may be added to them.
	 * 
	 * <p>
	 * If the receiver type is not generic, the method will always return the same
	 * token, or will throw an exception if the given type is not a subtype of the
	 * receiver.
	 * 
	 * <p>
	 * This may result in unsafe transformations when we convert from a raw
	 * receiver to a parameterized receiver, but declarations of those types
	 * should give a raw type warning from the Java compiler and this is
	 * considered sufficient.
	 * 
	 * @param type
	 *          The new owner type. The raw type of this type must be a subtype of
	 *          the raw type of the current receiver type.
	 * @return A new {@link MemberToken} compatible with the given owner type.
	 * 
	 *         <p>
	 *         The new owner type will not be effectively more specific than the
	 *         intersection type of the current owner type and the given type.
	 *         That is, any type which can be assigned to both the given type and
	 *         the current owner type, will also be assignable to the new type.
	 */
	<U> MemberToken<U, ?> withReceiverType(TypeToken<U> type);

	/**
	 * As @see {@link #withReceiverType(TypeToken)}.
	 */
	@SuppressWarnings("javadoc")
  MemberToken<?, ?> withReceiverType(Type type);

	/**
	 * Derived a new {@link MemberToken} instance with all associated generic
	 * parameters inferred.
	 * 
	 * @return the derived {@link MemberToken} with inferred types
	 */
	S resolve();

	/**
	 * @return true if the member is declared on a raw type, false otherwise
	 */
	@Override
	default boolean isRaw() {
		return getOwningDeclaration().map(DeclarationToken::isRaw).get();
	}
}
