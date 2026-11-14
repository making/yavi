/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A {@link DescribedValueValidator} whose delegate accepts a {@code null} input.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class DescribedNullAcceptingValueValidator<T, X extends @Nullable Object> extends DescribedValueValidator<T, X>
		implements NullAcceptingValueValidator<T, X> {

	private final Function<String, NullAcceptingValueValidator<T, X>> named;

	DescribedNullAcceptingValueValidator(ValueValidator<T, X> delegate, Supplier<ValueDescription> description,
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed,
			Function<String, NullAcceptingValueValidator<T, X>> named,
			Supplier<Optional<Function<? super X, ? extends @Nullable T>>> reverse) {
		super(delegate, description, untyped, preprocessed, named, reverse);
		this.named = named;
	}

	@Override
	public NullAcceptingValueValidator<T, X> named(String name) {
		return this.named.apply(name);
	}

}
