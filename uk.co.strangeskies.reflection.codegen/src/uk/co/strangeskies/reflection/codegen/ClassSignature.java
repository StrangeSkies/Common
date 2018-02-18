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

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;
import static uk.co.strangeskies.reflection.codegen.ConstructorSignature.constructorSignature;
import static uk.co.strangeskies.reflection.codegen.MethodSignature.methodSignature;
import static uk.co.strangeskies.reflection.codegen.Modifiers.emptyModifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.Types;
import uk.co.strangeskies.reflection.Visibility;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * Separating the logic for declaring the class into a builder allows us to
 * ensure the type of the class is immutable once an actual
 * {@link ClassDefinition} object is instantiated. This means that the type can
 * be safely reasoned about before any class members are defined or any
 * implementation details are specified.
 * 
 * <p>
 * A class signature contains a number of {@link MethodSignature method
 * signatures}. Some of these signatures may be implied by inheritance, and some
 * may be explicitly attached. When a class signature is declared, each of those
 * signatures is realized into a corresponding {@link MethodDeclaration method
 * declaration}.
 * 
 * @author Elias N Vasylenko
 * 
 * @param <T>
 *          the intersection of the supertypes of the described class
 */
public class ClassSignature<T> implements ParameterizedSignature<ClassSignature<T>> {
	private final String packageName;
	private final String simpleName;
	private final String enclosingClassName;

	private final AnnotatedType superClass;
	private final Set<AnnotatedType> superInterfaces;

	private final Set<ConstructorSignature> constructorSignatures;
	private final Set<MethodSignature<?>> methodSignatures;
	private final List<TypeVariableSignature> typeVariables;
	private final Set<Annotation> annotations;

	private final Modifiers modifiers;

	protected ClassSignature() {
		this(
				null,
				null,
				null,
				null,
				emptySet(),
				emptySet(),
				emptySet(),
				emptyList(),
				emptySet(),
				emptyModifiers());
	}

	protected ClassSignature(
			String packageName,
			String simpleName,
			String enclosingClassName,
			AnnotatedType superClass,
			Set<AnnotatedType> superInterfaces,
			Set<ConstructorSignature> constructorSignatures,
			Set<MethodSignature<?>> methodSignatures,
			List<TypeVariableSignature> typeVariables,
			Set<Annotation> annotations,
			Modifiers modifiers) {
		this.packageName = packageName;
		this.simpleName = simpleName;
		this.enclosingClassName = enclosingClassName;
		this.superClass = superClass;
		this.superInterfaces = superInterfaces;
		this.constructorSignatures = constructorSignatures;
		this.methodSignatures = methodSignatures;
		this.typeVariables = typeVariables;
		this.annotations = annotations;
		this.modifiers = modifiers;
	}

	public static ClassSignature<Object> classSignature() {
		return new ClassSignature<>();
	}

	/**
	 * Create the class signature describing the given class.
	 * 
	 * @param clazz
	 *          the class whose signature we wish to create
	 * @return a signature describing the given class
	 */
	@SuppressWarnings("unchecked")
	public static <T> ClassSignature<T> classSignature(Class<T> clazz) {
		ClassSignature<?> classSignature = new ClassSignature<>()
				.packageName(clazz.getPackage().getName())
				.simpleName(clazz.getSimpleName())
				.enclosingClassName(clazz.getEnclosingClass().getName())
				.annotated(clazz.getDeclaredAnnotations())
				.typeVariables(
						stream(clazz.getTypeParameters())
								.map(TypeVariableSignature::typeVariableSignature)
								.collect(toList()));

		List<AnnotatedType> superType = new ArrayList<>(clazz.getInterfaces().length);
		if (clazz.getSuperclass() != null) {
			superType.add(clazz.getAnnotatedSuperclass());
		}
		stream(clazz.getAnnotatedInterfaces()).forEach(superType::add);
		classSignature = classSignature.extending(superType);

		for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			classSignature = classSignature.constructor(constructorSignature(constructor));
		}
		for (Method method : clazz.getDeclaredMethods()) {
			classSignature = classSignature.method(methodSignature(method));
		}

