/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that delegates the validation and carries a description of its
 * input. The description is created when it is first requested.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class DescribedValueValidator<T, X> implements ValueValidator<T, X> {

	private final ValueValidator<T, X> delegate;

	private final Supplier<ValueDescription> description;

	DescribedValueValidator(ValueValidator<T, X> delegate, Supplier<ValueDescription> description) {
		this.delegate = delegate;
		this.description = description;
	}

	@Override
	public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
		return this.delegate.validate(t, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return this.description.get();
	}

}
