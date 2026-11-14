/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that also goes back from a value it produces to the input that
 * it reads that value from.
 *
 * <pre>{@code
 * ReversibleValueValidator<String, Email> emailValidator = Yavi._string("email", c -> c.notBlank().email())
 *     .map(Email::new, Email::value);
 * Email email = emailValidator.validated("taro@example.com");
 * emailValidator.reverse(email); // "taro@example.com"
 * }</pre>
 *
 * <p>
 * The reverse is defined by the round trip: for every value {@code x} that the validator
 * produces, {@code validate(reverse(x))} succeeds with a value equal to {@code x}. It is
 * not the inverse of the validation: the input that is returned is one that the value is
 * read from, not necessarily the one it was read from, so a text that the validator trims
 * is returned trimmed.
 *
 * <p>
 * A validator of this type knows its reverse by its type. Any validator tells whether it
 * knows one with {@link ValueValidator#reversible()}: the validators of single values
 * that do not map their value, and what {@link ValueValidator#named(String)},
 * {@link ValueValidator#optional()}, {@link ValueValidator#nullable()},
 * {@link ValueValidator#defaultValue(Object)}, {@link ValueValidator#recover(Object)},
 * {@link ValueValidator#constraintOnTarget} and the like make of a validator that knows
 * one. A validator that changes its value without saying how to go back, such as
 * {@link ValueValidator#map(Function)}, {@link ValueValidator#andThen(ValueValidator)} or
 * a lambda, does not.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 * @since 0.18.0
 * @see ValueValidator#map(Function, Function)
 */
public interface ReversibleValueValidator<T, X extends @Nullable Object> extends ValueValidator<T, X> {

	/**
	 * Returns the given validator as one that goes back from its value to its input with
	 * the given function. This states the reverse of a validator that does not know it,
	 * such as a lambda.
	 * @param validator the validator
	 * @param reverse the function that returns the input that the validator reads the
	 * given value from
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that validates like the given one and knows its reverse
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> of(ValueValidator<T, X> validator,
			Function<? super X, ? extends @Nullable T> reverse) {
		return new ReversedValueValidator<>(validator, reverse);
	}

	/**
	 * Returns the given validator as one that goes back from its value to its input as
	 * the other validator does. This is for a validator derived from a reversible one
	 * that produces the same values, such as one that reports its violations in another
	 * way.
	 * @param validator the validator
	 * @param reversible the validator that knows the reverse
	 * @param <T> the type of the object to be validated
	 * @param <X> the type of the validated and transformed result
	 * @return a validator that validates like the given one and knows its reverse
	 */
	static <T, X extends @Nullable Object> ReversibleValueValidator<T, X> of(ValueValidator<T, X> validator,
			ReversibleValueValidator<T, X> reversible) {
		final Function<? super X, ? extends @Nullable T> reverse = value -> reversible.reverse(value);
		return new ReversedValueValidator<>(validator, reverse);
	}

	/**
	 * Returns the input that this validator reads the given value from.
	 * @param value a value that this validator produces
	 * @return the input, which is {@code null} if the validator produces the value for
	 * {@code null}
	 */
	@Nullable T reverse(X value);

	/**
	 * Returns this validator.
	 * @return this validator
	 */
	@Override
	default Optional<ReversibleValueValidator<T, X>> reversible() {
		return Optional.of(this);
	}

	/**
	 * Returns a validator that reports the violations of the validated value under the
	 * given name, as {@link ValueValidator#named(String)} states, and that still knows
	 * its reverse.
	 * @param name the name under which the violations of the value are reported
	 * @return a validator of the value under the given name
	 */
	@Override
	default ReversibleValueValidator<T, X> named(String name) {
		return of(ValueValidator.super.named(name), this);
	}

	/**
	 * Returns a validator that additionally checks the produced value with the given
	 * predicate, as {@link ValueValidator#constraintOnTarget} states, and that still
	 * knows its reverse.
	 * @param predicate the rule that the produced value must satisfy
	 * @param name the name of the reported violation
	 * @param violationMessage the message key and the default message format of the
	 * reported violation
	 * @return a validator with the additional rule
	 */
	@Override
	default ReversibleValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return of(ValueValidator.super.constraintOnTarget(predicate, name, violationMessage), this);
	}

}
