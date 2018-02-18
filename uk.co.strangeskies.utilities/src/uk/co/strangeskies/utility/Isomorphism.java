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
 * This file is part of uk.co.strangeskies.utilities.
 *
 * uk.co.strangeskies.utilities is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.utilities is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.utility;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.property.IdentityProperty;

/**
 * An isomorphic mapping from one object graph to another, typically maintained
 * during a set of {@link Copyable#deepCopy(Isomorphism) deep copy} operations.
 * So long as mappings are obtained through the isomorphism they will only be
 * made at most once, and results of mapping will not be re-mapped in chains.
 * <p>
 * An isomorphism can be used across multiple deep copy operations, to build a
 * copy of a "forest" of object graphs.
 * <p>
 * An isomorphism cannot be reused over the same object after that object has
 * been mutated.
 * <p>
 * Please take care! If a different mapping function is used on an object which
 * has already been mapped, heap pollution may occur when the previous mapping
 * result is cast to the new expected result type.
 * 
 * @author Elias N Vasylenko
 */
public class Isomorphism {
	private final Mapping identity = new Mapping(new IdentityHashMap<>());
	private final Mapping equality = new Mapping(new HashMap<>());

	/**
	 * @return a {@link Mapping mapping} interface for nodes whose uniqueness is
	 *         determined by reference identity.
	 */
	public Mapping byIdentity() {
		return identity;
	}

	/**
	 * @return a {@link Mapping mapping} interface for nodes whose uniqueness is
	 *         determined by {@link Object#equals(Object) equality}.
	 */
	public Mapping byEquality() {
		return equality;
	}

	/**
	 * The main API surface for interacting with the {@link Isomorphism},
	 * represents a mapping according to a given key equality.
	 * 
	 * @author Elias N Vasylenko
	 */
	public class Mapping {
		private final Map<?, ? extends Supplier<?>> copiedNodes;

		Mapping(Map<Object, Supplier<Object>> backingMap) {
			copiedNodes = backingMap;
		}

		/**
		 * Make a shallow copy of the given {@link Copyable}, or fetch an existing
		 * mapping if one has been made via this {@link Isomorphism}.
		 * 
		 * @param <S>
		 *          the type of the result
		 * @param node
		 *          the graph node to copy
		 * @return a copy of the given node
		 */
		public <S> S getCopy(Copyable<? extends S> node) {
			return getMapping(node, Copyable::copy);
		}

