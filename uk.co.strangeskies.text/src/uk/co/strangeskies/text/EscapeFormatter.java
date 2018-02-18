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
package uk.co.strangeskies.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Allow the escape of special characters in a character sequence by prefixture
 * with a given special escape character.
 *
 * @author Elias N Vasylenko
 */
public class EscapeFormatter {
	@SuppressWarnings("serial")
	private final static EscapeFormatter JAVA = new EscapeFormatter('\\', "\"\'", new HashMap<String, String>() {
		{
			put("t", "\t");
			put("b", "\b");
			put("n", "\n");
			put("r", "\r");
			put("f", "\f");
		}
	});

	/**
	 * @return A string escaper for java string literals.
	 */
	public static EscapeFormatter java() {
		return JAVA;
	}

	@SuppressWarnings("serial")
	private final static EscapeFormatter XML = new EscapeFormatter(new HashMap<String, String>() {
		{
			put("&gt;", ">");
			put("&lt;", "<");
			put("&quot;", "\"");
			put("&amp;", "&");
			put("&apos;", "'");
		}
	});

	/**
	 * @return A string escaper for java string literals.
	 */
	public static EscapeFormatter xml() {
		return XML;
	}

	/*
	 * 
	 */
	private final SortedMap<String, String> escapeToSequence;
	private final SortedMap<String, String> sequenceToEscape;

	/**
	 * Create a new escaper with the given escape character and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 */
	public EscapeFormatter(char escapeCharacer, String escapingCharacters) {
		this(escapeCharacer, escapingCharacters, new HashMap<>());
	}

	/**
	 * Create a new escaper with the given escape character and the escapable
	 * characters described by the given string.
	 * 
	 * @param escapeCharacer
	 *          The character with which to escape the given escaping characters
	 * @param escapingCharacters
	 *          The characters which should be escaped with the given escape
	 *          character
	 * @param escapeTransforms
	 *          Strings which should be escaped with the given escape character,
	 *          mapped to the strings they represent.
	 */
	public EscapeFormatter(char escapeCharacer, String escapingCharacters, Map<String, String> escapeTransforms) {
		this(composeEscapingTransforms(escapeCharacer, escapingCharacters, escapeTransforms));
	}

	private static SortedMap<String, String> composeEscapingTransforms(char escapeCharacer, String escapingCharacters,
			Map<String, String> escapeTransforms) {
		String escapeCharacterString = ((Character) escapeCharacer).toString();

		SortedMap<String, String> escapedCharacters = new TreeMap<>();

		for (Map.Entry<String, String> escapeTransform : escapeTransforms.entrySet())
			escapedCharacters.put(escapeCharacterString + escapeTransform.getKey(), escapeTransform.getValue());

		for (Character escapingCharacter : escapingCharacters.toCharArray())
			escapedCharacters.put(escapeCharacterString + escapingCharacter, escapingCharacter.toString());

		escapedCharacters.put(escapeCharacterString + escapeCharacterString, escapeCharacterString);

		return escapedCharacters;
	}

	/**
	 * Create a new escaper with the given escape character and the given
	 * escapable characters.
	 * 
	 * @param escapeTransforms
	 *          Strings which should be escaped with the given escape character,
	 *          mapped to the strings they represent.
	 */
	public EscapeFormatter(Map<String, String> escapeTransforms) {
		this(new TreeMap<>(escapeTransforms));
	}

	/**
	 * Create a new escaper with the given escape character and the given
	 * escapable characters.
	 * 
	 * @param escapeTransforms
	 *          Strings which should be escaped with the given escape character,
	 *          mapped to the strings they represent.
	 */
	private EscapeFormatter(SortedMap<String, String> escapeTransforms) {
		SortedMap<String, String> inverseTransforms = new TreeMap<>();
		for (Map.Entry<String, String> escapingCharacter : escapeTransforms.entrySet())
			inverseTransforms.put(escapingCharacter.getValue(), escapingCharacter.getKey());

		this.escapeToSequence = Collections.unmodifiableSortedMap(escapeTransforms);
		this.sequenceToEscape = Collections.unmodifiableSortedMap(inverseTransforms);
	}

	/**
	 * @return A mapping of escape characters to the character sequences they
	 *         escape.
	 */
	public SortedMap<String, String> getEscapingCharacters() {
		return escapeToSequence;
	}

	/**
	 * @return A mapping of character sequences to their escaped characters.
	 */
	public SortedMap<String, String> getEscapedCharacters() {
		return sequenceToEscape;
	}

	/**
	 * @param string
	 *          The string we wish to escape
	 * @return The escaped form of the given string.
	 */
	public String escape(String string) {
		return mapString(string, sequenceToEscape);
	}

	/**
	 * @param string
	 *          The string we wish to escape
	 * @return The escaped form of the given string.
	 */
	public String unescape(String string) {
		return mapString(string, escapeToSequence);
	}

	private String mapString(String string, SortedMap<String, String> mapping) {
		StringBuilder builder = new StringBuilder();

		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			/*
			 * For each character in the string
			 */
			char character = chars[i];

			StringBuilder sequenceSoFar = new StringBuilder().append(character);
			String escapedString = mapping.get(sequenceSoFar.toString());
			if (escapedString == null) {
				/*
				 * No escapable sequence found for a single character, so look further
				 */
				String afterSequenceSoFar = ((Character) (char) (character + 1)).toString();
				SortedMap<String, String> matches = mapping.subMap(sequenceSoFar.toString(), afterSequenceSoFar);
				int j = i + 1;
				while (!matches.isEmpty() && j < chars.length) {
					char nextLetter = chars[j++];

					afterSequenceSoFar = new StringBuilder(sequenceSoFar).append((char) (nextLetter + 1)).toString();
					sequenceSoFar.append(nextLetter);

					escapedString = mapping.get(sequenceSoFar.toString());
					if (escapedString != null) {
						i = j - 1;
						break;
					}

					matches = matches.subMap(sequenceSoFar.toString(), afterSequenceSoFar);
				}
			}

			if (escapedString != null)
				builder.append(escapedString);
			else
				builder.append(chars[i]);
		}

		return builder.toString();
	}
}
