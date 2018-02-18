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

import static uk.co.strangeskies.fx.FxUtilities.getResource;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.util.Callback;
import javafx.util.Pair;

/**
 * A simple utility class for simplifying FXML resource loading. Common
 * configuration is avoided by providing behavior according to conventions.
 * 
 * @author Elias N Vasylenko
 *
 * @param <C>
 *          the controller type
 */
public class FxmlLoadBuilder<C> {
  private final FXMLLoader loader;

  private C controller;
  private Class<? extends C> controllerClass;
  private Node root;
  private URL resource;
  private String resourceName;

  /**
   * @param loader
   *          The FXML loader to use
   */
  protected FxmlLoadBuilder(FXMLLoader loader) {
    this.loader = loader;
  }

  /**
   * @param loader
   *          the {@link FXMLLoader} to back the builder
   * @return an {@link FxmlLoadBuilder} over the given backing loader
   */
  public static FxmlLoadBuilder<Object> buildWith(FXMLLoader loader) {
    return new FxmlLoadBuilder<>(loader);
  }

  /**
   * @return an {@link FxmlLoadBuilder} over the default backing loader
   */
  public static FxmlLoadBuilder<Object> build() {
    return buildWith(new FXMLLoader());
  }

  /**
   * Configure the builder to load directly into the given controller instance.
   * 
   * <p>
   * Unless an {@link #resource(URL) exact resource} is given, the resource
   * location will be derived according to {@link FxUtilities#getResource(Class)},
   * or {@link FxUtilities#getResource(Class, String)} if a
   * {@link #resource(String) resource name} is specified.
   * 
   * @param <D>
   *          the type of the controller object
   * @param controller
   *          the controller object to load into
   * @return the receiving instance
   */
  @SuppressWarnings("unchecked")
  public <D> FxmlLoadBuilder<D> controller(D controller) {
    this.controller = (C) controller;
    controllerClass = (Class<? extends C>) controller.getClass();

    return (FxmlLoadBuilder<D>) this;
  }

  /**
   * Configure the builder to load directly into the given controller instance.
   * 
   * <p>
   * Unless an {@link #resource(URL) exact resource} is given, the resource
   * location will be derived according to {@link FxUtilities#getResource(Class)},
   * or {@link FxUtilities#getResource(Class, String)} if a
   * {@link #resource(String) resource name} is specified.
   * 
   * @param <D>
   *          the type of the controller object
   * @param controllerClass
   *          the controller class to load into
   * @param controller
   *          the controller object to load into
   * @return the receiving instance
   */
  @SuppressWarnings("unchecked")
  public <D> FxmlLoadBuilder<D> controller(Class<D> controllerClass, D controller) {
    this.controller = (C) controller;
    this.controllerClass = (Class<? extends C>) controllerClass;

    return (FxmlLoadBuilder<D>) this;
  }

  /**
   * Configure the builder to load into a controller of the given class via
   * {@link FXMLLoader#setControllerFactory(javafx.util.Callback)}.
   * 
   * <p>
   * Unless an {@link #resource(URL) exact resource} is given, the resource
   * location will be derived according to {@link FxUtilities#getResource(Class)},
   * or {@link FxUtilities#getResource(Class, String)} if a
   * {@link #resource(String) resource name} is specified.
   * 
   * @param <D>
   *          the type of the controller class
   * @param controllerClass
   *          the controller class to load into
   * @return the receiving instance
   */
  @SuppressWarnings("unchecked")
  public <D> FxmlLoadBuilder<D> controller(Class<D> controllerClass) {
    this.controllerClass = (Class<? extends C>) controllerClass;
    controller = null;

    return (FxmlLoadBuilder<D>) this;
  }

  /**
   * The FXML resource name, such that the resource be located according to
   * {@link FxUtilities#getResource(Class, String)}.
   * 
   * @param resourceName
   *          the name of the FXML resource
   * @return the receiving instance
   */
  public FxmlLoadBuilder<C> resource(String resourceName) {
    this.resourceName = resourceName;
    resource = null;

    return this;
  }

  /**
   * @param resource
   *          the exact FXML resource
   * @return the receiving instance
   */
  public FxmlLoadBuilder<C> resource(URL resource) {
    this.resource = resource;
    this.resourceName = null;

    return this;
  }

  /**
   * @param root
   *          the root node to load the FXML into
   * @return the receiving instance
   */
  public FxmlLoadBuilder<C> root(Node root) {
    this.root = root;

    return this;
  }

  /**
   * @param classLoader
   *          the new class loader for the {@link FXMLLoader}.
   * @return the receiving instance
   */
  public FxmlLoadBuilder<C> classLoader(ClassLoader classLoader) {
    loader.setClassLoader(classLoader);

    return this;
  }

  /**
   * @param <D>
   *          the type of the controller/root object
   * @param object
   *          the root node <em>and</em> the controller to load the FXML into
   * @return the receiving instance
   */
  public <D extends Node> FxmlLoadBuilder<D> object(D object) {
    return controller(object).root(object);
  }

  /**
   * {@link #load() Load} the FXML item and return the root node.
   * 
   * @param <T>
   *          the expected type of the root node
   * 
   * @return the resulting root node
   */
  @SuppressWarnings("unchecked")
  public <T> T loadRoot() {
    load();

    return (T) root;
  }

  /**
   * {@link #load() Load} the FXML item and return the controller.
   * 
   * @return the resulting controller
   */
  public C loadController() {
    load();

    return controller;
  }

  /**
   * Load the TODO
   * 
   * @return a pair containing the controller object and the root node object
   */
  public Pair<C, Node> load() {
    // set controller
    if (controller != null) {
      loader.setController(controller);
    } else if (controllerClass != null) {
      Callback<Class<?>, Object> originalFactory = loader.getControllerFactory();

      loader.setControllerFactory(c -> {
        if (!c.isAssignableFrom(controllerClass)) {
          throw new ClassCastException();
        } else {
          return originalFactory.call(c);
        }
      });
    }

    // set root
    if (root != null) {
      loader.setRoot(root);
    }

    try {
      // set location
      if (resource == null) {
        if (resourceName != null) {
          resource = getResource(controllerClass, resourceName);
          if (resource == null)
            throw new IllegalStateException(
                "Resource not found for class " + controllerClass + " named " + resourceName);
        } else {
          resource = getResource(controllerClass);
          if (resource == null)
            throw new IllegalStateException("Resource not found for class " + controllerClass);
        }
      }
      loader.setLocation(resource);

      // load
      loader.load();
      controller = loader.getController();
      root = loader.getRoot();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      loader.setRoot(null);
      loader.setController(null);
      loader.setLocation(null);
    }

    return new Pair<>(controller, root);
  }
}
