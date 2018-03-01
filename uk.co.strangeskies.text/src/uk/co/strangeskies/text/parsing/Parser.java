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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.property.IdentityProperty;

/**
 * A simple text parser, composable by way of fluent and type-safe API. This
 * class is intended for fairly simple use-cases.
 * <p>
 * Usage should be intuitive enough for users familiar with context free
 * grammars, where a {@link Parser} instance represents a single symbol, and all
 * production rules on that symbol. {@link Parser} instances should be
 * immutable.
 * <p>
 * By this interpretation, production rules for new symbols can be derived by
 * appending and prepending text, or other symbols, to existing symbols.
 * Multiple production rules can effectively be created for a new symbol by
 * "piping" production rules through {@link #orElse(Parser)} and
 * {@link #orElse(Supplier)}.
 * <p>
 * For the sake of simplicity, all parsers are composed and evaluated greedily
 * from left to right, unless otherwise noted.
 * <p>
 * Parsing is one way. Composition should be dealt with by other means, as it is
 * expected to be a far simpler process in itself but support would add a
 * significant amount of complexity to this API.
 * 
 * @author Elias N Vasylenko
 * @param <T>
 *          the type of the object created by successful application of the
 *          {@link Parser} to a piece of text
 */
public interface Parser<T> {
	/**
	 * Parser for matching a whole string.
	 */
	Parser<String> MATCHING_ALL = new AbstractParser<String>() {
		@Override
		public ParseResult<String> parseSubstringImpl(ParseState state) {
			return state.parseTo(state.literal().length(), Function.identity());
		}

		@Override
		public String toString() {
			return "whole string";
		}
	};