		/**
		 * Make a deep copy of the given {@link Copyable}, or fetch an existing
		 * mapping if one has been made via this {@link Isomorphism}.
		 * 
		 * @param <S>
		 *          the type of the result
		 * @param node
		 *          the graph node to copy
		 * @return a copy of the given node
		 */
		public <S> S getDeepCopy(Copyable<? extends S> node) {
			return getMapping(node, n -> n.deepCopy(Isomorphism.this));
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param mapping
		 *          the mapping to provide
		 */
		@SuppressWarnings("unchecked")
		public <S, C> void putMapping(C node, S mapping) {
			((Map<C, S>) copiedNodes).put(node, mapping);
		}

		/**
		 * @return true if no node mappings exist yet in the isomorphism, false
		 *         otherwise
		 */
		public boolean isEmpty() {
			return getMappedNodes().isEmpty();
		}

		/**
		 * @return all the nodes which currently have mappings in the isomorphism
		 */
		public Set<?> getMappedNodes() {
			return copiedNodes.keySet();
		}

		/**
		 * Fetch an existing mapping if one has been made via this
		 * {@link Isomorphism}.
		 * 
		 * @param node
		 *          the graph node to map
		 * @return a mapping of the given node, or null if none exists
		 */
		public Object getMapping(Object node) {
			Supplier<?> copySupplier = copiedNodes.get(node);
			return copySupplier == null ? null : copySupplier.get();
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param mapping
		 *          the mapping function to apply
		 * @return a mapping of the given node
		 */
		@SuppressWarnings("unchecked")
		public <S, C> S getMapping(C node, Function<C, S> mapping) {
			Supplier<S> copy = ((Map<C, Supplier<S>>) copiedNodes).get(node);
			if (copy == null) {
				S copyItem = mapping.apply(node);
				copy = () -> copyItem;
				((Map<C, Supplier<S>>) copiedNodes).put(node, copy);
			}
			return copy.get();
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * <p>
		 * In the case of recursive graph structures we sometimes may need access to
		 * a partially constructed mapping, as we may revisit a node during the
		 * construction of its own mapping.
		 * <p>
		 * Once the mapping is calculated, the partial result will be removed from
		 * the isomorphism, and future attempts to map the node will return the
		 * complete result, though often the two references will be identity equal.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param mapping
		 *          the mapping function to apply, also accepting a consumer which
		 *          can be called back with a partial result
		 * @return a mapping of the given node
		 */
		@SuppressWarnings("unchecked")
		public <S, C> S getPartialMapping(C node, BiFunction<C, Consumer<Supplier<S>>, S> mapping) {
			Supplier<S> copySource = ((Map<C, Supplier<S>>) copiedNodes).get(node);

			S copy;

			if (copySource == null) {
				copy = mapping.apply(node, partial -> ((Map<C, Supplier<S>>) copiedNodes).put(node, partial));
				S finalCopy = copy;
				((Map<C, Supplier<S>>) copiedNodes).put(node, () -> finalCopy);
			} else {
				copy = copySource.get();
			}

			return copy;
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * <p>
		 * In the case of recursive graph structures we sometimes may need access to
		 * a partially constructed mapping, as we may revisit a node during the
		 * construction of its own mapping.
		 * <p>
		 * Once the mapping is calculated, the partial result will be removed from
		 * the isomorphism, and future attempts to map the node will return the
		 * complete result, though often the two references will be identity equal.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param partial
		 *          a partial mapping to use until the proper mapping is computed
		 * @param mapping
		 *          the mapping function to apply, also accepting a consumer which
		 *          can be called back with a partial result
		 * @return a mapping of the given node
		 */
		@SuppressWarnings("unchecked")
		public <S, C> S getPartialMapping(C node, Supplier<S> partial, Function<C, S> mapping) {
			Supplier<S> copySource = ((Map<C, Supplier<S>>) copiedNodes).get(node);

			S copy;

			if (copySource == null) {
				((Map<C, Supplier<S>>) copiedNodes).put(node, partial);
				copy = mapping.apply(node);
				((Map<C, Supplier<S>>) copiedNodes).put(node, () -> copy);
			} else {
				copy = copySource.get();
			}

			return copy;
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * <p>
		 * In the case of recursive graph structures we sometimes may need access to
		 * a proxied mapping, as we may revisit a node during the construction of
		 * its own mapping.
		 * <p>
		 * Once the mapping is calculated, the proxied result will be removed from
		 * the isomorphism, and future attempts to map the node will return the
		 * complete result.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param proxyClass
		 *          the class of the result to proxy
		 * @param mapping
		 *          the mapping function to apply
		 * @return a mapping of the given node
		 */
		public <S, C> S getProxiedMapping(C node, Class<? extends S> proxyClass, Function<C, S> mapping) {
			return getProxiedMapping(node, proxyClass.getClassLoader(), proxyClass, mapping);
		}

		/**
		 * Make a mapping of the given node, or fetch an existing mapping if one has
		 * been made via this {@link Isomorphism}.
		 * <p>
		 * In the case of recursive graph structures we sometimes may need access to
		 * a proxied mapping, as we may revisit a node during the construction of
		 * its own mapping.
		 * <p>
		 * Once the mapping is calculated, the proxied result will be removed from
		 * the isomorphism, and future attempts to map the node will return the
		 * complete result.
		 * 
		 * @param <S>
		 *          the type of the node
		 * @param <C>
		 *          the type of the result
		 * @param node
		 *          the graph node to map
		 * @param classLoader
		 *          the class loader to use for the proxy
		 * @param proxyClass
		 *          the class of the result to proxy
		 * @param mapping
		 *          the mapping function to apply
		 * @return a mapping of the given node
		 */
		@SuppressWarnings("unchecked")
		public <S, C> S getProxiedMapping(
				C node,
				ClassLoader classLoader,
				Class<? extends S> proxyClass,
				Function<C, S> mapping) {
			while (node instanceof IsomorphismProxy) {
				C proxiedNode = (C) ((IsomorphismProxy) node).getProxiedObjectFromIsomorphism();
				if (proxiedNode != null) {
					node = proxiedNode;
				} else {
					break;
				}
			}

			return getPartialMapping(node, (C n, Consumer<Supplier<S>> partial) -> {
				IdentityProperty<S> property = new IdentityProperty<>();

				partial.accept(
						() -> (S) Proxy.newProxyInstance(
								new IsomorphismProxyClassLoader(classLoader),
								new Class[] { proxyClass, IsomorphismProxy.class },
								new InvocationHandler() {
									@Override
									public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
										if (method.getDeclaringClass().equals(IsomorphismProxy.class)) {
											return property.get();
										}
										return method.invoke(property.get(), args);
									}
								}));

				S result = mapping.apply(n);

				property.set(result);
				return result;
			});
		}
	}

	class IsomorphismProxyClassLoader extends ClassLoader {
		public IsomorphismProxyClassLoader(ClassLoader classLoader) {
			super(classLoader);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			if (name.equals(IsomorphismProxy.class.getName())) {
				return IsomorphismProxy.class;
			} else {
				return super.findClass(name);
			}
		}
	}

	/**
	 * Marker interface for proxies created by {@link Isomorphism}. Only public to
	 * avoid bothering to fix access errors.
	 * 
	 * @author Elias N Vasylenko
	 */
	public static interface IsomorphismProxy {
		/**
		 * @return the original proxied object if available, otherwise null
		 */
		Object getProxiedObjectFromIsomorphism();
	}
}
