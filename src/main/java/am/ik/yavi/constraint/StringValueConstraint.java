/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * The constraints of a {@code String} validated as a value, which can be transformed
 * before it is constrained.
 *
 * <p>
 * A validator of a value produces what it validated, so the value can be brought into
 * shape first: the transforms come before the constraints, they are applied in the order
 * they are written, and the constraints and the result of the validator see the
 * transformed value.
 *
 * <pre>{@code
 * Yavi._string("email", c -> c.trim().toLowerCase().notBlank().email())
 * }</pre>
 *
 * <p>
 * A transform returns this type and a constraint returns {@link CharSequenceConstraint},
 * so a transform cannot be written after a constraint. {@code null} is not transformed.
 * The constraints of a property of an object ({@code ValidatorBuilder#constraint}) have
 * no transforms, because that validator does not produce a value.
 *
 * @param <T> the type of the target object
 * @since 0.18.0
 */
public class StringValueConstraint<T> extends CharSequenceConstraint<T, String> {

	private Function<String, @Nullable String> transformation = s -> s;

	/**
	 * Removes the leading and trailing white space, as {@link String#strip()} does. A
	 * character is white space according to {@link Character#isWhitespace(int)}, which
	 * includes the ideographic space, as for {@link #notBlank()}.
	 * @return this constraint
	 */
	public StringValueConstraint<T> trim() {
		return this.transform(String::strip);
	}

	/**
	 * Converts the value to lower case with the rules of {@link Locale#ROOT}.
	 * @return this constraint
	 */
	public StringValueConstraint<T> toLowerCase() {
		return this.transform(s -> s.toLowerCase(Locale.ROOT));
	}

	/**
	 * Converts the value to upper case with the rules of {@link Locale#ROOT}.
	 * @return this constraint
	 */
	public StringValueConstraint<T> toUpperCase() {
		return this.transform(s -> s.toUpperCase(Locale.ROOT));
	}

	/**
	 * Normalizes the value to the given Unicode normalization form.
	 *
	 * <p>
	 * This changes the value. {@link #normalizer(Normalizer.Form)} only selects the form
	 * in which the size is measured and leaves the value as it is.
	 * @param form the normalization form
	 * @return this constraint
	 */
	public StringValueConstraint<T> toNormalForm(Normalizer.Form form) {
		return this.transform(s -> Normalizer.normalize(s, form));
	}

	/**
	 * Transforms the value with the given function. The function is not called for
	 * {@code null}; it may return {@code null}, which the constraints then see.
	 * @param transform the function applied to the value
	 * @return this constraint
	 */
	public StringValueConstraint<T> transform(Function<? super String, ? extends @Nullable String> transform) {
		final Function<String, @Nullable String> current = this.transformation;
		this.transformation = s -> {
			final String transformed = current.apply(s);
			return transformed == null ? null : transform.apply(transformed);
		};
		return this;
	}

	/**
	 * Returns the transforms added so far as one function of a value other than
	 * {@code null}.
	 * @return the function that transforms the value before it is constrained
	 */
	public Function<String, @Nullable String> transformation() {
		return this.transformation;
	}

}
