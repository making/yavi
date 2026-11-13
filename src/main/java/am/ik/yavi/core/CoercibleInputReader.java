/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

/**
 * A reader of the nodes of a tree model whose strings can be taken as text that stands
 * for a value, with {@link #coercingStrings()}. This is the interface to implement for a
 * tree model: it adds no method to implement to {@link InputReader}.
 *
 * <p>
 * The reader that coerces strings is an {@link InputReader} only, so the rules are given
 * once: {@code reader.coercingStrings(rules).coercingStrings()}, which would either drop
 * the rules or do nothing, does not compile.
 *
 * @param <N> the type of the nodes
 * @since 0.18.0
 * @see InputReader#ofJdkTypes()
 */
public interface CoercibleInputReader<N> extends InputReader<N> {

	/**
	 * Returns a reader that reads the same tree as this reader and takes its strings as
	 * text that stands for a value of the checked type. It reads an input that has no
	 * other scalar than strings: request parameters, a submitted form, a row of a CSV
	 * file, environment variables or properties.
	 *
	 * <pre>{@code
	 * ValueValidator<Object, Integer> page = IntegerValidatorBuilder.of("page", c -> c.greaterThanOrEqual(1))
	 *     .build()
	 *     .readingWith(InputReader.ofJdkTypes().coercingStrings());
	 * Validated<Integer> validated = page.validate("2");
	 * }</pre>
	 *
	 * Request parameters, whose keys are paths and may have several values, are read with
	 * {@code readingParameters()} of a validator of maps built with {@code fromMap},
	 * which coerces strings in this way.
	 *
	 * The following is added to the rules of {@link ValueValidator#untyped(Class)}:
	 * <ul>
	 * <li>An empty string is a null node whatever it is checked as, because text has no
	 * other way to say that a value is not given. It is validated like {@code null}, so
	 * {@code optional()}, {@code nullable()} and {@code defaultValue} apply to it; a
	 * string property that must receive {@code ""} is given
	 * {@code defaultValue("")}.</li>
	 * <li>A string checked as a numeric type is read as a decimal number, in the grammar
	 * of {@link java.math.BigDecimal#BigDecimal(String)}: an optional sign, digits, an
	 * optional fraction and an optional exponent, without surrounding whitespace or
	 * grouping separators. The number is then checked like any other number, so
	 * {@code "30"} and {@code "30.0"} are an {@code Integer}, and {@code "30.5"} and
	 * {@code "NaN"} are not.</li>
	 * <li>A string checked as a {@code Boolean} is {@code "true"} or
	 * {@code "false"}.</li>
	 * </ul>
	 * Any other string is left as it is, so a string that does not stand for the type is
	 * reported with {@code object.type} like any other mismatch. A string is not trimmed,
	 * a value that is not a string is read as this reader reads it, and nothing is
	 * converted to a string: {@code 30} is not a {@code String}. Enum constants and
	 * temporal types are read from a string by every reader.
	 *
	 * <p>
	 * {@link #coercingStrings(StringCoercion)} takes more strings, such as {@code " 42 "}
	 * for a number or {@code "on"} for a {@code Boolean}.
	 * @return a reader that coerces strings
	 */
	default InputReader<N> coercingStrings() {
		return this.coercingStrings(StringCoercion.strict());
	}

	/**
	 * Returns a reader like {@link #coercingStrings()} that takes strings by the given
	 * rules, for an input whose text is written by a person or another loose source: the
	 * cells of a CSV file, the fields of a form.
	 *
	 * <pre>{@code
	 * InputReader<Object> cells = InputReader.ofJdkTypes()
	 *     .coercingStrings(StringCoercion.strict().trimming().booleanWords());
	 * Yavi._boolean().readingWith(cells).validate(" Yes "); // true
	 * }</pre>
	 *
	 * The returned reader does not coerce again: give all the rules at once.
	 * @param coercion the rules by which a string is taken as text
	 * @return a reader that coerces strings
	 * @see StringCoercion
	 */
	default InputReader<N> coercingStrings(StringCoercion coercion) {
		return new StringCoercingInputReader<>(this, coercion);
	}

}
