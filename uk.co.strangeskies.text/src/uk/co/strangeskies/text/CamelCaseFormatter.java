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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * A tool for formatting and unformatting series' of words to and from their
 * camel case representation.
 * <p>
 * For the purposes of application of camel case rules, any code point which has
 * no case is considered to be the same case as the preceding letter.
 * 
 * @author Elias N Vasylenko
 */
public class CamelCaseFormatter {
	/**
	 * Treatment of case when unformatting from camel case to separate words.
	 * 
	 * @author Elias N Vasylenko
	 */
	public enum UnformattingCase {
		/**
		 * Each word will be transformed to its upper case form upon unformatting
		 * from camel case.
		 */
		UPPER,

		/**
		 * Each word will be transformed to its lower case form upon unformatting
		 * from camel case.
		 */
		LOWER,

		/**
		 * The original case of each character will be preserved upon unformatting
		 * from camel case.
		 */
		PRESERVED,

		/**
		 * Each word will be transformed to its upper or lower case form upon
		 * unformatting from camel case, according to the following rules:
		 * 
		 * <ul>
		 * <li>Words which contain no lower case characters will retain their upper
		 * case form.</li>
		 * 
		 * <li>Words which contain at least one lower case character will be
		 * transformed to their lower case form.</li>
		 * </ul>
		 * 
		 * <p>
		 * Bear in mind this may produce unexpected results for single character
		 * words.
		 */
		FLATTENED
	}

	private final String canonicalDelimiter;
	private final Pattern delimiterPattern;
	private final boolean leadingCapital;
	private final UnformattingCase unformattingCase;

	/**
	 * Construct a formatter which {@link #getDelimiterPattern() delimits} over
	 * whitespace, which does not format camel case with a
	 * {@link #isLeadingCapital() leading upper case character}, and which
	 * unformats camel case with case {@link UnformattingCase#PRESERVED
	 * preserved}.
	 */
	public CamelCaseFormatter() {
		this(" ", Pattern.compile("\\s"), false, UnformattingCase.PRESERVED);
	}

	/**
	 * Construct a formatter with the given settings. Specifying an exact
	 * delimiter in this was gives faster {@link #format(String) formatting} than
	 * a delimiter pattern.
	 * 
	 * @param delimiter
	 *          the {@link #getCanonicalDelimiter() delimiter string}
	 * @param leadingCapital
	 *          the {@link #isLeadingCapital() leading capital} setting
	 * @param unformattingCase
	 *          the {@link #getUnformattingCase() unformatting case} strategy
	 */
	public CamelCaseFormatter(String delimiter, boolean leadingCapital, UnformattingCase unformattingCase) {
		this.canonicalDelimiter = delimiter;
		this.delimiterPattern = null;
		this.leadingCapital = leadingCapital;
		this.unformattingCase = unformattingCase;
	}

	/**
	 * Construct a formatter with the given settings. The given canonical
	 * delimiter should match the given delimiter pattern
	 * 
	 * @param canonicalDelimiter
	 *          the canonical {@link #getCanonicalDelimiter() delimiter string}
	 * @param delimiterPattern
	 *          the {@link #getDelimiterPattern() delimiter pattern}
	 * @param leadingCapital
	 *          the {@link #isLeadingCapital() leading capital} setting
	 * @param unformattingCase
	 *          the {@link #getUnformattingCase() unformatting case} strategy
	 */
	public CamelCaseFormatter(String canonicalDelimiter, Pattern delimiterPattern, boolean leadingCapital,
			UnformattingCase unformattingCase) {
		this.canonicalDelimiter = canonicalDelimiter;
		this.delimiterPattern = delimiterPattern;

		if (!delimiterPattern.matcher(canonicalDelimiter).matches())
			throw new IllegalArgumentException("Canonical delimiter does not match given delimiter pattern");

		this.leadingCapital = leadingCapital;
		this.unformattingCase = unformattingCase;
	}

	/**
	 * @return the canonical delimiter string for {@link #unformat(String)
	 *         unformatting} camel case strings
	 */
	public String getCanonicalDelimiter() {
		return canonicalDelimiter;
	}

	/**
	 * @return the delimiter pattern for {@link #format(String) formatting}
	 *         strings into camel case
	 */
	public Pattern getDelimiterPattern() {
		return delimiterPattern;
	}

	/**
	 * @return true if camel case should lead with a capital letter, false
	 *         otherwise
	 */
	public boolean isLeadingCapital() {
		return leadingCapital;
	}

	/**
	 * @return the {@link UnformattingCase unformatting case} strategy for this
	 *         formatter
	 */
	public UnformattingCase getUnformattingCase() {
		return unformattingCase;
	}

	/**
	 * @param words
	 *          a sequence of words in a list
	 * @return the given sequence of words in camel case format, with a
	 *         {@link #isLeadingCapital() leading capital} if specified
	 */
	public String join(List<String> words) {
		StringBuilder builder = new StringBuilder();

		for (String word : words) {
			format(builder, word);
		}

		return builder.toString();
	}

