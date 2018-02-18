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
package uk.co.strangeskies.mathematics.values;

import java.util.AbstractList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class DoubleArrayListView<V extends Value<V>> extends AbstractList<V> {
	private final double[] array;
	private final Supplier<V> valueFactory;

	public DoubleArrayListView(double[] array, Supplier<V> valueFactory) {
		if (array == null) {
			throw new IllegalArgumentException(new NullPointerException());
		}

		this.array = array;
		this.valueFactory = valueFactory;
	}

	@Override
	public final V get(int index) {
		return valueFactory.get().setValue(array[index]);
	}

	public V set(int index, Value<?> element) {
		double previousValue = array[index];

		array[index] = element.intValue();

		return valueFactory.get().setValue(previousValue);
	}

	@Override
	public final int size() {
		return array.length;
	}

	public static abstract class Tester {
		public Object testval;
		public int[] ints;

		public abstract Object test();

		public abstract Set<String> nonsense();

		public abstract int get(int i);

		public abstract void set(int[] i);
	}

	public static void main(String... args) {
		Tester tester = new Tester() {
			@Override
			public Object test() {
				return testval;
			}

			@Override
			public Set<String> nonsense() {
				return Collections.emptySet();
			}

			@Override
			public int get(int i) {
				return ints[i];
			}

			@Override
			public void set(int[] i) {
				ints = i;
			}
		};

		int rounds = 100;
		for (int r = 0; r < rounds; r++) {
			int size = 1000000;
			int roundRounds = 1000;
			long startTime;
			long count;

			int[] ints = new int[size];
			int[] testerInts = new int[size];
			tester.set(testerInts);
			Integer[] integers = new Integer[size];
			IntValue[] intValues = new IntValue[size];
			for (int i = 0; i < size; i++) {
				intValues[i] = new IntValue(
						testerInts[i] = ints[i] = integers[i] = new Random(System.currentTimeMillis())
								.nextInt(1000));
			}

			count = 0;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += ints[i];
				}
			System.out.println(
					"ints adding: " + (System.currentTimeMillis() - startTime) + " count = " + count);

			count = 0;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += integers[i];
				}
			System.out.println(
					"integers adding: " + (System.currentTimeMillis() - startTime) + " count = " + count);

			count = 0l;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += ints[i];
					if (tester.test() != null) {
						for (String nonsense : tester.nonsense()) {
							System.out.println(nonsense);
						}
					}
				}
			System.out.println(
					"ints adding with if: " + (System.currentTimeMillis() - startTime) + " count = " + count);

			count = 0l;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += integers[i];
					if (tester.test() != null) {
						for (String nonsense : tester.nonsense()) {
							System.out.println(nonsense);
						}
					}
				}
			System.out.println(
					"integers adding with if: " + (System.currentTimeMillis() - startTime) + " count = "
							+ count);

			count = 0l;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += tester.get(i);
					for (String nonsense : tester.nonsense()) {
						System.out.println(nonsense);
					}
				}
			System.out.println(
					"getints adding with if: " + (System.currentTimeMillis() - startTime) + " count = "
							+ count);

			count = 0l;
			startTime = System.currentTimeMillis();
			for (int s = 0; s < roundRounds; s++)
				for (int i = 0; i < size; i++) {
					count += intValues[i].intValue();
					for (String nonsense : tester.nonsense()) {
						System.out.println(nonsense);
					}
				}
			System.out.println(
					"getints adding with if: " + (System.currentTimeMillis() - startTime) + " count = "
							+ count);

			System.out.println();
		}
	}
}
