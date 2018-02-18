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
 * This file is part of uk.co.strangeskies.observable.
 *
 * uk.co.strangeskies.observable is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.observable is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.observable;

import static java.util.Objects.requireNonNull;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class ReferenceOwnedObserver<O, M> extends PassthroughObserver<M, OwnedMessage<O, M>> {
  public static <O, M> ReferenceOwnedObserver<O, M> weak(
      O owner,
      Observer<? super OwnedMessage<O, M>> downstreamObserver) {
    return new ReferenceOwnedObserver<>(owner, downstreamObserver, WeakReference::new);
  }

  public static <O, M> ReferenceOwnedObserver<O, M> soft(
      O owner,
      Observer<? super OwnedMessage<O, M>> downstreamObserver) {
    return new ReferenceOwnedObserver<>(owner, downstreamObserver, SoftReference::new);
  }

  private final Reference<O> ownerReference;

  protected ReferenceOwnedObserver(
      O owner,
      Observer<? super OwnedMessage<O, M>> downstreamObserver,
      Function<O, Reference<O>> referenceFunction) {
    super(downstreamObserver);
    this.ownerReference = requireNonNull(referenceFunction.apply(owner));
  }

  public void withOwner(Consumer<O> action) {
    O owner = ownerReference.get();
    if (owner != null) {
      action.accept(owner);
    } else {
      getObservation().cancel();
    }
  }

  @Override
  public void onNext(M message) {
    withOwner(o -> getDownstreamObserver().onNext(new OwnedMessage<O, M>() {
      @Override
      public O owner() {
        return o;
      }

      @Override
      public M message() {
        return message;
      }
    }));
  }
}
