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
 * This file is part of uk.co.strangeskies.text.
 *
 * uk.co.strangeskies.text is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.properties;

import java.util.Locale;

import uk.co.strangeskies.observable.ObservableProperty;
import uk.co.strangeskies.observable.ObservablePropertyImpl;

/**
 * Management interface over and associate {@link PropertyLoader localizer
 * instance}, allowing the locale of that instance to be changed.
 * <p>
 * A locale manager is observable over changes to its locale.
 * 
 * @author Elias N Vasylenko
 */
public interface LocaleManager extends LocaleProvider, ObservableProperty<Locale> {
  /**
   * As returned by {@link #getDefaultManager()}.
   */
  LocaleManager DEFAULT_MANAGER = new DefaultLocaleManagerImpl();

  /**
   * @param locale
   *          the new locale
   */
  default void setLocale(Locale locale) {
    set(locale);
  }

  /**
   * @return a simple mutable locale manager, with its locale initialised to the
   *         system default
   */
  static LocaleManager getManager() {
    return getManager(Locale.getDefault());
  }

  /**
   * @param locale
   *          the initial locale
   * @return a simple mutable locale manager
   */
  static LocaleManager getManager(Locale locale) {
    return new LocaleManagerImpl(locale);
  }

  /**
   * Create a locale manager based on the system default locale, as returned by
   * {@link Locale#getDefault()}. Changes made to this manager will be forwarded
   * to the system locale setting, and vice versa.
   * <p>
   * Unfortunately there is no standard mechanism to listen for changes to the
   * system locale, so the manager may not stay up to date until it is refreshed
   * by a call to {@link #setLocale(Locale)} or {@link #getLocale()}.
   * 
   * @return a locale manager backed by the system locale
   */
  static LocaleManager getDefaultManager() {
    return DEFAULT_MANAGER;
  }

}

class LocaleManagerImpl extends ObservablePropertyImpl<Locale> implements LocaleManager {
  public LocaleManagerImpl(Locale locale) {
    super(locale);
  }

  @Override
  public synchronized void setLocale(Locale locale) {
    set(locale);
  }

  @Override
  public synchronized Locale getLocale() {
    return get();
  }
}

class DefaultLocaleManagerImpl extends LocaleManagerImpl {
  public DefaultLocaleManagerImpl() {
    super(Locale.getDefault());
  }

  @Override
  public synchronized void setLocale(Locale locale) {
    Locale.setDefault(locale);
    super.setLocale(locale);
  }

  @Override
  public synchronized Locale getLocale() {
    Locale locale = Locale.getDefault();

    /*
     * Actual system locale may have changed, since we can't listen to events, so
     * make sure we're synced here.
     */
    setLocale(locale);

    return locale;
  }
}
