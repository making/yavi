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
 * {@link #strict()} is the rules of {@link CoercibleInputReader#coercingStrings()}. The
 * methods of an instance return a copy that takes more strings:
 *
 * <pre>{@code
 * InputReader<Object> cells = InputReader.ofJdkTypes()
 *     .coercingStrings(StringCoercion.strict().trimming().booleanWords());
 * }</pre>
 *
 * An instance is immutable.
 *
 * @since 0.18.0
 */
public final class StringCoercion {

	private static final StringCoercion STRICT = new StringCoercion(false, Map.of("true", true, "false", false), false);

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
	 * @return the strict rules
	 */
	public static StringCoercion strict() {
		return STRICT;
	}

	/**
	 * Returns rules that do not take the white space around a string as a part of the
	 * text, as a cell of a CSV file or a field of a form is usually meant:
	 * <ul>
	 * <li>A blank string is a null node whatever it is checked as, like an empty
	 * string.</li>
	 * <li>A string checked as a type other than a string (a number, a {@code Boolean}, an
	 * enum constant, a temporal type) is read without its leading and trailing white
	 * space, so {@code " 42 "} is an {@code Integer}.</li>
	 * </ul>
	 * A string checked as a {@code String} is left as it is, because a validator trims it
	 * with {@code c.trim()} where its white space has no meaning. White space is what
	 * {@link Character#isWhitespace(int)} tells, and the violated value is the string as
	 * it was given.
	 * @return rules that trim
	 */
	public StringCoercion trimming() {
		return new StringCoercion(true, this.booleans, this.ignoringBooleanCase);
	}

	/**
	 * Returns rules that take {@code "true"}, {@code "1"}, {@code "yes"} and {@code "on"}
	 * as {@code true}, and {@code "false"}, {@code "0"}, {@code "no"} and {@code "off"}
	 * as {@code false}, whatever their case is. A checked checkbox of an HTML form sends
	 * {@code on} unless it is given a value, and an unchecked one sends nothing, so its
	 * argument is given {@code defaultValue(false)}.
	 * @return rules that take the usual words for a {@code Boolean}
	 * @see #booleanWords(Collection, Collection)
	 */
	public StringCoercion booleanWords() {
		return this.booleanWords(List.of("true", "1", "yes", "on"), List.of("false", "0", "no", "off"));
	}

	/**
	 * Returns rules that take the given words, and no other, as a {@code Boolean},
	 * whatever their case is.
	 * @param trueWords the words that stand for {@code true}
	 * @param falseWords the words that stand for {@code false}
	 * @return rules that take the given words for a {@code Boolean}
	 * @throws IllegalArgumentException if a word stands for both values
	 */
	public StringCoercion booleanWords(Collection<String> trueWords, Collection<String> falseWords) {
		final Map<String, Boolean> words = new HashMap<>();
		trueWords.forEach(word -> words.put(word.toLowerCase(Locale.ROOT), Boolean.TRUE));
		falseWords.forEach(word -> {
			if (words.put(word.toLowerCase(Locale.ROOT), Boolean.FALSE) == Boolean.TRUE) {
				throw new IllegalArgumentException("'" + word + "' stands for both true and false.");
			}
		});
		return new StringCoercion(this.trimming, Map.copyOf(words), true);
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
