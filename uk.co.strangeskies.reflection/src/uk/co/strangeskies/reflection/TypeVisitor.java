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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A visitor to allow types to be dealt with as their specific class without the
 * need for manual type checking and casting. Types can be visited recursively
 * by calling {@link TypeVisitor#visit(Collection)} or
 * {@link TypeVisitor#visit(Type...)} within each implementation of the abstract
 * visiting methods.
 * 
 * @author Elias N Vasylenko
 */
public abstract class TypeVisitor {
	private final boolean allowRepeatVisits;
	private final Set<Type> visited = new HashSet<>();

	/**
	 * Instantiate a new TypeVisitor which does not allow repeat visits.
	 */
	public TypeVisitor() {
		this(false);
	}

	/**
	 * Instantiate a new TypeVisitor.
	 * 
	 * @param allowRepeatVisits
	 *          If this is true, types which are encountered multiple times will
	 *          be visited each time they are encountered, otherwise they will
	 *          only be visited the first time they are encountered.
	 */
	public TypeVisitor(boolean allowRepeatVisits) {
		this.allowRepeatVisits = allowRepeatVisits;
	}

	/**
	 * Visit a given type by passing it to the appropriate visitation methods.
	 * 
	 * @param type
	 *          The type to visit.
	 */
	public synchronized final void visit(Type type) {
		if (visited.add(type)) {
			if (type instanceof Class)
				visitClass((Class<?>) type);
			else if (type instanceof ParameterizedType)
				visitParameterizedType((ParameterizedType) type);
			else if (type instanceof GenericArrayType)
				visitGenericArrayType((GenericArrayType) type);
			else if (type instanceof WildcardType)
				visitWildcardType((WildcardType) type);
			else if (type instanceof IntersectionType)
				visitIntersectionType((IntersectionType) type);
			else if (type instanceof TypeVariableCapture)
				visitTypeVariableCapture((TypeVariableCapture) type);
			else if (type instanceof TypeVariable)
				visitTypeVariable((TypeVariable<?>) type);
			else if (type instanceof InferenceVariable)
				visitInferenceVariable((InferenceVariable) type);
			else if (type == null)
				visitNull();
			else
				throw new AssertionError("Unknown type: " + type + " of class " + type.getClass());

			if (allowRepeatVisits)
				visited.remove(type);
		}
	}

	/**
	 * Visit each type in a given collection, passing them to the appropriate
	 * member methods as encountered.
	 * 
	 * @param types
	 *          The collection of types to visit.
	 */
	public synchronized void visit(Type... types) {
		for (Type type : types) {
			visit(type);
		}
	}

	/**
	 * Visit each type in a given collection, passing them to the appropriate
	 * member methods as encountered.
	 * 
	 * @param types
	 *          The collection of types to visit.
	 */
	public synchronized void visit(Collection<? extends Type> types) {
		for (Type type : types) {
			visit(type);
		}
	}

	protected void visitNull() {}

	protected void visitClass(Class<?> type) {}

	protected void visitParameterizedType(ParameterizedType type) {}

	protected void visitGenericArrayType(GenericArrayType type) {}

	protected void visitWildcardType(WildcardType type) {}

	protected void visitTypeVariableCapture(TypeVariableCapture type) {}

	protected void visitTypeVariable(TypeVariable<?> type) {}

	protected void visitInferenceVariable(InferenceVariable type) {}

	protected void visitIntersectionType(IntersectionType type) {}
}
