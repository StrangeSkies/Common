package uk.co.strangeskies.reflection;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Types {
	public static final Map<Class<?>, Class<?>> WRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					put(void.class, Void.class);
					put(boolean.class, Boolean.class);
					put(byte.class, Byte.class);
					put(char.class, Character.class);
					put(short.class, Short.class);
					put(int.class, Integer.class);
					put(long.class, Long.class);
					put(float.class, Float.class);
					put(double.class, Double.class);
				}
			});

	public static final Map<Class<?>, Class<?>> UNWRAPPED_PRIMITIVES = Collections
			.unmodifiableMap(new HashMap<Class<?>, Class<?>>() {
				private static final long serialVersionUID = 1L;
				{
					for (Class<?> primitive : WRAPPED_PRIMITIVES.keySet())
						put(WRAPPED_PRIMITIVES.get(primitive), primitive);
				}
			});

	private Types() {}

	public static Class<?> getRawType(Type type) {
		if (type == null) {
			return null;
		} else if (type instanceof TypeVariable) {
			return getRawType(((TypeVariable<?>) type).getBounds()[0]);
		} else if (type instanceof WildcardType) {
			return getRawType(((WildcardType) type).getUpperBounds()[0]);
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof GenericArrayType) {
			return Array.newInstance(
					(getRawType(((GenericArrayType) type).getGenericComponentType())), 0)
					.getClass();
		}
		throw new IllegalArgumentException("Type of type '" + type
				+ "' is unsupported.");
	}

	public static boolean isPrimitive(Type type) {
		return WRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static boolean isPrimitiveWrapper(Type type) {
		return UNWRAPPED_PRIMITIVES.keySet().contains(getRawType(type));
	}

	public static Type wrap(Type type) {
		if (isPrimitive(type))
			return WRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static Type unwrap(Type type) {
		if (isPrimitiveWrapper(type))
			return UNWRAPPED_PRIMITIVES.get(getRawType(type));
		else
			return type;
	}

	public static boolean isAssignable(Type from, Type to) {
		if (from == null || from.equals(to) || to == null
				|| to.equals(Object.class)) {
			/*
			 * We can always assign to or from 'null', and we can always assign to
			 * Object.
			 */
			return true;
		} else if (to instanceof TypeVariable) {
			/*
			 * We can only assign to a type variable if it is from the exact same
			 * type.
			 */
			return false;
		} else if (from instanceof TypeVariable) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((TypeVariable<?>) from).getBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.of(upperBounds), to);
		} else if (from instanceof IntersectionType) {
			/*
			 * We must be able to assign from at least one member of the intersection
			 * type.
			 */
			return Arrays.stream(((IntersectionType) from).getTypes()).anyMatch(
					f -> isAssignable(f, to));
		} else if (to instanceof IntersectionType) {
			/*
			 * We must be able to assign to each member of the intersection type.
			 */
			return Arrays.stream(((IntersectionType) to).getTypes()).allMatch(
					t -> isAssignable(from, t));
		} else if (from instanceof WildcardType) {
			/*
			 * We must be able to assign from at least one of the upper bound,
			 * including the implied upper bound of Object, to the target type.
			 */
			Type[] upperBounds = ((WildcardType) from).getUpperBounds();
			if (upperBounds.length == 0)
				upperBounds = new Type[] { Object.class };
			return isAssignable(IntersectionType.of(upperBounds), to);
		} else if (to instanceof WildcardType) {
			/*
			 * If there are no lower bounds the target may be arbitrarily specific, so
			 * we can never assign to it. Otherwise we must be able to assign to each
			 * lower bound.
			 */
			Type[] lowerBounds = ((WildcardType) to).getLowerBounds();
			if (lowerBounds.length == 0)
				return false;
			else
				return isAssignable(from, IntersectionType.of(lowerBounds));
		} else if (from instanceof GenericArrayType) {
			GenericArrayType fromArray = (GenericArrayType) from;
			if (to instanceof Class) {
				Class<?> toClass = (Class<?>) to;
				return toClass.isArray()
						&& isAssignable(fromArray.getGenericComponentType(),
								toClass.getComponentType());
			} else if (to instanceof GenericArrayType) {
				GenericArrayType toArray = (GenericArrayType) to;
				return isAssignable(fromArray.getGenericComponentType(),
						toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof GenericArrayType) {
			GenericArrayType toArray = (GenericArrayType) to;
			if (from instanceof Class) {
				Class<?> fromClass = (Class<?>) from;
				return fromClass.isArray()
						&& isAssignable(fromClass.getComponentType(),
								toArray.getGenericComponentType());
			} else
				return false;
		} else if (to instanceof Class) {
			return ((Class<?>) to).isAssignableFrom(getRawType(from));
		} else if (to instanceof ParameterizedType) {
			return false; // TODO
		} else
			return false;
	}

	public static boolean isStrictInvocationContextCompatible(Type from, Type to) {
		if (TypeLiteral.of(from).isPrimitive())
			if (TypeLiteral.of(to).isPrimitive())
				return true; // TODO check widening primitive conversion
			else
				return false;
		else if (TypeLiteral.of(to).isPrimitive())
			return false;
		else
			return isAssignable(from, to);
	}

	public static boolean isLooseInvocationContextCompatible(Type from, Type to) {
		if (isPrimitive(from) && !isPrimitive(to))
			from = wrap(from);
		else if (!isPrimitive(from) && isPrimitive(to))
			from = unwrap(from);
		return isStrictInvocationContextCompatible(from, to);
	}

	public static WildcardType unboundedWildcard() {
		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return new Type[0];
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				return "?";
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return wildcard.getLowerBounds().length == 0
						&& wildcard.getUpperBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	public static WildcardType lowerBoundedWildcard(Type type) {
		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getLowerBounds().length > 0
					|| wildcardType.getUpperBounds().length == 0)
				return wildcardType;
		}
		Supplier<Type[]> types;
		if (type instanceof IntersectionType)
			types = ((IntersectionType) type)::getTypes;
		else
			types = () -> new Type[] { type };
		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return new Type[0];
			}

			@Override
			public Type[] getLowerBounds() {
				return types.get();
			}

			@Override
			public String toString() {
				return "? super "
						+ Arrays.stream(types.get()).map(Object::toString)
								.collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return Arrays.equals(types.get(), wildcard.getLowerBounds())
						&& wildcard.getUpperBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	public static WildcardType upperBoundedWildcard(Type type) {
		if (type instanceof WildcardType) {
			WildcardType wildcardType = ((WildcardType) type);
			if (wildcardType.getUpperBounds().length > 0
					|| wildcardType.getLowerBounds().length == 0)
				return wildcardType;
			else
				return unboundedWildcard();
		}
		Supplier<Type[]> types;
		if (type instanceof IntersectionType)
			types = ((IntersectionType) type)::getTypes;
		else
			types = () -> new Type[] { type };
		return new WildcardType() {
			@Override
			public Type[] getUpperBounds() {
				return types.get();
			}

			@Override
			public Type[] getLowerBounds() {
				return new Type[0];
			}

			@Override
			public String toString() {
				return "? extends "
						+ Arrays.stream(types.get()).map(Object::toString)
								.collect(Collectors.joining(" & "));
			}

			@Override
			public boolean equals(Object that) {
				if (!(that instanceof WildcardType))
					return false;
				if (that == this)
					return true;
				WildcardType wildcard = (WildcardType) that;
				return Arrays.equals(types.get(), wildcard.getUpperBounds())
						&& wildcard.getLowerBounds().length == 0;
			}

			@Override
			public int hashCode() {
				return Arrays.hashCode(getLowerBounds())
						^ Arrays.hashCode(getUpperBounds());
			}
		};
	}

	public static ParameterizedType parameterizedType(Type ownerType,
			Class<?> rawType, Type... typeArguments) {
		return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
	}

	public static ParameterizedType parameterizedType(Class<?> rawClass,
			Map<TypeVariable<?>, Type> typeArguments) {
		return new ParameterizedTypeImpl(
				rawClass.getEnclosingClass() != null ? parameterizedType(
						rawClass.getEnclosingClass(), typeArguments) : null, rawClass,
				argumentsForClass(rawClass, typeArguments));
	}

	private static Type[] argumentsForClass(Class<?> rawClass,
			Map<TypeVariable<?>, Type> typeArguments) {
		Type[] arguments = new Type[rawClass.getTypeParameters().length];
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = typeArguments.get(rawClass.getTypeParameters()[i]);
			if (arguments[i] == null)
				arguments[i] = rawClass.getTypeParameters()[i];
		}
		return arguments;
	}

	private static final class ParameterizedTypeImpl implements
			ParameterizedType, Serializable {
		private final Type ownerType;
		private final List<Type> argumentsList;
		private final Class<?> rawType;

		ParameterizedTypeImpl(Type ownerType, Class<?> rawType, Type[] typeArguments) {
			// TODO checkNotNull(rawType);
			// checkArgument(typeArguments.length ==
			// rawType.getTypeParameters().length);
			// disallowPrimitiveType(typeArguments, "type parameter");
			this.ownerType = ownerType;
			this.rawType = rawType;
			this.argumentsList = Arrays.asList(typeArguments);
		}

		@Override
		public Type[] getActualTypeArguments() {
			return argumentsList.toArray(new Type[argumentsList.size()]);
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return ownerType;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			if (ownerType != null)
				builder.append(ownerType).append('.');
			builder
					.append(rawType.getName())
					.append('<')
					.append(
							argumentsList.stream().map(Type::toString)
									.collect(Collectors.joining(" & "))).append('>');
			return builder.toString();
		}

		@Override
		public int hashCode() {
			return (ownerType == null ? 0 : ownerType.hashCode())
					^ argumentsList.hashCode() ^ rawType.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof ParameterizedType)) {
				return false;
			}
			ParameterizedType that = (ParameterizedType) other;
			return getRawType().equals(that.getRawType())
					&& Objects.equals(getOwnerType(), that.getOwnerType())
					&& Arrays.equals(getActualTypeArguments(),
							that.getActualTypeArguments());
		}

		private static final long serialVersionUID = 0;
	}
}
