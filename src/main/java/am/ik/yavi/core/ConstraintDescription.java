/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Describes one constraint applied to a value.
 *
 * <p>
 * A constraint is identified by its message key, the same key that a
 * {@link ConstraintViolation} of the constraint reports (see
 * {@link ViolationMessage.Default} for the built-in keys). The arguments are the ones the
 * constraint was configured with, in the order the constraint method takes them: for
 * example {@code lessThanOrEqual(14)} on a string is described as
 * {@code container.lessThanOrEqual} with {@code [14]}, {@code pattern("[0-9]+")} as
 * {@code charSequence.pattern} with {@code ["[0-9]+"]} and {@code oneOf(values)} as
 * {@code object.oneOf} with {@code [values]}. Neither the name of the value nor a
 * violated value is part of the arguments, so a consumer can translate a constraint
 * without parsing a message.
 *
 * @param messageKey the message key of the constraint
 * @param args the arguments the constraint was configured with; unmodifiable
 * @param rejectsNull whether the constraint reports a violation for {@code null}
 * @since 0.18.0
 * @see ValueDescription
 */
public record ConstraintDescription(String messageKey, List<@Nullable Object> args, boolean rejectsNull) {

	/**
	 * Returns the description of the given constraint predicate.
	 *
	 * <p>
	 * Whether {@code null} is rejected is determined by applying the predicate to
	 * {@code null}, unless the predicate skips {@code null} (see {@link NullAs}).
	 * @param predicate the constraint predicate to describe
	 * @return the description of the constraint
	 */
	public static ConstraintDescription of(ConstraintPredicate<?> predicate) {
		final List<@Nullable Object> args = Collections.unmodifiableList(Arrays.asList(predicate.args().get().clone()));
		return new ConstraintDescription(predicate.messageKey(), args, rejectsNull(predicate));
	}

	/**
	 * Returns the description of the given constraint predicate of a value for which
	 * {@code null} is validated as the given value, such as the empty list of a list
	 * whose elements are validated one by one.
	 *
	 * <p>
	 * Whether {@code null} is rejected is determined by applying the predicate to the
	 * given value.
	 * @param predicate the constraint predicate to describe
	 * @param nullValue the value as which {@code null} is validated
	 * @param <V> the type of the constrained value
	 * @return the description of the constraint
	 */
	public static <V> ConstraintDescription of(ConstraintPredicate<V> predicate, V nullValue) {
		final ConstraintDescription description = of(predicate);
		boolean rejected;
		try {
			rejected = predicate.violatedValue(nullValue).isPresent();
		}
		catch (RuntimeException e) {
			rejected = false;
		}
		return new ConstraintDescription(description.messageKey(), description.args(), rejected);
	}

	@SuppressWarnings("NullAway") // a predicate that does not skip null handles null
	private static boolean rejectsNull(ConstraintPredicate<?> predicate) {
		if (predicate.nullValidity().skipNull()) {
			return false;
		}
		try {
			return predicate.violatedValue(null).isPresent();
		}
		catch (RuntimeException e) {
			return false;
		}
	}

}
