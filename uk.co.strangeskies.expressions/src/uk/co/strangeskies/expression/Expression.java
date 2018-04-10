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
 * This file is part of uk.co.strangeskies.expressions.
 *
 * uk.co.strangeskies.expressions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.expressions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.expression;

import java.util.function.Supplier;

import uk.co.strangeskies.observable.HotObservable;
import uk.co.strangeskies.observable.ImmutableObservable;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.ObservableValue;
import uk.co.strangeskies.observable.Observer;

/**
 * <p>
 * An expression for use in reactive programming. An expression has a
 * <em>value</em> which can be <em>resolved</em> through the invocation of
 * {@link #getValue()} or {@link #decoupleValue()}.
 * 
 * <p>
 * This class is intended to be {@link Observable} over a specific behavior: its
 * {@link Observer}s should be notified any time the value which would be
 * resolved from the expression becomes different from the previously-resolved,
 * last-known value.
 * 
 * <p>
 * Any mechanism of synchronization, though a common concern, is left entirely
 * to the discretion of implementations.
 * 
 * <p>
 * Note that the concept of an {@link Expression} differs from the concept of a
 * {@link ObservableValue} in that an expression is observable over itself,
 * rather than being observable over the value it represents. This is because
 * expressions are more typically intended to be lazily evaluated, and observers
 * directly over the value would cause eager evaluation. The methods
 * {@link #over(ObservableValue)} and {@link #over(Observable, Object)} are
 * provided to bridge the gap from {@link Observable} to {@link Expression}.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the value of this expression
 */
public interface Expression<T> {
  Observable<Expression<? extends T>> invalidations();

  /**
   * This should always return the correct current value for this Expression. Be
   * careful to remember that the reference returned should not be able to mutate
   * this {@link Expression}, i.e. it should be either an immutable class, a const
   * reference, or a copy of the underlying value. This is important, but
   * conversely it does <em>not</em> mean that the return value can necessarily be
   * relied upon not to not mutate when this expression is updated, unless a read
   * lock is held.
   *
   * <p>
   * Once a value has been returned, it is up to the implementing Expression as to
   * whether the value will be reliable such that it will remain the same even if
   * the conceptual value of this expression subsequently changes, or whether it
   * will update automatically with the expression. Please only rely on either
   * behavior if it is explicitly documented, otherwise use
   * {@link #decoupleValue()} if you need a persistent reference which is safe
   * <em>to</em> mutate and/or safe <em>from</em> external mutation.
   * 
   * <p>
   * The observers should only ever be notified of an update from the thread which
   * has the write lock on an {@link Expression}, and {@link Expression}s should
   * be careful to only notify observers when they are in a state where their
   * value can be fetched. Immediate fetch is discouraged, though. Expressions
   * should generally update lazily, not eagerly.
   * 
   * @return The fully evaluated value of this Expression at the time of method
   *         invocation.
   */
  T getValue();

  /**
   * @return a value which is equal to the result of {@link #getValue()} at time
   *         of invocation, with the added guarantee that it will not be further
   *         mutated by this {@link Expression}
   */
  default T decoupleValue() {
    return getValue();
  }

  /**
   * Create an immutable {@link Expression} instance whose value is always that
   * given, and upon which read locks are always available. Further, an observer
   * set is not maintained, as observers will never receive notifications, so
   * attempting to add or remove them will always return {@code true}.
   * 
   * @param <T>
   *          the type of the expression
   * @param value
   *          the value of the new immutable {@link Expression}
   * @return an immutable {@link Expression} instance whose value is always that
   *         given, and upon which read locks are always available
   */
  static <T> AnonymousExpression<T> immutable(T value) {
    return new ImmutableExpressionImpl<>(value);
  }

  /**
   * Provide an expression view over an observable value.
   * 
   * @param changes
   *          the observable providing the value updates
   * @param initialValue
   *          the initial expression value
   * @return an observable with the given initial value, and whose value changes
   *         as per the given observable
   */
  static <T> AnonymousExpression<T> over(Observable<T> changes, T initialValue) {
    IdentityExpression<T> expression = new IdentityExpression<>(initialValue);

    changes.weakReference(expression).observe(m -> m.owner().set(m.message()));

    return expression.anonymize();
  }

  /**
   * Provide an expression view over an observable value.
   * 
   * @param value
   *          the observable providing the initial value, and the value updates
   * @return an observable with the given initial value, and whose value changes
   *         as per the given observable
   */
  static <T> AnonymousExpression<T> over(ObservableValue<T> value) {
    return new AnonymousExpressionImpl<>(value);
  }

  /**
   * @return an anonymous view of the expression
   */
  default AnonymousExpression<T> anonymize() {
    return new AnonymousExpressionImpl<>(this);
  }
}

/*
 * TODO with Valhalla we can probably make this a value type to just about get
 * rid of the overhead
 */
class AnonymousExpressionImpl<T> extends HotObservable<Expression<? extends T>>
    implements AnonymousExpression<T> {
  private final Supplier<T> base;

  AnonymousExpressionImpl(Expression<T> base) {
    this.base = base::getValue;

    base.invalidations().observe(b -> next(this));
  }

  AnonymousExpressionImpl(ObservableValue<T> base) {
    this.base = base::get;

    base.observe(b -> next(this));
  }

  @Override
  public Observable<Expression<? extends T>> invalidations() {
    return this;
  }

  @Override
  public T getValue() {
    return base.get();
  }
}

class ImmutableExpressionImpl<T> extends ImmutableObservable<Expression<? extends T>>
    implements Expression<T>, AnonymousExpression<T> {
  private final T value;

  ImmutableExpressionImpl(T value) {
    this.value = value;
  }

  @Override
  public T getValue() {
    return value;
  }

  @Override
  public Observable<Expression<? extends T>> invalidations() {
    return this;
  }
}
