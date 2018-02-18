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
 * This file is part of uk.co.strangeskies.mathematics.geometry.
 *
 * uk.co.strangeskies.mathematics.geometry is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.mathematics.geometry is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.mathematics.geometry.matrix;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.co.strangeskies.collection.ListTransformationView;
import uk.co.strangeskies.expression.SelfExpression;
import uk.co.strangeskies.function.TriFunction;
import uk.co.strangeskies.mathematics.geometry.DimensionalityException;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector.Orientation;
import uk.co.strangeskies.mathematics.geometry.matrix.vector.Vector2;
import uk.co.strangeskies.mathematics.operation.Negatable;
import uk.co.strangeskies.mathematics.operation.NonCommutativelyMultipliable;
import uk.co.strangeskies.mathematics.operation.Scalable;
import uk.co.strangeskies.mathematics.operation.Subtractable;
import uk.co.strangeskies.mathematics.values.IntValue;
import uk.co.strangeskies.mathematics.values.Value;

public interface Matrix<S extends Matrix<S, V>, V extends Value<V>>
    extends Comparable<Matrix<?, ?>>, SelfExpression<S>, Scalable<S>, Subtractable<S, Matrix<?, ?>>,
    Negatable<S, S>, NonCommutativelyMultipliable<S, Matrix<?, ?>> {
  public static enum Order {
    ROW_MAJOR {
      @Override
      public Orientation getAssociatedOrientation() {
        return Orientation.ROW;
      }

      @Override
      public Order getOther() {
        return COLUMN_MAJOR;
      }
    },

    COLUMN_MAJOR {
      @Override
      public Orientation getAssociatedOrientation() {
        return Orientation.COLUMN;
      }

      @Override
      public Order getOther() {
        return ROW_MAJOR;
      }
    };

    public abstract Orientation getAssociatedOrientation();

    public abstract Order getOther();
  }

  public Order getOrder();

  /**
   * @return A view of this matrix with the requested order. Changes to the view
   *         affect this matrix and vice versa.
   */
  public Matrix<?, V> withOrder(Order order);

  public Vector<?, V> getMajorVector(int index);

  public Vector<?, V> getMinorVector(int index);

  public List<V> getData();

  public List<List<V>> getData2();

  /**
   * Get the dimensions as: [columns, rows].
   *
   * @return A vector representing the current dimensions.
   */
  public Vector2<IntValue> getDimensions2();

  @Override
  public default S negate() {
    return operateOnData(element -> element.negate());
  }

  @Override
  public default S multiply(Value<?> scalar) {
    return operateOnData(element -> element.multiply(scalar));
  }

  @Override
  public default S multiply(long scalar) {
    return operateOnData(element -> element.multiply(scalar));
  }

  @Override
  public default S multiply(double scalar) {
    return operateOnData(element -> element.multiply(scalar));
  }

  @Override
  public default S divide(Value<?> scalar) {
    return operateOnData(element -> element.divide(scalar));
  }

  @Override
  public default S divide(long scalar) {
    return operateOnData(element -> element.divide(scalar));
  }

  @Override
  public default S divide(double scalar) {
    return operateOnData(element -> element.divide(scalar));
  }

  @Override
  public default S add(Matrix<?, ?> other) {
    return operateOnData2(
        other.withOrder(getOrder()).getData2(),
        (firstOperand, secondOperand) -> firstOperand.add(secondOperand));
  }

  @Override
  public default S subtract(Matrix<?, ?> other) {
    return operateOnData2(
        other.withOrder(getOrder()).getData2(),
        (firstOperand, secondOperand) -> firstOperand.subtract(secondOperand));
  }

  @Override
  public default S multiply(Matrix<?, ?> other) {
    return setData2(multiplyData(getData2(), other.withOrder(getOrder()).getData2()));
  }

  @Override
  public default S preMultiply(Matrix<?, ?> other) {
    return setData2(preMultiplyData(getData2(), other.withOrder(getOrder()).getData2()));
  }

  public static <V extends Value<?>> List<List<V>> multiplyData(
      List<? extends List<? extends Value<?>>> data,
      List<? extends List<? extends Value<?>>> otherData) {
    // TODO implement multiplication! include isResiseable() in parameter
    // dimensions check...
    return null;
  }

  public static <V extends Value<?>> List<List<V>> preMultiplyData(
      List<? extends List<? extends Value<?>>> data,
      List<? extends List<? extends Value<?>>> otherData) {
    return null;
  }

  public S transpose();

  public Matrix<?, V> getTransposed();

  public static <V extends Value<V>> List<List<V>> transposeData(List<List<V>> data) {
    int majorSize = data.get(0).size();
    int minorSize = data.size();

    List<List<V>> transposedData = new ArrayList<>();
    for (int i = 0; i < majorSize; i++) {
      List<V> elements = new ArrayList<>();
      transposedData.add(elements);
      for (int j = 0; j < minorSize; j++) {
        elements.add(data.get(j).get(i));
      }
    }
    return transposedData;
  }

  public default int getDataSize() {
    return getMajorSize() * getMinorSize();
  }

  public int getMajorSize();

  public int getMinorSize();

  public V getElement(int major, int minor);

  public default int getRowSize() {
    return getOrder() == Order.ROW_MAJOR ? getMajorSize() : getMinorSize();
  }

  public default int getColumnSize() {
    return getOrder() == Order.COLUMN_MAJOR ? getMajorSize() : getMinorSize();
  }

  public default boolean isSquare() {
    return getMajorSize() == getMinorSize();
  }

  public static <T extends Matrix<?, ?>> T assertIsSquare(T matrix) {
    try {
      DimensionalityException.checkEquivalence(matrix.getMajorSize(), matrix.getMinorSize());
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }

    return matrix;
  }

  public static <T extends Matrix<?, ?>> T assertDimensions(T matrix, int rows, int columns) {
    try {
      DimensionalityException.checkEquivalence(matrix.getRowSize(), rows);
      DimensionalityException.checkEquivalence(matrix.getColumnSize(), columns);
    } catch (DimensionalityException e) {
      throw new IllegalArgumentException(e);
    }

    return matrix;
  }

  public static <T extends Matrix<?, ?>> T assertDimensions(T matrix, int size) {
    return assertDimensions(matrix, size, size);
  }

  public default List<? extends Vector<?, V>> getRowVectors() {
    if (getOrder() == Order.ROW_MAJOR) {
      return getMajorVectors();
    } else {
      return getMinorVectors();
    }
  }

  public default List<? extends Vector<?, V>> getColumnVectors() {
    if (getOrder() == Order.COLUMN_MAJOR) {
      return getMajorVectors();
    } else {
      return getMinorVectors();
    }
  }

  public default Vector<?, V> getRowVector(int row) {
    if (getOrder() == Order.ROW_MAJOR) {
      return getMajorVector(row);
    } else {
      return getMinorVector(row);
    }
  }

  public default Vector<?, V> getColumnVector(int column) {
    if (getOrder() == Order.COLUMN_MAJOR) {
      return getMajorVector(column);
    } else {
      return getMinorVector(column);
    }
  }

  public default List<? extends Vector<?, V>> getMajorVectors() {
    return new AbstractList<Vector<?, V>>() {
      @Override
      public Vector<?, V> get(int index) {
        return getMajorVector(index);
      }

      @Override
      public int size() {
        return getMinorSize();
      }
    };
  }

  public default List<? extends Vector<?, V>> getMinorVectors() {
    return new AbstractList<Vector<?, V>>() {
      @Override
      public Vector<?, V> get(int index) {
        return getMinorVector(index);
      }

      @Override
      public int size() {
        return getMajorSize();
      }
    };
  }

  public default int[] getData(int[] dataArray) {
    operateOnData((value, index) -> {
      dataArray[index] = value.intValue();
      return value;
    });
    return dataArray;
  }

  public default long[] getData(long[] dataArray) {
    operateOnData((value, index) -> {
      dataArray[index] = value.longValue();
      return value;
    });
    return dataArray;
  }

  public default float[] getData(float[] dataArray) {
    operateOnData((value, index) -> {
      dataArray[index] = value.floatValue();
      return value;
    });
    return dataArray;
  }

  public default double[] getData(double[] dataArray) {
    operateOnData((value, index) -> {
      dataArray[index] = value.doubleValue();
      return value;
    });
    return dataArray;
  }

  public default int[][] getData2(int[][] dataArray) {
    operateOnData2((value, i, j) -> {
      dataArray[i][j] = value.intValue();
      return value;
    });
    return dataArray;
  }

  public default long[][] getData2(long[][] dataArray) {
    operateOnData2((value, i, j) -> {
      dataArray[i][j] = value.longValue();
      return value;
    });
    return dataArray;
  }

  public default float[][] getData2(float[][] dataArray) {
    operateOnData2((value, i, j) -> {
      dataArray[i][j] = value.floatValue();
      return value;
    });
    return dataArray;
  }

  public default double[][] getData2(double[][] dataArray) {
    operateOnData2((value, i, j) -> {
      dataArray[i][j] = value.doubleValue();
      return value;
    });
    return dataArray;
  }

  public default int[] getIntData() {
    return getData(new int[getDataSize()]);
  }

  public default long[] getLongData() {
    return getData(new long[getDataSize()]);
  }

  public default float[] getFloatData() {
    return getData(new float[getDataSize()]);
  }

  public default double[] getDoubleData() {
    return getData(new double[getDataSize()]);
  }

  public default int[][] getIntData2() {
    return getData2(new int[getMajorSize()][getMinorSize()]);
  }

  public default long[][] getLongData2() {
    return getData2(new long[getMajorSize()][getMinorSize()]);
  }

  public default float[][] getFloatData2() {
    return getData2(new float[getMajorSize()][getMinorSize()]);
  }

  public default double[][] getDoubleData2() {
    return getData2(new double[getMajorSize()][getMinorSize()]);
  }

  public S operateOnData(Function<? super V, ? extends V> operator);

  public S operateOnData(BiFunction<? super V, Integer, ? extends V> operator);

  public S operateOnData2(TriFunction<? super V, Integer, Integer, ? extends V> operator);

  public default <I> S operateOnData(
      List<? extends I> itemList,
      BiFunction<? super V, ? super I, ? extends V> operator) {
    return operateOnData((value, index) -> operator.apply(value, itemList.get(index)));
  }

  public default <I> S operateOnData2(
      List<? extends List<? extends I>> itemList,
      BiFunction<? super V, ? super I, ? extends V> operator) {
    return operateOnData2((value, i, j) -> operator.apply(value, itemList.get(i).get(j)));
  }

  public default S setData2(boolean setByReference, List<? extends List<? extends V>> to) {
    if (setByReference)
      return operateOnData2(to, (V firstOperand, V secondOperand) -> secondOperand);
    else
      return operateOnData2(
          to,
          (V firstOperand, V secondOperand) -> firstOperand.setValue(secondOperand));
  }

  public default S setData(boolean setByReference, List<? extends V> to) {
    if (setByReference)
      return operateOnData(to, (V firstOperand, V secondOperand) -> secondOperand);
    else
      return operateOnData(
          to,
          (V firstOperand, V secondOperand) -> firstOperand.setValue(secondOperand));
  }

  @SuppressWarnings("unchecked")
  public default S setData(boolean setByReference, V... to) {
    return setData(setByReference, Arrays.asList(to));
  }

  public default S setData(Number... values) {
    return operateOnData(
        Arrays.asList(values),
        (assignee, assignment) -> assignee.setValue(assignment));
  }

  public default S setData(Value<?>... to) {
    return setData(Arrays.asList(to));
  }

  public default S setData(int[] to) {
    return operateOnData((value, index) -> value.setValue(to[index]));
  }

  public default S setData(long[] to) {
    return operateOnData((value, index) -> value.setValue(to[index]));
  }

  public default S setData(float[] to) {
    return operateOnData((value, index) -> value.setValue(to[index]));
  }

  public default S setData(double[] to) {
    return operateOnData((value, index) -> value.setValue(to[index]));
  }

  public default S setData2(int[]... to) {
    return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
  }

  public default S setData2(long[]... to) {
    return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
  }

  public default S setData2(float[]... to) {
    return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
  }

  public default S setData2(double[]... to) {
    return operateOnData2((value, i, j) -> value.setValue(to[i][j]));
  }

  @SuppressWarnings("unchecked")
  public default S setData2(boolean copyByReference, Vector<?, V>... to) {
    return setData2(
        copyByReference,
        new ListTransformationView<>(Arrays.asList(to), i -> i.getData()));
  }

  public default S setData2(Vector<?, ?>... values) {
    return setData2(new ListTransformationView<>(Arrays.asList(values), i -> i.getData()));
  }

  public default S setData2(List<? extends List<? extends Value<?>>> to) {
    return operateOnData2(to, (assignee, assignment) -> assignee.setValue(assignment));
  }

  public default S setData(List<? extends Value<?>> to) {
    return operateOnData(to, (assignee, assignment) -> assignee.setValue(assignment));
  }
}
