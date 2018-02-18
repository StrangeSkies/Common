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
 * This file is part of uk.co.strangeskies.fx.
 *
 * uk.co.strangeskies.fx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.fx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.fx;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.TransformationList;
import uk.co.strangeskies.collection.computingmap.ComputingHashMap;
import uk.co.strangeskies.collection.computingmap.ComputingMap;
import uk.co.strangeskies.observable.Observable;

/**
 * A collection of general utility methods for working with JavaFX and
 * e(fx)clipse.
 * 
 * @author Elias N Vasylenko
 */
public class FxUtilities {
  private static final String CONTROLLER_STRING = "Controller";

  private FxUtilities() {}

  /**
   * Perform the given action on the JavaFX event thread as soon as possible,
   * returning upon completion. This method is safe whether or not the invoking
   * thread is the event thread.
   * 
   * @param runnable
   *          The action to execute
   */
  public static void runNow(Runnable runnable) {
    runNow(() -> {
      runnable.run();
      return null;
    });
  }

  /**
   * Perform the given action on the JavaFX event thread as soon as possible,
   * returning the result upon completion. This method is safe whether or not the
   * invoking thread is the event thread.
   * 
   * @param <T>
   *          The type of value to result from the action
   * @param runnable
   *          The action to execute
   * @return The result of invoking the given supplier
   */
  public static <T> T runNow(Supplier<T> runnable) {
    if (Platform.isFxApplicationThread()) {
      return runnable.get();
    } else {
      FutureTask<T> task = new FutureTask<>(runnable::get);
      Platform.runLater(task);
      try {
        return task.get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        try {
          throw e.getCause();
        } catch (Error | RuntimeException c) {
          throw c;
        } catch (Throwable t) {
          throw new RuntimeException(t);
        }
      }
    }
  }

  /**
   * Create a new observable list as a transformation of a component observable
   * list.
   * 
   * @param <T>
   *          the type of the elements of the new list
   * @param <U>
   *          the type of the elements of the component list
   * @param component
   *          the component list
   * @param mapper
   *          a mapping from the type of the elements of the component list to the
   *          type of the elements of the new list
   * @return a new observable list, backed by the given list, with each element in
   *         that list transformed according to the given function
   */
  public static <T, U> ObservableList<T> map(ObservableList<U> component, Function<U, T> mapper) {
    return new TransformationList<T, U>(component) {
      @Override
      public T get(int index) {
        return mapper.apply(getSource().get(index));
      }

      @Override
      public int size() {
        return getSource().size();
      }

      @Override
      public int getSourceIndex(int index) {
        return index;
      }

      @Override
      protected void sourceChanged(Change<? extends U> change) {
        fireChange(new Change<T>(this) {
          @Override
          public boolean wasAdded() {
            return change.wasAdded();
          }

          @Override
          public boolean wasRemoved() {
            return change.wasRemoved();
          }

          @Override
          public boolean wasReplaced() {
            return change.wasReplaced();
          }

          @Override
          public boolean wasUpdated() {
            return change.wasUpdated();
          }

          @Override
          public boolean wasPermutated() {
            return change.wasPermutated();
          }

          @Override
          public int getPermutation(int i) {
            return change.getPermutation(i);
          }

          @Override
          protected int[] getPermutation() {
            throw new AssertionError();
          }

          @Override
          public List<T> getRemoved() {
            List<T> removed = new ArrayList<>(change.getRemovedSize());
            for (U element : change.getRemoved()) {
              removed.add(mapper.apply(element));
            }
            return removed;
          }

          @Override
          public int getFrom() {
            return change.getFrom();
          }

          @Override
          public int getTo() {
            return change.getTo();
          }

          @Override
          public boolean next() {
            return change.next();
          }

          @Override
          public void reset() {
            change.reset();
          }
        });
      }
    };
  }

  /**
   * Create a new observable set as a transformation of a component observable
   * set.
   * 
   * @param <T>
   *          the type of the elements of the new set
   * @param <U>
   *          the type of the elements of the component set
   * @param component
   *          the component set
   * @param mapper
   *          a mapping from the type of the elements of the component set to the
   *          type of the elements of the new set
   * @return a new observable set, backed by the given set, with each element in
   *         that set transformed according to the given function
   */
  public static <T, U> ObservableSet<T> map(ObservableSet<U> component, Function<U, T> mapper) {
    @SuppressWarnings("unchecked")
    ObservableSet<T> set = FXCollections.observableSet();

    ComputingMap<U, T> map = new ComputingHashMap<>(mapper);

    component.addListener((SetChangeListener<U>) event -> {
      if (event.wasAdded()) {
        set.add(map.putGet(event.getElementAdded()));
      }

      if (event.wasRemoved()) {
        T removed = map.removeGet(event.getElementRemoved());
        if (!map.values().contains(removed)) {
          set.remove(event.getElementRemoved());
        }
      }
    });

    return FXCollections.unmodifiableObservableSet(set);
  }

