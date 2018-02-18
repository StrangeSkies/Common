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

import static java.lang.Thread.currentThread;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * A simple {@link StripedReadWriteLock} implementation.
 * 
 * @author Elias N Vasylenko
 *
 * @param <K>
 *          the type of the keys by which locks are indexed
 */
public class StripedReadWriteLockImpl<K> implements StripedReadWriteLock<K> {
	private final Map<K, ReentrantReadWriteLock> locks;
	private final Map<K, Set<Thread>> readLockingThreads;

	@SuppressWarnings("javadoc")
	public StripedReadWriteLockImpl() {
		locks = new HashMap<>();
		readLockingThreads = new HashMap<>();
	}

	@Override
	public Set<K> readLocksHeldByCurrentThread() {
		synchronized (locks) {
			return locks.keySet().stream().filter(this::isReadLockHeldByCurrentThread).collect(Collectors.toSet());
		}
	}

	@Override
	public Set<K> writeLocksHeldByCurrentThread() {
		synchronized (locks) {
			return locks.keySet().stream().filter(this::isWriteLockHeldByCurrentThread).collect(Collectors.toSet());
		}
	}

	@Override
	public final void obtainReadLocks(Collection<K> readKeys) throws InterruptedException {
		synchronized (locks) {
			boolean allLocksAvailable;

			Set<K> readLocksObtained = new HashSet<>();

			do {
				readLocksObtained.clear();
				allLocksAvailable = true;

				for (K readkey : readKeys) {
					if (!tryToObtainReadLock(readkey)) {
						allLocksAvailable = false;
						break;
					}

					readLocksObtained.add(readkey);
				}

				if (!allLocksAvailable) {
					for (K readkey : readLocksObtained) {
						releaseReadLock(readkey);
					}

					locks.wait();
				}
			} while (!allLocksAvailable);
		}
	}

	@Override
	public final void obtainWriteLocks(Collection<K> writeKeys) throws InterruptedException {
		synchronized (locks) {
			boolean allLocksAvailable;

			Set<K> writeLocksObtained = new HashSet<>();

			do {
				writeLocksObtained.clear();
				allLocksAvailable = true;

				for (K writekey : writeKeys) {
					if (!tryToObtainWriteLock(writekey)) {
						allLocksAvailable = false;
						break;
					}

					writeLocksObtained.add(writekey);
				}

				if (!allLocksAvailable) {
					for (K writekey : writeLocksObtained) {
						releaseWriteLock(writekey);
					}

					locks.wait();
				}
			} while (!allLocksAvailable);
		}
	}

	@Override
	public final void obtainLocks(Collection<K> readKeys, Collection<K> writeKeys) throws InterruptedException {
		synchronized (locks) {
			boolean allLocksAvailable;

			Set<K> readLocksObtained = new HashSet<>();
			Set<K> writeLocksObtained = new HashSet<>();

			do {
				readLocksObtained.clear();
				writeLocksObtained.clear();
				allLocksAvailable = true;

				for (K writekey : writeKeys) {
					if (!tryToObtainWriteLock(writekey)) {
						allLocksAvailable = false;
						break;
					}

					writeLocksObtained.add(writekey);
				}

				if (allLocksAvailable) {
					for (K readkey : readKeys) {
						if (!tryToObtainReadLock(readkey)) {
							allLocksAvailable = false;
							break;
						}

						readLocksObtained.add(readkey);
					}
				}

				if (!allLocksAvailable) {
					for (K readkey : readLocksObtained) {
						releaseReadLock(readkey);
					}
					for (K writekey : writeLocksObtained) {
						releaseWriteLock(writekey);
					}

					locks.wait();
				}
			} while (!allLocksAvailable);
		}
	}

	@Override
	public final boolean releaseReadLocks(Collection<? extends K> readKeys) {
		boolean released = false;

		synchronized (locks) {
			for (K readkey : readKeys) {
				released |= silentlyReleaseReadLock(readkey);
			}

			if (released) {
				locks.notifyAll();
			}
		}

		return released;
	}

	@Override
	public final boolean releaseWriteLocks(Collection<? extends K> writeKeys) {
		boolean released = false;

		synchronized (locks) {
			for (K readkey : writeKeys) {
				released |= silentlyReleaseWriteLock(readkey);
			}

			if (released) {
				locks.notifyAll();
			}
		}

		return released;
	}

	@Override
	public boolean releaseLocks(Collection<? extends K> keys) {
		return releaseLocks(keys, keys);
	}

