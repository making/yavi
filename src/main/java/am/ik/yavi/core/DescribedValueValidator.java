/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ValueValidator} that delegates the validation and carries a description of its
 * input and the way to read it from an untyped input. The description is created when it
 * is requested.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class DescribedValueValidator<T, X extends @Nullable Object> implements ValueValidator<T, X> {

	private final ValueValidator<T, X> delegate;

	private final Supplier<ValueDescription> description;

	private final Function<InputReader<Object>, ValueValidator<Object, X>> untyped;

	private final @Nullable Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed;

	/**
	 * @param preprocessed returns this validator with the given transform applied to its
	 * input, by a way that keeps the input type; {@code null} if there is none
	 */
	DescribedValueValidator(ValueValidator<T, X> delegate, Supplier<ValueDescription> description,
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			@Nullable Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed) {
		this.delegate = delegate;
		this.description = description;
		this.untyped = untyped;
		this.preprocessed = preprocessed;
	}

	@Override
	public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
		return this.delegate.validate(t, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return this.description.get();
	}

	@Override
	public ValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return this.preprocessed != null ? this.preprocessed.apply(transform)
				: ValueValidator.super.preprocess(transform);
	}

	// the node type is erased: the function is written for any reader
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		return (ValueValidator<N, X>) (ValueValidator<?, X>) this.untyped
			.apply((InputReader<Object>) (InputReader<?>) reader);
	}

}