	/**
	 * Convenience method to create a {@link Parser} which accepts a list of text
	 * items, delimited by the given string, which are each parsable according to
	 * the given parser.
	 * 
	 * @param <T>
	 *          The type of the objects to be parsed as elements of the list.
	 * @param element
	 *          The parser responsible for parsing the elements of the list.
	 * @param delimiter
	 *          The string delimiter between parsable text items in the list. This
	 *          may be set to an empty string.
	 * @return A newly derived {@link Parser} which will accept a {@link String}
	 *         representation of a list of items, and parse it into a {@link List}
	 *         instance containing objects parsed from its elements.
	 */
	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter) {
		return list(element, delimiter, 0);
	}

	/**
	 * Convenience method to create a {@link Parser} which accepts a list of text
	 * items, delimited by the given string, which are each parsable according to
	 * the given parser.
	 * 
	 * @param <T>
	 *          The type of the objects to be parsed as elements of the list.
	 * @param element
	 *          The parser responsible for parsing the elements of the list.
	 * @param delimiter
	 *          The string delimiter between parsable text items in the list. This
	 *          may be set to an empty string.
	 * @param minimum
	 *          The minimum length of the list to be parsed.
	 * @return A newly derived {@link Parser} which will accept a {@link String}
	 *         representation of a list of items, and parse it into a {@link List}
	 *         instance containing objects parsed from its elements.
	 */
	public static <T> Parser<List<T>> list(Parser<T> element, String delimiter, int minimum) {
		IdentityProperty<Parser<List<T>>> listParser = new IdentityProperty<>();

		listParser.set(
				Parser
						.proxy(listParser::get)
						.prepend(delimiter)
						.orElse(ArrayList::new)
						.prepend(element, (l, e) -> l.add(0, e))
						.orElse(ArrayList::new));

		for (int i = 0; i < minimum; i++)
			listParser.set(listParser.get().prepend(delimiter).prepend(element, List::add));

		return listParser.get();
	}

	/**
	 * Create a trivial parser matching a piece of text to a regular expression
	 * and then returning that text unmodified.
	 * 
	 * @param regex
	 *          The regular expression matcher
	 * @return A new {@link Parser} instance to match the regular expression
	 */
	public static Parser<String> matching(String regex) {
		return new RegexParser<>(regex, Function.identity());
	}

	/**
	 * @return a parser for matching a whole string
	 */
	public static Parser<String> matchingAll() {
		return MATCHING_ALL;
	}

	/**
	 * @param transformation
	 *          a transformation to apply to the string
	 * @return a parser for matching a whole string
	 */
	public static <T> Parser<T> matchingAll(Function<String, T> transformation) {
		return MATCHING_ALL.transform(transformation);
	}

	/**
	 * Create a trivial parser matching an empty piece of text and returning a
	 * supplied object.
	 * 
	 * @param <T>
	 *          The type of the object to supply
	 * @param supplier
	 *          The object to supply when matching an empty piece of text
	 * @return A new {@link Parser} instance to supply the given object
	 */
	public static <T> Parser<T> from(Supplier<T> supplier) {
		return new RegexParser<>("", s -> supplier.get());
	}

	/**
	 * Create a proxy of a parser supplied at a future time.
	 * <p>
	 * The supplier will only be invoked at the time of parsing, meaning this
	 * method can be used to build self-mentioning parsers. At this time, the
	 * parser returned by this method will behave in an identical manner to the
	 * supplier parser.
	 * 
	 * @param <T>
	 *          the type of the object to supply
	 * @param parser
	 *          the supplier of a parser
	 * @return a new {@link Parser} instance to proxy the supplied parser
	 */
	public static <T> Parser<T> proxy(Supplier<Parser<T>> parser) {
		return new ParserProxy<>(parser, Function.identity());
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will match the same text as the given parser, but the object
	 * parsed will be transformed by the given {@link Function}.
	 * 
	 * @param <U>
	 *          the type of the object of the new parser, post-transformation
	 * @param transform
	 *          the transformation to apply to the object given by application of
	 *          this parser
	 * @return a new {@link Parser} instance which applies the given
	 *         transformation
	 */
	<U> Parser<U> transform(Function<? super T, ? extends U> transform);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will attempt to match a piece of text in the same manner as
	 * the receiving parser. If the attempt fails, the text matched, or partially
	 * matched, by the original attempt will remain unconsumed, and the parser
	 * will instead return the given value.
	 * 
	 * @param onFailure
	 *          the object the new parser should return if application of the
	 *          original parser fails
	 * @return a new {@link Parser} instance which includes the given failure
	 *         guard
	 */
	default Parser<T> orElse(T onFailure) {
		return orElse(() -> onFailure);
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will attempt to match a piece of text in the same manner as
	 * the receiving parser. If the attempt fails, the text matched, or partially
	 * matched, by the original attempt will remain unconsumed, and the parser
	 * will instead return a value from the given supplier.
	 * 
	 * @param onFailure
	 *          The supplier for the object the new parser should return if
	 *          application of the original parser fails
	 * @return A new {@link Parser} instance which includes the given failure
	 *         guard
	 */
	Parser<T> orElse(Supplier<? extends T> onFailure);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will attempt to match a piece of text in the same manner as
	 * the receiving parser. If the attempt fails, the text matched, or partially
	 * matched, by the original attempt will remain unconsumed, then an attempt
	 * will be made to instead apply the given parser.
	 * 
	 * @param onFailure
	 *          The parser which should be applied if and when application of the
	 *          original parser fails
	 * @return A new {@link Parser} instance which includes the given failure
	 *         guard
	 */
	Parser<T> orElse(Parser<? extends T> onFailure);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then match the start of any subsequent text according to
	 * the given pattern.
	 * <p>
	 * The appended pattern contains no parsable data, and will not change the
	 * parsed object upon matching.
	 * 
	 * @param pattern
	 *          A pattern matching the text which should immediately follow from
	 *          the text matched by this parser
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended text pattern
	 */
	default Parser<T> append(String pattern) {
		return append(pattern, (t, s) -> {});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then match the start of any subsequent text according to
	 * the given pattern.
	 * <p>
	 * Upon success, the text matched by the appended pattern will be transformed,
	 * along with the result of applying the receiving parser, into a new result
	 * according to the given function.
	 * 
	 * @param <U>
	 *          The type of the new parse result
	 * @param pattern
	 *          A pattern matching the text which should immediately follow from
	 *          the text matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the appended text, and transforming them into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended text pattern
	 */
	<U> Parser<U> appendTransform(String pattern, BiFunction<T, String, ? extends U> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then match the start of any subsequent text according to
	 * the given pattern.
	 * <p>
	 * Upon success, the text matched by the appended pattern will be consumed,
	 * along with the result of applying the receiving parser, allowing the text
	 * to inform mutation of the parse result's state.
	 * 
	 * @param pattern
	 *          A pattern matching the text which should immediately follow from
	 *          the text matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the appended text, and transforming them into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended text pattern
	 */
	default Parser<T> append(String pattern, BiConsumer<T, String> incorporate) {
		return appendTransform(pattern, (t, s) -> {
			incorporate.accept(t, s);
			return t;
		});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given pattern, then match the start of any subsequent text according to the
	 * receiving parser.
	 * <p>
	 * The appended pattern contains no parsable data, and will not change the
	 * parsed object upon matching.
	 * 
	 * @param pattern
	 *          A pattern matching the text which should immediately follow from
	 *          the text matched by this parser
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended text pattern
	 */
	default Parser<T> prepend(String pattern) {
		return prepend(pattern, (s, t) -> {});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given pattern, then match the start of any subsequent text according to the
	 * receiving parser.
	 * <p>
	 * Upon success, the text matched by the appended pattern will be transformed,
	 * along with the result of applying the receiving parser, into a new result
	 * according to the given function.
	 * 
	 * @param <U>
	 *          The type of the new parse result
	 * @param pattern
	 *          A pattern matching the text which should immediately precede the
	 *          text matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the prepending text, and transforming them into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         prepended text pattern
	 */
	<U> Parser<U> prependTransform(String pattern, BiFunction<T, String, ? extends U> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given pattern, then match the start of any subsequent text according to the
	 * receiving parser.
	 * <p>
	 * Upon success, the text matched by the prepended pattern will be consumed,
	 * along with the result of applying the receiving parser, allowing the text
	 * to inform mutation of the parse result's state.
	 * 
	 * @param pattern
	 *          A pattern matching the text which should immediately precede the
	 *          text matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the prepended text, and transforming them into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         prepended text pattern
	 */
	default Parser<T> prepend(String pattern, BiConsumer<T, String> incorporate) {
		return prependTransform(pattern, (t, s) -> {
			incorporate.accept(t, s);
			return t;
		});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then match the start of any subsequent text according to
	 * the given parser.
	 * <p>
	 * Upon success, the result of applying the appended parser will be
	 * transformed, along with the result of applying the receiving parser, into a
	 * new result according to the given function.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param <V>
	 *          The type of the new parse result
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	<U, V> Parser<V> appendTransform(Parser<U> parser, BiFunction<T, U, ? extends V> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then match the start of any subsequent text according to
	 * the given parser.
	 * <p>
	 * Upon success, the result of applying the appended parser will be consumed,
	 * along with the result of applying the receiving parser, allowing the text
	 * to inform mutation of the parse result's state.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	default <U> Parser<T> append(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return appendTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then attempt to match the start of any subsequent text
	 * according to the given parser. If the second step - of matching the
	 * remaining text with the given parser - fails, then the result of applying
	 * the receiving parser alone will be returned.
	 * <p>
	 * If application of the appended parser also succeeds, the result of applying
	 * the appended parser will be transformed, along with the result of applying
	 * the receiving parser, into a new result according to the given function.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	<U> Parser<T> tryAppendTransform(Parser<U> parser, BiFunction<T, U, ? extends T> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * receiving parser, then attempt to match the start of any subsequent text
	 * according to the given parser. If the second step - of matching the
	 * remaining text with the given parser - fails, then the result of applying
	 * the receiving parser alone will be returned.
	 * <p>
	 * If application of the appended parser also succeeds, the result of applying
	 * the appended parser will be incorporated with the result of applying the
	 * receiving parser, and the result of the receiving parser will then be
	 * returned.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	default <U> Parser<T> tryAppend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return tryAppendTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given parser, then match the start of any subsequent text according to the
	 * receiving parser.
	 * <p>
	 * Upon success, the result of applying the prepended parser will be
	 * transformed, along with the result of applying the receiving parser, into a
	 * new result according to the given function.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param <V>
	 *          The type of the new parse result
	 * @param parser
	 *          A parser matching the text immediately preceding the text matched
	 *          by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	<U, V> Parser<V> prependTransform(Parser<U> parser, BiFunction<T, U, ? extends V> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given parser, then match the start of any subsequent text according to the
	 * receiving parser.
	 * <p>
	 * Upon success, the result of applying the prepended parser will be consumed,
	 * along with the result of applying the receiving parser, allowing the text
	 * to inform mutation of the parse result's state.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	default <U> Parser<T> prepend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return prependTransform(parser, (BiFunction<T, U, T>) (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given parser, then attempt to match the start of any subsequent text
	 * according to the receiving parser. If the first step - of matching the
	 * remaining text with the given parser - fails, then the result of applying
	 * the receiving parser alone will be returned.
	 * <p>
	 * If application of the prepended parser also succeeds, the result of
	 * applying the prepended parser will be transformed, along with the result of
	 * applying the receiving parser, into a new result according to the given
	 * function.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	<U> Parser<T> tryPrependTransform(Parser<U> parser, BiFunction<T, U, ? extends T> incorporate);

	/**
	 * Derive a new {@link Parser} instance from the receiving instance.
	 * <p>
	 * The new parser will first match the start of the text according to the
	 * given parser, then attempt to match the start of any subsequent text
	 * according to the receiving parser. If the first step - of matching the
	 * remaining text with the given parser - fails, then the result of applying
	 * the receiving parser alone will be returned.
	 * <p>
	 * If application of the prepended parser also succeeds, the result of
	 * applying the prepended parser will be incorporated with the result of
	 * applying the receiving parser, and the result of the receiving parser will
	 * then be returned.
	 * 
	 * @param <U>
	 *          The type of the parse result of the appended parser
	 * @param parser
	 *          A parser matching the text immediately following from the text
	 *          matched by this parser
	 * @param incorporate
	 *          A function taking the result of parsing the receiving parser and
	 *          the result of parsing the appended parser, and transforming them
	 *          into a new parse result
	 * @return A new {@link Parser} instance which also matches against the
	 *         appended parser
	 */
	default <U> Parser<T> tryPrepend(Parser<U> parser, BiConsumer<T, U> incorporate) {
		return tryPrependTransform(parser, (t, u) -> {
			incorporate.accept(t, u);
			return t;
		});
	}

	/*
	 * TODO greedy append: evaluate appended value first, not needing to match
	 * from the beginning of the unmatched region.
	 * 
	 * lazy prepend: evaluate original value first, not needing to match from the
	 * beginning of the unmatched region.
	 */

	/**
	 * Parse the given literal according to this parser.
	 * 
	 * @param literal
	 *          the string literal to parse
	 * @return the object result of parsing
	 */
	T parse(String literal);

	/**
	 * Parse the substring at the given parse state according to this parser.
	 * 
	 * @param currentState
	 *          the current parse state over a string literal
	 * @return the object result of parsing
	 */
	ParseResult<T> parseSubstring(ParseState currentState);
}
