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
package uk.co.strangeskies.text.parsing;

import java.util.LinkedList;
import java.util.function.Function;

public class ParseStateImpl implements ParseState {
  private final LinkedList<Parser<?>> parserStack;
  private final ParseException furthestException;

  private final String literal;
  private final int fromIndex;
  private final boolean toEnd;

  public ParseStateImpl(String literal) {
    this(new LinkedList<>(), literal, 0, true, null);
  }

  private ParseStateImpl(
      LinkedList<Parser<?>> parserStack,
      String literal,
      int fromIndex,
      boolean toEnd,
      ParseException furthestException) {
    this.parserStack = parserStack;
    this.literal = literal;
    this.fromIndex = fromIndex;
    this.toEnd = toEnd;
    this.furthestException = furthestException;
  }

  public ParseStateImpl(ParseStateImpl state, ParseException furthestException) {
    this(state.parserStack, state.literal, state.fromIndex, state.toEnd, furthestException);
  }

  @Override
  public String literal() {
    return literal;
  }

  @Override
  public int fromIndex() {
    return fromIndex;
  }

  @Override
  public ParseState fromIndex(int fromIndex) {
    return new ParseStateImpl(parserStack, literal, fromIndex, toEnd, furthestException);
  }

  @Override
  public boolean toEnd() {
    return toEnd;
  }

  @Override
  public ParseState toEnd(boolean toEnd) {
    return new ParseStateImpl(parserStack, literal, fromIndex, toEnd, furthestException);
  }

  @Override
  public <T> ParseResult<T> parseTo(int toIndex, Function<String, T> transform) {
    return new ParseResult<>(this, toIndex, literal.substring(fromIndex, toIndex))
        .mapResult(transform);
  }

  @Override
  public ParseException getException() {
    return furthestException;
  }

  @Override
  public ParseState addException(ParseException exception) {
    if (exception == null || (furthestException != null
        && exception.getIndexReached() < furthestException.getIndexReached()))
      exception = furthestException;

    return new ParseStateImpl(this, exception);
  }

  @Override
  public ParseState addException(String message, int indexReached) {
    return addException(message, indexReached, furthestException);
  }

  @Override
  public ParseState addException(String message, int indexReached, Exception cause) {
    return addException(new ParseException(message, literal, fromIndex, indexReached, cause));
  }

  @Override
  public ParseState addException(ParseState state) {
    return addException(state.getException());
  }

  @Override
  public ParseState push(Parser<?> parser) {
    @SuppressWarnings("unchecked")
    LinkedList<Parser<?>> stack = (LinkedList<Parser<?>>) parserStack.clone();
    stack.push(parser);
    return new ParseStateImpl(stack, literal, fromIndex, toEnd, furthestException);
  }

  @Override
  public ParseState pop(Parser<?> expected) {
    @SuppressWarnings("unchecked")
    LinkedList<Parser<?>> stack = (LinkedList<Parser<?>>) parserStack.clone();
    if (stack.pop() != expected)
      throw new ParseException(
          "Illegal parse state exception on completing '" + expected + "'",
          literal,
          fromIndex,
          fromIndex);

    return new ParseStateImpl(stack, literal, fromIndex, toEnd, furthestException);
  }
}
