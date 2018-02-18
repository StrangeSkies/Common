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
 * This file is part of uk.co.strangeskies.mathematics.
 *
 * uk.co.strangeskies.mathematics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import uk.co.strangeskies.property.Property;

public final class Interval<T> {
	private final T leftEndpoint;
	private final T rightEndpoint;

	private final boolean leftClosed;
	private final boolean rightClosed;

	private final Comparator<? super T> comparator;

	private Interval(T leftEndpoint, T rightEndpoint, boolean leftClosed, boolean rightClosed,
			Comparator<? super T> comparator) {
		this.leftEndpoint = leftEndpoint;
		this.rightEndpoint = rightEndpoint;

		this.leftClosed = leftClosed;
		this.rightClosed = rightClosed;

		this.comparator = comparator;
	}

	public static <T> Interval<T> bounded(T leftEndpoint, T rightEndpoint, Comparator<? super T> comparator) {
		Objects.requireNonNull(leftEndpoint);
		Objects.requireNonNull(rightEndpoint);
		return new Interval<>(leftEndpoint, rightEndpoint, true, true, comparator);
	}

	public static <T extends Comparable<? super T>> Interval<T> bounded(T leftEndpoint, T rightEndpoint) {
		return bounded(leftEndpoint, rightEndpoint, Comparable::compareTo);
	}

	public static <T> Interval<T> leftBounded(T leftEndpoint, Comparator<? super T> comparator) {
		Objects.requireNonNull(leftEndpoint);
		return new Interval<>(leftEndpoint, null, true, true, comparator);
	}

	public static <T extends Comparable<? super T>> Interval<T> leftBounded(T leftEndpoint) {
		return leftBounded(leftEndpoint, Comparable::compareTo);
	}

	public static <T> Interval<T> rightBounded(T rightEndpoint, Comparator<? super T> comparator) {
		Objects.requireNonNull(rightEndpoint);
		return new Interval<>(null, rightEndpoint, true, true, comparator);
	}

	public static <T extends Comparable<? super T>> Interval<T> rightBounded(T rightEndpoint) {
		return rightBounded(rightEndpoint, Comparable::compareTo);
	}

	public static <T> Interval<T> unbounded(Comparator<? super T> comparator) {
		return new Interval<>(null, null, true, true, comparator);
	}

	public static <T extends Comparable<? super T>> Interval<T> unbounded() {
		return unbounded(Comparable::compareTo);
	}

	public static <T> Interval<T> over(Collection<? extends T> over, Comparator<? super T> comparator) {
		if (over.isEmpty())
			throw new IllegalArgumentException("No elements given to range over");

		return new Interval<>(over.stream().min(comparator).get(), over.stream().max(comparator).get(), true, true,
				comparator);
	}

	public static <T extends Comparable<? super T>> Interval<T> over(Collection<? extends T> over) {
		return over(over, Comparable::compareTo);
	}