		return (ClassSignature<T>) classSignature;
	}

	protected String getClassName() {
		StringBuilder builder = new StringBuilder();

		getPackageName().ifPresent(p -> builder.append(p).append('.'));

		getEnclosingClassName().ifPresent(e -> builder.append(e).append('.'));

		return builder.append(getSimpleName()).toString();
	}

	protected Optional<String> getPackageName() {
		return Optional.ofNullable(packageName);
	}

	public ClassSignature<T> packageName(String packageName) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				null,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public ClassSignature<T> packageName(Function<String, String> packageNameTransformation) {
		return new ClassSignature<>(
				packageNameTransformation.apply(packageName),
				simpleName,
				null,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	protected String getSimpleName() {
		return simpleName;
	}

	public ClassSignature<T> simpleName(String simpleName) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public ClassSignature<T> simpleName(Function<String, String> simpleNameTransformation) {
		return new ClassSignature<>(
				packageName,
				simpleNameTransformation.apply(simpleName),
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public Modifiers getModifiers() {
		return modifiers;
	}

	protected ClassSignature<T> withModifiers(Modifiers modifiers) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public ClassSignature<T> withVisibility(Visibility visibility) {
		return withModifiers(modifiers.withVisibility(visibility));
	}

	public Optional<String> getEnclosingClassName() {
		return Optional.ofNullable(enclosingClassName);
	}

	public ClassSignature<T> enclosingClassName(String enclosingClassName) {
		return new ClassSignature<>(
				null,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	protected Optional<? extends AnnotatedType> getSuperClass() {
		return Optional.ofNullable(superClass);
	}

	protected Stream<? extends AnnotatedType> getSuperInterfaces() {
		return superInterfaces.stream();
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> extending(Type... superType) {
		return extending(
				Arrays.stream(superType).map(AnnotatedTypes::annotated).collect(Collectors.toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> extending(AnnotatedType... superType) {
		return extending(Arrays.asList(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public <U> ClassSignature<? extends U> extending(Class<U> superType) {
		return extending(TypeToken.forClass(superType));
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SuppressWarnings("unchecked")
	public <U> ClassSignature<? extends U> extending(TypeToken<U> superType) {
		return (ClassSignature<U>) extending(superType.getAnnotatedDeclaration());
	}

	/**
	 * @param <U>
	 *          the supertype for the class signature
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	@SafeVarargs
	@SuppressWarnings("unchecked")
	public final <U> ClassSignature<? extends U> extending(TypeToken<? extends U>... superType) {
		return (ClassSignature<U>) extending(
				stream(superType).map(TypeToken::getAnnotatedDeclaration).collect(toList()));
	}

	/**
	 * @param superType
	 *          the supertype for the class signature
	 * @return the receiver
	 */
	public ClassSignature<?> extending(Collection<? extends AnnotatedType> superType) {
		Set<AnnotatedType> superInterfaces = new HashSet<>(superType.size());
		AnnotatedType superClass = null;

		for (AnnotatedType type : superType) {
			if (Types.getErasedType(type.getType()).isInterface()) {
				superInterfaces.add(type);
			} else if (superClass == null) {
				superClass = type;
			} else {
				throw new CodeGenerationException(
						CODEGEN_PROPERTIES.cannotExtendMultipleClassTypes(superClass, type));
			}
		}

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public Stream<? extends ConstructorSignature> getConstructors() {
		return constructorSignatures.stream();
	}

	public ClassSignature<T> constructor(ConstructorSignature constructorSignature) {
		HashSet<ConstructorSignature> constructorSignatures = new HashSet<>(this.constructorSignatures);
		constructorSignatures.add(constructorSignature);

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	public Stream<? extends MethodSignature<?>> getMethods() {
		return methodSignatures.stream();
	}

	public ClassSignature<T> method(MethodSignature<?> methodSignature) {
		HashSet<MethodSignature<?>> methodSignatures = new HashSet<>(this.methodSignatures);
		methodSignatures.add(methodSignature);

		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				annotations,
				modifiers);
	}

	@Override
	public Stream<? extends Annotation> getAnnotations() {
		return annotations.stream();
	}

	@Override
	public ClassSignature<T> annotated(Collection<? extends Annotation> annotations) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				typeVariables,
				new HashSet<>(annotations),
				modifiers);
	}

	@Override
	public Stream<? extends TypeVariableSignature> getTypeVariables() {
		return typeVariables.stream();
	}

	@Override
	public ClassSignature<T> typeVariables(
			Collection<? extends TypeVariableSignature> typeVariables) {
		return new ClassSignature<>(
				packageName,
				simpleName,
				enclosingClassName,
				superClass,
				superInterfaces,
				constructorSignatures,
				methodSignatures,
				new ArrayList<>(typeVariables),
				annotations,
				modifiers);
	}
}
