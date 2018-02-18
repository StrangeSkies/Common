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

import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Type.getInternalName;
import static uk.co.strangeskies.reflection.codegen.ClassWritingContext.visitTypeSignature;
import static uk.co.strangeskies.reflection.codegen.CodeGenerationException.CODEGEN_PROPERTIES;

import java.lang.reflect.Executable;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureWriter;

import uk.co.strangeskies.reflection.Types;

public class MethodDeclaration<C, T> extends ParameterizedDeclaration<ExecutableSignature<?>> {
  enum Kind {
    CONSTRUCTOR, INSTANCE_METHOD, STATIC_METHOD
  }

  private static final ExecutableSignature<?> VOID_SIGNATURE = new ConstructorSignature();
  private static final String VOID_DESCRIPTOR = getMethodDescriptor(VOID_SIGNATURE);
  private static final String STUB_INVOCATION_ERROR = getInternalName(StubInvocationError.class);

  private final String name;
  private final String descriptor;
  private final Kind kind;

  private final ClassDeclaration<?, ?> declaringClass;
  private final ClassDeclaration<?, C> owningDeclaration;

  private Executable executableStub;

  protected MethodDeclaration(
      Kind kind,
      ClassDeclaration<?, ?> declaringClass,
      ClassDeclaration<?, C> owningDeclaration,
      ExecutableSignature<?> signature,
      ClassWriter classWriter) {
    this(kind, declaringClass, owningDeclaration, signature, classWriter, new SignatureWriter());
  }

  protected MethodDeclaration(
      Kind kind,
      ClassDeclaration<?, ?> declaringClass,
      ClassDeclaration<?, C> owningDeclaration,
      ExecutableSignature<?> signature,
      ClassWriter classWriter,
      SignatureWriter signatureWriter) {
    super(signature, signatureWriter);

    this.kind = kind;
    this.name = signature.getName();
    this.declaringClass = declaringClass;
    this.owningDeclaration = owningDeclaration;
    this.descriptor = getMethodDescriptor(signature);

    String typeSignature = writeGenericParameters(signatureWriter).toString();

    MethodVisitor methodVisitor = classWriter.visitMethod(
        signature.getModifiers().toInt(),
        signature.getName(),
        descriptor,
        typeSignature,
        null);

    methodVisitor.visitCode();
    methodVisitor.visitTypeInsn(NEW, STUB_INVOCATION_ERROR);
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitMethodInsn(
        INVOKESPECIAL,
        STUB_INVOCATION_ERROR,
        VOID_SIGNATURE.getName(),
        VOID_DESCRIPTOR,
        false);
    methodVisitor.visitInsn(ATHROW);
    methodVisitor.visitMaxs(2, 1);
    methodVisitor.visitEnd();
  }

  private SignatureWriter writeGenericParameters(SignatureWriter signatureWriter) {
    getSignature().getParameters().forEach(parameter -> {
      visitTypeSignature(signatureWriter.visitParameterType(), parameter.getType().getType());
    });
    visitTypeSignature(signatureWriter.visitReturnType(), getSignature().getReturnType().getType());

    return signatureWriter;
  }

  protected static String getMethodDescriptor(ExecutableSignature<?> signature) {
    return Type.getMethodDescriptor(
        Type.getType(Types.getErasedType(signature.getReturnType().getType())),
        signature.getParameters().map(ParameterSignature::getErasedType).map(Type::getType).toArray(
            Type[]::new));
  }

  protected static <C, T> MethodDeclaration<C, T> declareConstructor(
      ClassDeclaration<C, T> classDeclaration,
      ConstructorSignature signature,
      ClassWriter writer) {
    return new MethodDeclaration<>(
        Kind.CONSTRUCTOR,
        classDeclaration,
        classDeclaration.getEnclosingClassDeclaration(),
        signature,
        writer);
  }

  protected static <C, T> MethodDeclaration<C, T> declareStaticMethod(
      ClassDeclaration<C, ?> classDeclaration,
      MethodSignature<T> signature,
      ClassWriter writer) {
    if (signature.getModifiers().isDefault())
      throw new CodeGenerationException(CODEGEN_PROPERTIES.staticMethodCannotBeDefault(signature));

    return new MethodDeclaration<>(
        Kind.STATIC_METHOD,
        classDeclaration,
        classDeclaration.getEnclosingClassDeclaration(),
        signature,
        writer);
  }

  protected static <C, T> MethodDeclaration<C, T> declareMethod(
      ClassDeclaration<?, C> classDeclaration,
      MethodSignature<T> signature,
      ClassWriter writer) {
    return new MethodDeclaration<>(
        Kind.INSTANCE_METHOD,
        classDeclaration,
        classDeclaration,
        signature,
        writer);
  }

  public ClassDeclaration<?, ?> getDeclaringClass() {
    return declaringClass;
  }

  public ClassDeclaration<?, C> getOwningDeclaration() {
    return owningDeclaration;
  }

  public Executable getExecutableStub() {
    if (executableStub == null) {
      Class<?>[] erasedParameters = getSignature()
          .getParameters()
          .map(ParameterSignature::getErasedType)
          .toArray(Class<?>[]::new);
      try {
        executableStub = getKind() == Kind.CONSTRUCTOR
            ? getDeclaringClass().getStubClass().getConstructor(erasedParameters)
            : getDeclaringClass().getStubClass().getMethod(name, erasedParameters);
      } catch (NoSuchMethodException | SecurityException e) {
        throw new AssertionError(e);
      }
    }

    return executableStub;
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return kind;
  }

  @Override
  public String toString() {
    return getExecutableStub().toGenericString();
  }
}