	public static Interval<Integer> parse(String range) {
		String[] splitRange = range.split("\\.\\.", -1);
		if (splitRange.length != 2)
			throw new IllegalArgumentException();
		try {
			Integer from = splitRange[0].equals("") ? null : Integer.parseInt(splitRange[0]);
			Integer to = splitRange[1].equals("") ? null : Integer.parseInt(splitRange[1]);
			return bounded(from, to);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	public static String compose(Interval<Integer> range) {
		String from = range.getLeftEndpoint() == null ? ""
				: (range.isLeftClosed() ? range.getLeftEndpoint().toString()
						: Integer.toString(range.getLeftEndpoint() + 1));

		String to = range.getRightEndpoint() == null ? ""
				: (range.isRightClosed() ? range.getRightEndpoint().toString()
						: Integer.toString(range.getRightEndpoint() - 1));

		return from + ".." + to;
	}

	@Override
	public String toString() {
		return (leftClosed ? "[" : "(") + (leftEndpoint != null ? leftEndpoint : "") + ","
				+ (rightEndpoint != null ? rightEndpoint : "") + (rightClosed ? "]" : ")");
	}

	public Comparator<? super T> getComparator() {
		return comparator;
	}

	public T getLeftEndpoint() {
		return leftEndpoint;
	}

	public T getRightEndpoint() {
		return rightEndpoint;
	}

	public Interval<T> withLeftBound(T leftEndpoint) {
		return new Interval<>(leftEndpoint, rightEndpoint, leftClosed, rightClosed, comparator);
	}

	public Interval<T> withRightBound(T rightEndpoint) {
		return new Interval<>(leftEndpoint, rightEndpoint, leftClosed, rightClosed, comparator);
	}

	public Interval<T> withLeftUnbounded() {
		return new Interval<>(null, rightEndpoint, leftClosed, rightClosed, comparator);
	}

	public Interval<T> withRightUnbounded() {
		return new Interval<>(leftEndpoint, null, leftClosed, rightClosed, comparator);
	}

	public boolean isLeftClosed() {
		return leftClosed;
	}

	public boolean isRightClosed() {
		return rightClosed;
	}

	public boolean isLeftOpen() {
		return !leftClosed;
	}

	public boolean isRightOpen() {
		return !rightClosed;
	}

	public boolean isOpen() {
		return isLeftOpen() && isRightOpen();
	}

	public boolean isClosed() {
		return isLeftClosed() && isRightClosed();
	}

	public boolean isHalfOpen() {
		return isLeftOpen() ^ isRightOpen();
	}

	public boolean isLeftBounded() {
		return leftEndpoint != null;
	}

	public boolean isRightBounded() {
		return rightEndpoint != null;
	}

	public boolean isLeftUnbounded() {
		return leftEndpoint == null;
	}

	public boolean isRightUnbounded() {
		return rightEndpoint == null;
	}

	public boolean isBounded() {
		return isLeftBounded() && isRightBounded();
	}

	public boolean isUnbounded() {
		return isLeftUnbounded() && isRightUnbounded();
	}

	public boolean isHalfBounded() {
		return isLeftBounded() ^ isRightBounded();
	}

	public Interval<T> withLeftClosed() {
		return new Interval<>(leftEndpoint, rightEndpoint, true, rightClosed, comparator);
	}

	public Interval<T> withRightClosed() {
		return new Interval<>(leftEndpoint, rightEndpoint, leftClosed, true, comparator);
	}

	public Interval<T> withLeftOpen() {
		return new Interval<>(leftEndpoint, rightEndpoint, false, rightClosed, comparator);
	}

	public Interval<T> withRightOpen() {
		return new Interval<>(leftEndpoint, rightEndpoint, leftClosed, false, comparator);
	}

	public Interval<T> withClosedEndpoints() {
		return new Interval<>(leftEndpoint, rightEndpoint, true, true, comparator);
	}

	public Interval<T> withOpenEndpoints() {
		return new Interval<>(leftEndpoint, rightEndpoint, false, false, comparator);
	}

	public Interval<T> withClosedEndpoints(boolean leftClosed, boolean rightClosed) {
		return new Interval<>(leftEndpoint, rightEndpoint, leftClosed, rightClosed, comparator);
	}

	public Interval<T> withClosedEndpoints(boolean closed) {
		return new Interval<>(leftEndpoint, rightEndpoint, closed, closed, comparator);
	}

	public boolean isEmpty() {
		return !leftClosed && !rightClosed && leftEndpoint.equals(rightEndpoint);
	}

	public boolean isDegenerate() {
		return leftClosed || rightClosed && leftEndpoint.equals(rightEndpoint);
	}

	public boolean contains(final T value) {
		return contains(new Comparable<T>() {
			@Override
			public int compareTo(T other) {
				return comparator.compare(value, other);
			}
		});
	}

	public boolean touches(final T value) {
		return touches(new Comparable<T>() {
			@Override
			public int compareTo(T other) {
				return comparator.compare(value, other);
			}
		});
	}

	public boolean contains(Comparable<? super T> value) {
		int fromCompare = leftEndpoint == null ? 1 : value.compareTo(leftEndpoint);
		int toCompare = rightEndpoint == null ? -1 : value.compareTo(rightEndpoint);

		if (leftClosed) {
			if (rightClosed) {
				return fromCompare >= 0 && toCompare <= 0;
			} else {
				return fromCompare >= 0 && toCompare < 0;
			}
		} else {
			if (rightClosed) {
				return fromCompare > 0 && toCompare <= 0;
			} else {
				return fromCompare > 0 && toCompare < 0;
			}
		}
	}

	public boolean touches(Comparable<? super T> value) {
		return value.compareTo(leftEndpoint) >= 0 && value.compareTo(rightEndpoint) <= 0;
	}

	public boolean contains(Interval<T> range) {
		if (getLeftEndpoint() != null)
			if (range.getLeftEndpoint() == null) {
				return false;
			} else if (range.isLeftClosed()) {
				if (!contains(range.getLeftEndpoint()))
					return false;
			} else {
				if (!touches(range.getLeftEndpoint()))
					return false;
			}

		if (getRightEndpoint() != null)
			if (range.getRightEndpoint() == null) {
				return false;
			} else if (range.isRightClosed()) {
				if (!contains(range.getRightEndpoint()))
					return false;
			} else {
				if (!touches(range.getRightEndpoint()))
					return false;
			}

		return true;
	}

	public boolean isValueBelow(T value) {
		if (leftEndpoint == null)
			return false;
		if (leftClosed) {
			return comparator.compare(value, leftEndpoint) < 0;
		} else {
			return comparator.compare(value, leftEndpoint) <= 0;
		}
	}

	public boolean isValueAbove(T value) {
		if (rightEndpoint == null)
			return false;
		if (rightClosed) {
			return comparator.compare(value, rightEndpoint) > 0;
		} else {
			return comparator.compare(value, rightEndpoint) >= 0;
		}
	}

	public boolean isValueBelow(Comparable<? super T> value) {
		if (leftEndpoint == null)
			return false;
		if (leftClosed) {
			return value.compareTo(leftEndpoint) < 0;
		} else {
			return value.compareTo(leftEndpoint) <= 0;
		}
	}

	public boolean isValueAbove(Comparable<? super T> value) {
		if (rightEndpoint == null)
			return false;
		if (rightClosed) {
			return value.compareTo(rightEndpoint) > 0;
		} else {
			return value.compareTo(rightEndpoint) >= 0;
		}
	}

	public T getConfined(T value) {
		if (isEmpty()) {
			return null;
		}
		if (contains(value)) {
			return value;
		}
		if (comparator.compare(leftEndpoint, value) > 0) {
			return leftEndpoint;
		}
		return rightEndpoint;
	}

	public <M extends Property<T>> M confine(M value) {
		if (isEmpty()) {
			return null;
		}
		if (isValueAbove(value.get())) {
			value.set(rightEndpoint);
		} else if (isValueBelow(value.get())) {
			value.set(leftEndpoint);
		}
		return value;
	}

	public Interval<T> getExtendedThrough(Interval<? extends T> other) {
		T from;
		boolean fromInclusive;

		int compareFrom = comparator.compare(other.getLeftEndpoint(), getLeftEndpoint());
		if (compareFrom == 0) {
			from = getLeftEndpoint();
			fromInclusive = isLeftClosed() || other.isLeftClosed();
		} else if (compareFrom < 0) {
			from = other.getLeftEndpoint();
			fromInclusive = other.isLeftClosed();
		} else {
			from = getLeftEndpoint();
			fromInclusive = isLeftClosed();
		}

		T to;
		boolean toInclusive;

		int compareTo = comparator.compare(other.getRightEndpoint(), getRightEndpoint());
		if (compareTo == 0) {
			to = getRightEndpoint();
			toInclusive = isRightClosed() || other.isRightClosed();
		} else if (compareTo > 0) {
			to = other.getRightEndpoint();
			toInclusive = other.isRightClosed();
		} else {
			to = getRightEndpoint();
			toInclusive = isRightClosed();
		}

		return new Interval<>(from, to, fromInclusive, toInclusive, comparator);
	}

	public Interval<T> getExtendedThrough(T other, boolean inclusive) {
		T from;
		boolean fromInclusive;
		T to;
		boolean toInclusive;

		int compareFrom = comparator.compare(other, getLeftEndpoint());
		int compareTo = comparator.compare(other, getRightEndpoint());

		if (compareFrom < 0) {
			from = other;
			fromInclusive = inclusive;
			to = getRightEndpoint();
			toInclusive = isRightClosed();

		} else if (compareTo > 0) {
			from = getLeftEndpoint();
			fromInclusive = isLeftClosed();
			to = other;
			toInclusive = inclusive;

		} else {
			fromInclusive = (inclusive && compareFrom == 0) || isLeftClosed();
			toInclusive = (inclusive && compareTo == 0) || isRightClosed();

			return new Interval<>(getLeftEndpoint(), getRightEndpoint(), fromInclusive, toInclusive, comparator);
		}

		return new Interval<>(from, to, fromInclusive, toInclusive, comparator);
	}

	public Interval<T> getIntersectionWith(Interval<? extends T> other) {
		T newFrom = getLeftEndpoint();
		boolean newFromInclusive = isLeftClosed();

		int compareFrom = comparator.compare(other.getLeftEndpoint(), getLeftEndpoint());
		if (compareFrom == 0) {
			newFromInclusive = isLeftClosed() && other.isLeftClosed();
		} else if (compareFrom > 0) {
			newFrom = other.getLeftEndpoint();
			newFromInclusive = other.isLeftClosed();
		}

		T newTo = getRightEndpoint();
		boolean newToInclusive = isRightClosed();

		int compareTo = comparator.compare(other.getRightEndpoint(), getRightEndpoint());
		if (compareTo == 0) {
			newToInclusive = isRightClosed() || other.isRightClosed();
		} else if (compareTo < 0) {
			newTo = other.getRightEndpoint();
			newToInclusive = other.isRightClosed();
		}

		if (comparator.compare(newFrom, newTo) > 0)
			throw new IllegalArgumentException("Ranges '" + this + "' and '" + other + "' do not intersect");

		return new Interval<>(newFrom, newTo, newFromInclusive, newToInclusive, comparator);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Interval))
			return false;
		if (obj == this)
			return true;

		Interval<?> otherRange = (Interval<?>) obj;

		return Objects.equals(leftEndpoint, otherRange.leftEndpoint)
				&& Objects.equals(rightEndpoint, otherRange.rightEndpoint)
				&& Objects.equals(rightClosed, otherRange.rightClosed)
				&& Objects.equals(leftClosed, otherRange.leftClosed);
	}

	@Override
	public int hashCode() {
		return Objects.hash(leftEndpoint) ^ Objects.hash(rightEndpoint);
	}
}
