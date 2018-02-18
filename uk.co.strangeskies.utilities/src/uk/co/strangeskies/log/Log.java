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
package uk.co.strangeskies.log;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A log interface for clients to write events to.
 * 
 * @author Elias N Vasylenko
 */
public interface Log {
  /**
   * The level of importance of a log entry.
   *
   * @author Elias N Vasylenko
   */
  public enum Level {
    /**
     * Trace level – Huge output
     */
    TRACE,
    /**
     * Debug level – Very large output
     */
    DEBUG,
    /**
     * Info – Provide information about processes that go ok
     */
    INFO,
    /**
     * Warning – A failure or unwanted situation that is not blocking
     */
    WARN,
    /**
     * Error – An error situation
     */
    ERROR
  }

  /**
   * Log a message.
   * 
   * @param level
   *          The importance level of the given message
   * @param message
   *          The message to log
   */
  void log(Level level, String message);

  /**
   * Log a message and an associated throwable.
   * 
   * @param level
   *          The importance level of the given message
   * @param message
   *          The message to log
   * @param exception
   *          The exception associated with the message
   */
  void log(Level level, String message, Throwable exception);

  /**
   * Log a message and an associated throwable.
   * 
   * @param level
   *          The importance level of the given message
   * @param exception
   *          The exception associated with the message
   */
  void log(Level level, Throwable exception);

  /**
   * Create a simple log which derives messages according to exceptions and
   * otherwise discards exception information.
   * 
   * @param action
   *          the action upon receiving the level and derived message string for a
   *          log event
   * @return a log implementing the given behavior
   */
  static Log simpleLog(BiConsumer<Level, String> action) {
    return new Log() {
      @Override
      public void log(Level level, String message, Throwable exception) {
        log(level, message + ": " + exception.getMessage());
      }

      @Override
      public void log(Level level, Throwable exception) {
        log(level, exception.getMessage());
      }

      @Override
      public void log(Level level, String message) {
        action.accept(level, message);
      }
    };
  }

  /**
   * Create a simple log which derives messages according to levels and exceptions
   * and otherwise discards level and exception information.
   * 
   * @param action
   *          the action upon receiving the derived message string for a log event
   * @return a log implementing the given behavior
   */
  static Log simpleLog(Consumer<String> action) {
    return new Log() {
      @Override
      public void log(Level level, String message, Throwable exception) {
        log(level, message + ": " + exception.getMessage());
      }

      @Override
      public void log(Level level, Throwable exception) {
        log(level, exception.getMessage(), exception);
      }

      @Override
      public void log(Level level, String message) {
        action.accept("[" + level.toString() + "] " + message);
      }
    };
  }

  /**
   * Derive a log which defers to the given if available, or otherwise discards
   * the event.
   * 
   * @param log
   *          a nullable reference to a log instance
   * @return the derived log
   */
  static Log forwardingLog(Log log) {
    return forwardingLog(() -> log);
  }

  /**
   * Derive a log which defers to the log obtained from the given supplier when
   * available, or otherwise discards the event.
   * 
   * @param logSupplier
   *          an supplier which may be able to find a log to defer to
   * @return the derived log
   */
  static Log forwardingLog(Supplier<Log> logSupplier) {
    return new Log() {
      @Override
      public void log(Level level, String message) {
        Optional.ofNullable(logSupplier.get()).ifPresent(l -> l.log(level, message));
      }

      @Override
      public void log(Level level, Throwable exception) {
        Optional.ofNullable(logSupplier.get()).ifPresent(l -> l.log(level, exception));
      }

      @Override
      public void log(Level level, String message, Throwable exception) {
        Optional.ofNullable(logSupplier.get()).ifPresent(l -> l.log(level, message, exception));
      }
    };
  }

  /**
   * @return a log which discards all events
   */
  static Log discardingLog() {
    return new Log() {
      @Override
      public void log(Level level, String message) {}

      @Override
      public void log(Level level, Throwable exception) {}

      @Override
      public void log(Level level, String message, Throwable exception) {}
    };
  }

  /**
   * Derive a log which defers to the receiver, after modifying the string message
   * according to the given function.
   * 
   * @param mapping
   *          the mapping function to modify the message strings
   * @return the derived log
   */
  default Log mapMessage(Function<String, String> mapping) {
    Log component = this;

    return new Log() {
      @Override
      public void log(Level level, String message) {
        component.log(level, mapping.apply(message));
      }

      @Override
      public void log(Level level, Throwable exception) {
        component.log(level, exception);
      }

      @Override
      public void log(Level level, String message, Throwable exception) {
        component.log(level, mapping.apply(message), exception);
      }
    };
  }

  /**
   * Derive a log which defers to the receiver, after mapping events of level
   * {@code from} to events of level {@code to}.
   * 
   * @param from
   *          the level to map from
   * @param to
   *          the level to map to
   * @return the derived log
   */
  default Log mapLevel(Level from, Level to) {
    Log component = this;

    return new Log() {
      @Override
      public void log(Level level, String message) {
        component.log(level == from ? to : level, message);
      }

      @Override
      public void log(Level level, Throwable exception) {
        component.log(level == from ? to : level, exception);
      }

      @Override
      public void log(Level level, String message, Throwable exception) {
        component.log(level == from ? to : level, message, exception);
      }
    };
  }

  /**
   * Derive a log which defers to the receiver, after modifying the throwable
   * according to the given function where relevant.
   * 
   * @param mapping
   *          the mapping function to modify the logged exceptions
   * @return the derived log
   */
  default Log mapThrowable(Function<Throwable, Throwable> mapping) {
    Log component = this;

    return new Log() {
      @Override
      public void log(Level level, String message) {
        component.log(level, message);
      }

      @Override
      public void log(Level level, Throwable exception) {
        component.log(level, mapping.apply(exception));
      }

      @Override
      public void log(Level level, String message, Throwable exception) {
        component.log(level, message, mapping.apply(exception));
      }
    };
  }
}