  /**
   * Create an observable list view of an observable set.
   * 
   * @param <T>
   *          the type of the elements of the set
   * @param component
   *          the set to wrap as a list
   * @return a list whose contents are backed by the given set
   */
  public static <T> ObservableList<T> asList(ObservableSet<T> component) {
    ObservableList<T> list = FXCollections.observableArrayList();

    component.addListener((SetChangeListener<T>) event -> {
      if (event.wasAdded()) {
        list.add(event.getElementAdded());
      }

      if (event.wasRemoved()) {
        list.remove(event.getElementRemoved());
      }
    });

    list.addAll(component);

    return FXCollections.unmodifiableObservableList(list);
  }

  /**
   * Create an observable set view of an observable list.
   * 
   * @param <T>
   *          the type of the elements of the list
   * @param component
   *          the list to wrap as a set
   * @return a set whose contents are backed by the given list
   */
  public static <T> ObservableSet<T> asSet(ObservableList<T> component) {
    @SuppressWarnings("unchecked")
    ObservableSet<T> set = FXCollections.observableSet();

    component.addListener((ListChangeListener<T>) event -> {
      boolean removed = false;

      while (event.next()) {
        if (event.wasAdded()) {
          set.addAll(event.getAddedSubList());
        } else if (event.wasRemoved()) {
          /*
           * We can't simply remove everything, as the list may still contain duplicates
           * of those removed duplicates
           */
          removed = true;
        }
      }

      if (removed) {
        set.retainAll(component);
      }
    });

    set.addAll(component);

    return FXCollections.unmodifiableObservableSet(set);
  }

  /**
   * Wrap an {@link uk.co.strangeskies.observable.Observable} with a JavaFX
   * equivalent {@link ObservableValue}.
   * 
   * @param <T>
   *          the type of the value
   * @param observable
   *          the observable value to wrap
   * @param initial
   *          the initial value of the wrapping observable value
   * @return a JavaFX observable value backed by the given observable
   */
  public static <T> ObservableValue<T> wrap(Observable<T> observable, T initial) {
    ObjectProperty<T> property = new SimpleObjectProperty<>(initial);

    observable.weakReference(property).observe(m -> m.owner().set(m.message()));

    return property;
  }

  /**
   * Wrap an {@link uk.co.strangeskies.observable.ObservableValue} with a JavaFX
   * equivalent {@link ObservableValue}.
   * 
   * @param <T>
   *          the type of the value
   * @param observable
   *          the observable value to wrap
   * @return a JavaFX observable value backed by the given observable value
   */
  public static <T> ObservableValue<T> wrap(
      uk.co.strangeskies.observable.ObservableValue<T> observable) {
    return wrap(observable, observable.get());
  }

  /**
   * Find the {@code .fxml} resource associated with a given controller class by
   * location and naming conventions. The location of the file is assumed to be
   * the same package as the controller class. The name of the file is determined
   * according to the convention described by {@link #getResourceName(Class)}.
   * 
   * @param controllerClass
   *          The controller class whose resource we wish to locate
   * @return The URL for the resource associated with the given controller class.
   */
  public static URL getResource(Class<?> controllerClass) {
    return getResource(controllerClass, getResourceName(controllerClass));
  }

  /**
   * Find the name of the {@code .fxml} resource associated with a given
   * controller class by convention. The name of the file is assumed to be
   * {@code [classname].fxml}, or if {@code [classname]} takes the form
   * {@code [classnameprefix]Controller}, the name of the file is assumed to be
   * {@code [classnameprefix].fxml}.
   * 
   * @param controllerClass
   *          The controller class whose resource we wish to locate
   * @return The URL for the resource associated with the given controller class.
   */
  public static String getResourceName(Class<?> controllerClass) {
    String resourceName = controllerClass.getSimpleName();

    if (resourceName.endsWith(CONTROLLER_STRING)) {
      resourceName = resourceName.substring(0, resourceName.length() - CONTROLLER_STRING.length());
    }

    return resourceName;
  }

  /**
   * Find the {@code .fxml} resource for a given controller class by location
   * conventions. The location of the file is assumed to be the same package as
   * the controller class.
   * 
   * @param controllerClass
   *          The controller class whose resource we wish to locate
   * @param resourceName
   *          The name of the resource file
   * @return The URL for the resource associated with the given controller class.
   */
  public static URL getResource(Class<?> controllerClass, String resourceName) {
    String resourceLocation = "/" + controllerClass.getPackage().getName().replace('.', '/') + "/"
        + resourceName + ".fxml";

    return controllerClass.getResource(resourceLocation);
  }
}
