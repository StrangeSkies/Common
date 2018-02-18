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
 * This file is part of uk.co.strangeskies.text.provider.
 *
 * uk.co.strangeskies.text.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.text.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.text.provider;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import uk.co.strangeskies.observable.Disposable;
import uk.co.strangeskies.observable.Observable;
import uk.co.strangeskies.observable.Observer;
import uk.co.strangeskies.text.properties.LocaleManager;
import uk.co.strangeskies.text.properties.LocaleProvider;
import uk.co.strangeskies.text.provider.LocaleManagerService.LocaleManagerConfiguration;

/**
 * A locale manager configurable via the config admin service.
 * 
 * @author Elias N Vasylenko
 */
@Designate(ocd = LocaleManagerConfiguration.class)
@Component(
    name = LocaleManagerService.CONFIGURATION_PID,
    configurationPid = LocaleManagerService.CONFIGURATION_PID)
public class LocaleManagerService implements LocaleManager, LocaleProvider {
  @SuppressWarnings("javadoc")
  @ObjectClassDefinition(
      id = CONFIGURATION_PID,
      name = "Locale Configuration",
      description = "The configuration for the user locale for the application")
  public @interface LocaleManagerConfiguration {
    @AttributeDefinition(name = "Locale", description = "The user locale for the application")
    String locale() default "";
  }

  @Reference
  private ConfigurationAdmin configurationAdmin;

  /**
   * Configuration pid for OSGi configuration.
   */
  static final String CONFIGURATION_PID = "uk.co.strangeskies.text.locale.manager";
  /**
   * Key for locale setting string, in the format specified by
   * {@link Locale#forLanguageTag(String)}.
   */
  static final String LOCALE_KEY = "locale";

  private final LocaleManager component;

  /**
   * Create empty manager
   */
  public LocaleManagerService() {
    component = LocaleManager.getManager(Locale.getDefault());
  }

  @Override
  public Locale get() {
    return component.getLocale();
  }

  @Override
  public Locale set(Locale locale) {
    Locale previous = setImpl(locale);

    if (!previous.equals(locale)) {
      try {
        Configuration configuration = configurationAdmin.getConfiguration(CONFIGURATION_PID);

        Dictionary<String, Object> properties = configuration.getProperties();
        properties.put(LOCALE_KEY, locale.toLanguageTag());
        configuration.update(properties);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return previous;
  }

  private Locale setImpl(Locale locale) {
    return component.set(locale);
  }

  @Activate
  void activate(LocaleManagerConfiguration configuration) {
    update(configuration);
  }

  @Modified
  void update(LocaleManagerConfiguration configuration) {
    Locale locale;

    String localeString = configuration.locale();
    if (localeString.equals(""))
      locale = Locale.getDefault();
    else
      locale = Locale.forLanguageTag(localeString);
    setImpl(locale);
  }

  @Override
  public Disposable observe(Observer<? super Locale> observer) {
    return component.observe();
  }

  @Override
  public Observable<Change<Locale>> changes() {
    return component.changes();
  }

  @Override
  public void setProblem(Throwable t) {
    component.setProblem(t);
  }
}