	/**
	 * @param string
	 *          a string in camel case format
	 * @return the given camel case string split into words according to the
	 *         {@link #getUnformattingCase() unformatting case}
	 */
	public List<String> split(String string) {
		ArrayList<String> split = new ArrayList<>(string.length());

		unformat(string, w -> {
			split.add(w);
		});

		split.trimToSize();

		return split;
	}

	/**
	 * @param words
	 *          a sequence of words separated by the {@link #getDelimiterPattern()
	 *          delimiter pattern}
	 * @return the given sequence of words in camel case format, with a
	 *         {@link #isLeadingCapital() leading capital} if specified
	 */
	public String format(String words) {
		StringBuilder builder = new StringBuilder();

		if (delimiterPattern != null) {
			for (String word : delimiterPattern.split(words)) {
				format(builder, word);
			}
		} else {
			int indexFrom = 0;
			int indexTo = words.indexOf(canonicalDelimiter);
			while (indexTo >= 0) {
				format(builder, words.substring(indexFrom, indexTo));

				indexFrom = indexTo + canonicalDelimiter.length();
				indexTo = words.indexOf(canonicalDelimiter, indexFrom);
			}

			format(builder, words.substring(indexFrom));
		}

		return builder.toString();
	}

	/**
	 * @param string
	 *          a string in camel case format
	 * @return the given camel case string split into words according to the
	 *         {@link #getUnformattingCase() unformatting case}, then joined about
	 *         the {@link #getCanonicalDelimiter() delimiter string}
	 */
	public String unformat(String string) {
		StringBuilder builder = new StringBuilder();

		unformat(string, w -> {
			if (builder.length() > 0) {
				builder.append(canonicalDelimiter);
			}
			builder.append(w);
		});

		return builder.toString();
	}

	private void format(StringBuilder builder, String word) {
		if (word.codePoints().allMatch(Character::isUpperCase)) {
			builder.append(word);
		} else if (builder.length() == 0 && !leadingCapital) {
			builder.append(word.toLowerCase());
		} else {
			builder.append(word.substring(0, 1).toUpperCase());
			builder.append(word.substring(1).toLowerCase());
		}
	}

	private static enum CharacterType {
		UPPER_CASE_LETTER, LOWER_CASE_LETTER, DIGIT, PUNCTUATION;

		private static final Pattern PUNCTUATION_CHARACTER = Pattern.compile("\\p{Punct}");

		public static CharacterType forCodePoint(CharacterType previousCharacterType, int character) {
			if (Character.isLetter(character) && Character.isUpperCase(character)) {
				/*
				 * The character is an upper case letter
				 */
				return UPPER_CASE_LETTER;

			} else if (Character.isDigit(character)) {
				/*
				 * The character is a digit
				 */
				return DIGIT;

			} else if (PUNCTUATION_CHARACTER.matcher(new String(new int[] { character }, 0, 1)).matches()) {
				/*
				 * The character is punctuation
				 */
				return PUNCTUATION;

			} else if (Character.isLowerCase(character) || previousCharacterType == LOWER_CASE_LETTER) {
				/*
				 * The character is lower case, or if it is non-cased and the previous
				 * is lower case
				 */
				return LOWER_CASE_LETTER;

			} else {
				/*
				 * If a character is non-cased
				 */
				return UPPER_CASE_LETTER;
			}
		}

	}

	private void unformat(String string, Consumer<String> wordConsumer) {
		int previousIndex = 0;
		CharacterType previousCharacterType = null;

		int copyFromIndex = 0;
		boolean wordContainsLowerCase = false;

		for (int index = 0; index < string.length();) {
			int character = string.codePointAt(index);
			int copyToIndex = copyFromIndex;

			CharacterType characterType = CharacterType.forCodePoint(previousCharacterType, character);

			switch (characterType) {
			case PUNCTUATION:
				copyToIndex = index;

				break;
			case LOWER_CASE_LETTER:
				if (previousCharacterType == CharacterType.UPPER_CASE_LETTER) {
					index = previousIndex;
				} else {
					wordContainsLowerCase = true;
				}
				// fall through here
			case DIGIT:
			case UPPER_CASE_LETTER:
				if (previousCharacterType != characterType) {
					copyToIndex = index;
				}

				break;
			}

			if (copyToIndex > copyFromIndex) {
				String word = string.substring(copyFromIndex, copyToIndex);
				if (wordContainsLowerCase) {
					word = word.toLowerCase();
				}
				wordConsumer.accept(word);

				copyFromIndex = copyToIndex;
				wordContainsLowerCase = false;
			}

			if (characterType == CharacterType.PUNCTUATION) {
				copyFromIndex++;
			}

			previousCharacterType = characterType;
			previousIndex = index;
			index += Character.charCount(character);
		}

		String word = string.substring(copyFromIndex);
		if (wordContainsLowerCase) {
			word = word.toLowerCase();
		}
		wordConsumer.accept(word);
	}
}
