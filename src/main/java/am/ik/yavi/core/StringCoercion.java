/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The rules by which a reader takes a string as text that stands for a value of another
 * type, given to {@link CoercibleInputReader#coercingStrings(StringCoercion)}.
 *
 * <p>
 * {@link #strict()} is the rules of {@link CoercibleInputReader#coercingStrings()}. Its
 * methods return rules that take more strings:
 *
 * <pre>{@code
 * InputReader<Object> cells = InputReader.ofJdkTypes()
 *     .coercingStrings(StringCoercion.strict().trimming().booleanWords());
 * }</pre>
 *
 * Each rule is given once, in any order: the returned rules offer only what is not given
 * yet, so {@code booleanWords(...).booleanWords()}, in which the second words would
 * replace the first ones, does not compile. An instance is immutable.
 *
 * @since 0.18.0
 */
public sealed class StringCoercion {

	private static final Map<String, Boolean> STRICT_WORDS = Map.of("true", true, "false", false);

	private static final Strict STRICT = new Strict();

	private static final Map<String, Boolean> USUAL_WORDS = words(List.of("true", "1", "yes", "on"),
			List.of("false", "0", "no", "off"));

	private final boolean trimming;

	/** The booleans by their word, which is in lower case if the case is ignored. */
	private final Map<String, Boolean> booleans;

	private final boolean ignoringBooleanCase;

	private StringCoercion(boolean trimming, Map<String, Boolean> booleans, boolean ignoringBooleanCase) {
		this.trimming = trimming;
		this.booleans = booleans;
		this.ignoringBooleanCase = ignoringBooleanCase;
	}

	/**
	 * Returns the rules of {@link CoercibleInputReader#coercingStrings()}: a string is
	 * not trimmed, and a {@code Boolean} is {@code "true"} or {@code "false"}.
	 * @return the strict rules, to which the other rules are added
	 */
	public static Strict strict() {
		return STRICT;
	}

	/**
	 * Returns the booleans by the given words in lower case.
	 */
	private static Map<String, Boolean> words(Collection<String> trueWords, Collection<String> falseWords) {
		final Map<String, Boolean> words = new HashMap<>();
		trueWords.forEach(word -> words.put(word.toLowerCase(Locale.ROOT), Boolean.TRUE));
		falseWords.forEach(word -> {
			if (words.put(word.toLowerCase(Locale.ROOT), Boolean.FALSE) == Boolean.TRUE) {
				throw new IllegalArgumentException("'" + word + "' stands for both true and false.");
			}
		});
		return Map.copyOf(words);
	}

	/**
	 * The strict rules, to which trimming and the words of a {@code Boolean} can be
	 * added.
	 *
	 * @since 0.18.0
	 */
	public static final class Strict extends StringCoercion {

		private Strict() {
			super(false, STRICT_WORDS, false);
		}

		/**
		 * Returns rules that do not take the white space around a string as a part of the
		 * text, as a cell of a CSV file or a field of a form is usually meant:
		 * <ul>
		 * <li>A blank string is a null node whatever it is checked as, like an empty
		 * string.</li>
		 * <li>A string checked as a type other than a string (a number, a
		 * {@code Boolean}, an enum constant, a temporal type, a {@code UUID}) is read
		 * without its leading and trailing white space, so {@code " 42 "} is an
		 * {@code Integer}.</li>
		 * </ul>
		 * A string checked as a {@code String} is left as it is, because a validator
		 * trims it with {@code c.trim()} where its white space has no meaning. White
		 * space is what {@link Character#isWhitespace(int)} tells, and the violated value
		 * is the string as it was given.
		 * @return rules that trim
		 */
		public Trimming trimming() {
			return new Trimming();
		}

		/**
		 * Returns rules that take {@code "true"}, {@code "1"}, {@code "yes"} and
		 * {@code "on"} as {@code true}, and {@code "false"}, {@code "0"}, {@code "no"}
		 * and {@code "off"} as {@code false}, whatever their case is. A checked checkbox
		 * of an HTML form sends {@code on} unless it is given a value, and an unchecked
		 * one sends nothing, so its argument is given {@code defaultValue(false)}.
		 * @return rules that take the usual words for a {@code Boolean}
		 * @see #booleanWords(Collection, Collection)
		 */
		public BooleanWords booleanWords() {
			return new BooleanWords(USUAL_WORDS);
		}

		/**
		 * Returns rules that take the given words, and no other, as a {@code Boolean},
		 * whatever their case is.
		 * @param trueWords the words that stand for {@code true}
		 * @param falseWords the words that stand for {@code false}
		 * @return rules that take the given words for a {@code Boolean}
		 * @throws IllegalArgumentException if a word stands for both values
		 */
		public BooleanWords booleanWords(Collection<String> trueWords, Collection<String> falseWords) {
			return new BooleanWords(words(trueWords, falseWords));
		}

	}

	/**
	 * The rules that trim, to which the words of a {@code Boolean} can be added.
	 *
	 * @since 0.18.0
	 */
	public static final class Trimming extends StringCoercion {

		private Trimming() {
			super(true, STRICT_WORDS, false);
		}

		/**
		 * Returns rules that also take the usual words as a {@code Boolean}, as
		 * {@link Strict#booleanWords()} states.
		 * @return rules that trim and take the usual words for a {@code Boolean}
		 */
		public StringCoercion booleanWords() {
			return new StringCoercion(true, USUAL_WORDS, true);
		}

		/**
		 * Returns rules that also take the given words, and no other, as a
		 * {@code Boolean}, as {@link Strict#booleanWords(Collection, Collection)} states.
		 * @param trueWords the words that stand for {@code true}
		 * @param falseWords the words that stand for {@code false}
		 * @return rules that trim and take the given words for a {@code Boolean}
		 * @throws IllegalArgumentException if a word stands for both values
		 */
		public StringCoercion booleanWords(Collection<String> trueWords, Collection<String> falseWords) {
			return new StringCoercion(true, words(trueWords, falseWords), true);
		}

	}

	/**
	 * The rules that take words as a {@code Boolean}, to which trimming can be added.
	 *
	 * @since 0.18.0
	 */
	public static final class BooleanWords extends StringCoercion {

		private BooleanWords(Map<String, Boolean> booleans) {
			super(false, booleans, true);
		}

		/**
		 * Returns rules that also trim, as {@link Strict#trimming()} states.
		 * @return rules that take the words for a {@code Boolean} and trim
		 */
		public StringCoercion trimming() {
			return new StringCoercion(true, super.booleans, true);
		}

	}

	/**
	 * Tells whether the string is a null node.
	 */
	boolean isNull(String string) {
		return this.trimming ? string.isBlank() : string.isEmpty();
	}

	/**
	 * Returns the text of a string that is checked as another type than a string.
	 */
	String text(String string) {
		return this.trimming ? string.strip() : string;
	}

	/**
	 * Returns the boolean the text stands for, or {@code null} if it stands for none.
	 */
	@Nullable Boolean toBoolean(String text) {
		return this.booleans.get(this.ignoringBooleanCase ? text.toLowerCase(Locale.ROOT) : text);
	}

}