	@Override
	public final boolean releaseLocks(Collection<? extends K> readKeys, Collection<? extends K> writeKeys) {
		boolean released = false;

		synchronized (locks) {
			for (K readkey : readKeys) {
				released |= silentlyReleaseReadLock(readkey);
			}
			for (K writekey : writeKeys) {
				released |= silentlyReleaseWriteLock(writekey);
			}

			if (released) {
				locks.notifyAll();
			}
		}

		return released;
	}

	@Override
	public final boolean releaseReadLock(K key) {
		synchronized (locks) {
			boolean released = silentlyReleaseReadLock(key);

			if (released) {
				locks.notifyAll();
			}

			return released;
		}
	}

	@Override
	public final boolean releaseWriteLock(K key) {
		synchronized (locks) {
			boolean released = silentlyReleaseWriteLock(key);

			if (released) {
				locks.notifyAll();
			}

			return released;
		}
	}

	@Override
	public final boolean downgradeLock(K key) {
		synchronized (locks) {
			boolean downgraded = silentlyDowngradeLock(key);

			if (downgraded) {
				locks.notifyAll();
			}

			return downgraded;
		}
	}

	@Override
	public final boolean releaseLock(K key) {
		synchronized (locks) {
			return releaseReadLock(key) | releaseWriteLock(key);
		}
	}

	protected final boolean tryToObtainReadLock(final K key) {
		synchronized (locks) {
			ReentrantReadWriteLock lock = locks.get(key);

			if (lock == null) {
				lock = new ReentrantReadWriteLock(true);
				locks.put(key, lock);
			}

			boolean obtained = lock.readLock().tryLock();

			if (obtained) {
				readLockingThreads.computeIfAbsent(key, a -> new HashSet<>()).add(currentThread());
			}

			return obtained;
		}
	}

	protected final boolean tryToObtainWriteLock(final K key) {
		synchronized (locks) {
			ReentrantReadWriteLock lock = locks.get(key);

			if (lock == null) {
				lock = new ReentrantReadWriteLock(true);
				locks.put(key, lock);
			}

			return lock.writeLock().tryLock();
		}
	}

	protected final boolean silentlyReleaseReadLock(final K key) {
		synchronized (locks) {
			ReentrantReadWriteLock lock = locks.get(key);

			if (lock != null && lock.getReadHoldCount() > 0) {
				lock.readLock().unlock();

				if (!lock.hasQueuedThreads()) {
					locks.remove(key);
				}

				readLockingThreads.get(key).remove(currentThread());
				readLockingThreads.remove(key, emptySet());

				return true;
			}

			return false;
		}
	}

	protected final boolean silentlyReleaseWriteLock(final K key) {
		synchronized (locks) {
			ReentrantReadWriteLock lock = locks.get(key);

			if (lock != null && lock.isWriteLockedByCurrentThread()) {
				lock.writeLock().unlock();

				if (!lock.hasQueuedThreads()) {
					locks.remove(key);
				}

				return true;
			}

			return false;
		}
	}

	protected final boolean silentlyDowngradeLock(final K key) {
		synchronized (locks) {
			ReentrantReadWriteLock lock = locks.get(key);

			if (lock != null && lock.isWriteLockedByCurrentThread()) {
				lock.readLock().lock();
				lock.writeLock().unlock();

				if (!lock.hasQueuedThreads()) {
					locks.remove(key);
				}

				readLockingThreads.computeIfAbsent(key, a -> new HashSet<>()).add(currentThread());

				return true;
			}

			return false;
		}
	}

	@Override
	public boolean isLockHeldByCurrentThread(K key) {
		return isReadLockHeldByCurrentThread(key) || isWriteLockHeldByCurrentThread(key);
	}

	@Override
	public boolean isReadLockHeldByCurrentThread(K key) {
		return readLockingThreads.getOrDefault(key, emptySet()).contains(currentThread());
	}

	@Override
	public boolean isWriteLockHeldByCurrentThread(K key) {
		ReentrantReadWriteLock lock = locks.get(key);
		return lock != null && lock.writeLock().isHeldByCurrentThread();
	}

	@Override
	public void wait(K key) throws InterruptedException {
		ReentrantReadWriteLock lock = locks.get(key);

		if (lock != null) {
			lock.wait();
		}
	}

	@Override
	public void wait(K key, long milliseconds) throws InterruptedException {
		ReentrantReadWriteLock lock = locks.get(key);

		if (lock != null) {
			lock.wait(milliseconds);
		}
	}

	@Override
	public void wait(K key, long milliseconds, int nanoseconds) throws InterruptedException {
		ReentrantReadWriteLock lock = locks.get(key);

		if (lock != null) {
			lock.wait(milliseconds, nanoseconds);
		}
	}
}
