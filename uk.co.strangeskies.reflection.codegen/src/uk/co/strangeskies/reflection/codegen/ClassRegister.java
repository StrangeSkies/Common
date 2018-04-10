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
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;
import static uk.co.strangeskies.reflection.codegen.MethodDeclaration.Kind.CONSTRUCTOR;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loading stub-classes first then overriding with full implementations as a
 * two-step process means we can inject into existing class loaders without any
 * of the circularity restrictions present in other solutions.
 * 
 * Think about how we can achieve clever re-definition abilities here by loading
 * existing classes into the class definition space and then modifying them.
 * Remember that we can then use instrumentation to reload the classes into the
 * same class loader if we really need to.
 * 
 * Load classes into ClassDefinitionSpace so we can reuse their methods:
 * 
 * - ClassDefinition.withMethodsFrom(Class)
 * 
 * - ClassDefinition.withMethodFrom(Class, String)
 * 
 * @author Elias N Vasylenko
 */
public class ClassRegister {
  static class ClassRegistrationContext {
    private final Map<String, ClassDeclaration<?, ?>> classDeclarations;
    private final ByteArrayClassLoader stubClassLoader;

    public ClassRegistrationContext(ClassRegister register) {
      classDeclarations = new HashMap<>(register.classDeclarations);
      stubClassLoader = register.stubClassLoader;
    }

    ClassDeclaration<?, ?> getClassDeclaration(String className) {
      return null /* TODO getClassDeclaration(getClassSignature(className)) */;
    }

    ClassDeclaration<?, ?> getClassDeclaration(ClassSignature<?> signature) {
      return classDeclarations
          .computeIfAbsent(signature.getClassName(), s -> new ClassDeclaration<>(this, signature));
    }

    @SuppressWarnings("unchecked")
    <T> Class<T> loadStubClass(ClassSignature<T> signature, byte[] bytes) {
      return (Class<T>) stubClassLoader.defineClass(signature.getClassName(), bytes);
    }
  }

  private static final Method DEFINE_CLASS_METHOD;
  static {
    try {
      DEFINE_CLASS_METHOD = ClassLoader.class
          .getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
      DEFINE_CLASS_METHOD.setAccessible(true);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new AssertionError(e);
    }
  }

  private final Map<String, ClassDeclaration<?, ?>> classDeclarations;
  private final Map<String, byte[]> classBytecodes;

  private final Map<MethodDeclaration<?, ?>, MethodImplementation<?>> methodDefinitions;
  private final Set<MethodDeclaration<?, ?>> undefinedMethods;
  private final boolean allowPartialImplementation;

  private final ClassLoader classLoader;
  private final ByteArrayClassLoader stubClassLoader;

  public ClassRegister() {
    this(ClassRegister.class.getClassLoader());
  }

  public ClassRegister(ClassLoader classLoader) {
    this.classLoader = classLoader;
    this.stubClassLoader = new ByteArrayClassLoader(classLoader);
    this.classDeclarations = emptyMap();
    this.classBytecodes = emptyMap();
    this.methodDefinitions = emptyMap();
    this.undefinedMethods = emptySet();
    this.allowPartialImplementation = false;
  }

  protected ClassRegister(
      Map<String, ClassDeclaration<?, ?>> classDeclarations,
      Map<String, byte[]> classBytecodes,
      Map<MethodDeclaration<?, ?>, MethodImplementation<?>> methodDefinitions,
      Set<MethodDeclaration<?, ?>> undefinedMethods,
      boolean allowPartialImplementation,
      ClassLoader classLoader,
      ByteArrayClassLoader stubClassLoader) {
    this.classDeclarations = classDeclarations;
    this.classBytecodes = classBytecodes;
    this.methodDefinitions = methodDefinitions;
    this.undefinedMethods = undefinedMethods;
    this.allowPartialImplementation = allowPartialImplementation;
    this.classLoader = classLoader;
    this.stubClassLoader = stubClassLoader;
  }

  @SuppressWarnings("unchecked")
  public <T> ClassDefinition<Void, T> withClassSignature(ClassSignature<T> classSignature) {
    return (ClassDefinition<Void, T>) withClassSignatures(classSignature)
        .getClassDefinition(classSignature)
        .get();
  }

  public ClassRegister withClassSignatures(ClassSignature<?>... classSignatures) {
    if (!isClassOverridingSupported()) {
      stream(classSignatures).forEach(s -> {
        try {
          stubClassLoader.loadClass(s.getClassName());
          throw new CodeGenerationException(
              CODEGEN_PROPERTIES.cannotOverrideExistingClass(s.getClassName()));
        } catch (ClassNotFoundException e) {}
      });
    }

    Map<MethodDeclaration<?, ?>, MethodImplementation<?>> methodDefinitions = new HashMap<>(
        this.methodDefinitions);
    Set<MethodDeclaration<?, ?>> undefinedMethods = new HashSet<>(this.undefinedMethods);

    ClassRegistrationContext context = new ClassRegistrationContext(this);
    stream(classSignatures)
        .forEach(
            signature -> context
                .getClassDeclaration(signature)
                .methodDeclarations()
                .filter(m -> !methodDefinitions.keySet().contains(m))
                .forEach(undefinedMethods::add));

    return new ClassRegister(
        context.classDeclarations,
        classBytecodes,
        methodDefinitions,
        undefinedMethods,
        allowPartialImplementation,
        classLoader,
        stubClassLoader);
  }

