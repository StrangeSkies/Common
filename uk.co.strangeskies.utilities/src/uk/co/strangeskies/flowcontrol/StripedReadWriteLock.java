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
package uk.co.strangeskies.flowcontrol;

import java.util.Collection;

/**
 * A striped lock may contain any number of locks, which are indexed by a keys
 * of a given type.
 * 
 * <p>
 * The purpose of a striped lock is generally to avoid deadlock in systems with
 * many different locks. This is achieved by requiring {@link Thread}s to
 * acquire all the locks they will need in one go, then releasing them when
 * possible, meaning that a thread will never be waiting for a lock when it
 * already has one.
 * 
 * <p>
 * This class does not enforce that a {@link Thread} does not try to obtain more
 * locks when it already has some, this is expected to be dealt with in user
 * code, though it would be trivial to implement a subclass providing this
 * functionality. Instead the {@link StripedReadWriteLockRelease} interface is
 * provided such that once locks are acquired, processes can be given the means
 * to release and query the state of those locks without leaking the ability to
 * acquire more.
 * 
 * <p>
 * The overloads of {@link #obtainLocks(Collection, Collection)} will block
 * until every requested lock is obtained, without allowing deadlocks to occur
 * between multiple blocking threads.
 *
 * @author Elias N Vasylenko
 * @param <K>
 *          the type of the keys by which locks are indexed
 */
public interface StripedReadWriteLock<K> extends StripedReadWriteLockRelease<K> {
	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param readKeys
	 *          Keys for read locks to be obtained
	 * @param writeKeys
	 *          Keys for write locks to be obtained
	 * @throws InterruptedException
	 *           The obtaining thread was interrupted before the locks could be
	 *           obtained.
	 */
	public void obtainLocks(Collection<K> readKeys, Collection<K> writeKeys) throws InterruptedException;

	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param readKeys
	 *          Keys for read locks to be obtained
	 * @throws InterruptedException
	 *           The obtaining thread was interrupted before the locks could be
	 *           obtained.
	 */
	public void obtainReadLocks(Collection<K> readKeys) throws InterruptedException;

	/**
	 * Multiple concurrent calls from different threads do not lock. Blocks until
	 * all locks can be obtained (or until interruption).
	 * 
	 * @param writeKeys
	 *          Keys for write locks to be obtained
	 * @throws InterruptedException
	 *           The obtaining thread was interrupted before the locks could be
	 *           obtained.
	 */
	public void obtainWriteLocks(Collection<K> writeKeys) throws InterruptedException;

	/**
	 * Wait for the given lock to become available.
	 * 
	 * @param key
	 *          The key to the lock on which we wish to wait.
	 * @throws InterruptedException
	 *           The waiting thread was interrupted before the lock could be
	 *           obtained.
	 */
	public void wait(K key) throws InterruptedException;

	/**
	 * Wait for the given lock to become available.
	 * 
	 * @param key
	 *          The key to the lock on which we wish to wait.
	 * @param timeoutMilliseconds
	 *          The timeout in milliseconds to wait.
	 * @throws InterruptedException
	 *           The waiting thread was interrupted before the lock could be
	 *           obtained.
	 */
	public void wait(K key, long timeoutMilliseconds) throws InterruptedException;

	/**
	 * Wait for the given lock to become available.
	 * 
	 * @param key
	 *          The key to the lock on which we wish to wait.
	 * @param timeoutMilliseconds
	 *          The timeout component in milliseconds to wait.
	 * @param timeoutNanoseconds
	 *          The timeout component in nanoseconds to wait.
	 * @throws InterruptedException
	 *           The waiting thread was interrupted before the lock could be
	 *           obtained.
	 */
	public void wait(K key, long timeoutMilliseconds, int timeoutNanoseconds) throws InterruptedException;
}
