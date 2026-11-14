/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * A validator that accepts {@code null}, with the function that goes back from its value
 * to its input.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class ReversedNullAcceptingValueValidator<T, X extends @Nullable Object> extends ReversedValueValidator<T, X>
		implements ReversibleValueValidator.NullAccepting<T, X> {

	private final NullAcceptingValueValidator<T, X> validator;

	private final Function<? super X, ? extends @Nullable T> reverse;

	ReversedNullAcceptingValueValidator(NullAcceptingValueValidator<T, X> validator,
			Function<? super X, ? extends @Nullable T> reverse) {
		super(validator, reverse);
		this.validator = validator;
		this.reverse = reverse;
	}

	@Override
	public NullAccepting<T, X> named(String name) {
		return new ReversedNullAcceptingValueValidator<>(this.validator.named(name), this.reverse);
	}

}