  ClassRegister withMethodDefinition(
      MethodDeclaration<?, ?> declaration,
      MethodImplementation<?> definition) {
    Map<MethodDeclaration<?, ?>, MethodImplementation<?>> methodDefinitions = new HashMap<>(
        this.methodDefinitions);
    methodDefinitions.put(declaration, definition);

    Set<MethodDeclaration<?, ?>> undefinedMethods = new HashSet<>(this.undefinedMethods);
    undefinedMethods.remove(declaration);

    return new ClassRegister(
        classDeclarations,
        classBytecodes,
        methodDefinitions,
        undefinedMethods,
        allowPartialImplementation,
        classLoader,
        stubClassLoader);
  }

  public boolean isClassOverridingSupported() {
    return false; // TODO overriding loaded classes using instrumentation
  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  private ClassSignature<?> getClassSignature(String className) {
    /*
     * TODO Auto-generated method stub
     * 
     * replace with something akin to TypeVariableSignature.Reference for Types?
     * 
     * or a more general type look-up thing by string
     */
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> Optional<ClassDefinition<?, T>> getClassDefinition(ClassSignature<T> classSignature) {
    return getClassDefinition(classSignature.getClassName())
        .filter(definition -> definition.getDeclaration().getSignature().equals(classSignature))
        .map(definition -> (ClassDefinition<?, T>) definition);
  }

  public Optional<ClassDefinition<?, ?>> getClassDefinition(String className) {
    return ofNullable(classDeclarations.get(className))
        .map(declaration -> new ClassDefinition<>(declaration, this));
  }

  public void validate() {
    undefinedMethods
        .stream()
        .filter(
            m -> m.getKind().equals(CONSTRUCTOR) || m.getSignature().getModifiers().isStatic()
                || !m.getSignature().getModifiers().isDefault())
        .findAny()
        .ifPresent(m -> {
          throw new CodeGenerationException(CODEGEN_PROPERTIES.mustImplementMethod(m));
        });
  }

  public boolean isFullyImplemented() {
    return undefinedMethods.isEmpty();
  }

  /**
   * Is the class space not {@link #isFullyImplemented() fully implemented}, and
   * is partial implementation {@link #withPartialImplementation(boolean) allowed}
   * and possible.
   * 
   * @return true if the class space is partially implemented, false otherwise
   */
  public boolean isPartiallyImplemented() {
    return allowPartialImplementation && !isFullyImplemented();
  }

  /**
   * {@code partialImplementation} defaults to true.
   *
   * @see #withPartialImplementation(boolean)
   */
  @SuppressWarnings("javadoc")
  public ClassRegister asPartialImplementation() {
    return withPartialImplementation(true);
  }

  /**
   * Derive a class space allowing for partial implementation.
   * 
   * <p>
   * Partial implementation will attempt to still generate valid classes when some
   * method implementations are not provided. This is achieved by providing
   * default implementations to throw an error on invocation.
   * 
   * @param allowPartialImplementation
   *          true if partial implementation should be allowed, false otherwise
   * @return the derived class space
   */
  public ClassRegister withPartialImplementation(boolean allowPartialImplementation) {
    return new ClassRegister(
        classDeclarations,
        classBytecodes,
        methodDefinitions,
        undefinedMethods,
        allowPartialImplementation,
        classLoader,
        stubClassLoader);
  }

  public Map<String, byte[]> generateClasses() {
    return classDeclarations
        .values()
        .stream()
        .collect(
            Collectors
                .toMap(
                    declaration -> declaration.getSignature().getClassName(),
                    declaration -> new ClassDefinition<>(declaration, this).writeClass()));
  }

  /**
   * Generate the classes and load them into the runtime.
   * 
   * @return the class loader containing, or allowing the loading of, the
   *         generated classes
   */
  public ClassLoader loadClasses() {
    Map<String, byte[]> bytecodes = generateClasses();

    if (getClassLoader() instanceof ByteArrayClassLoader) {
      ((ByteArrayClassLoader) classLoader).addClasses(bytecodes);
    } else {
      /*
       * TODO if there are cycles in the class dependency graph and the class loader
       * in not a ByteArrayClassLoader then we must support class overriding for this
       * to work. This should be detected so we can load in the correct order where
       * possible, and throw an exception early where not.
       */
    }

    return classLoader;
  }
}
