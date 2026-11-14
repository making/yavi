/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.Optional;
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
sealed class DescribedValueValidator<T, X extends @Nullable Object>
		implements ValueValidator<T, X>permits DescribedNullAcceptingValueValidator {

	private final ValueValidator<T, X> delegate;

	private final Supplier<ValueDescription> description;

	private final Function<InputReader<Object>, ValueValidator<Object, X>> untyped;

	private final @Nullable Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed;

	private final @Nullable Function<String, ? extends ValueValidator<T, X>> named;

	/**
	 * Returns the function that goes back from the value to the input, if the delegate
	 * tells one; {@code null} if this validator does not know its reverse.
	 */
	private final @Nullable Supplier<Optional<Function<? super X, ? extends @Nullable T>>> reverse;

	/**
	 * @param preprocessed returns this validator with the given transform applied to its
	 * input, by a way that keeps the input type; {@code null} if there is none
	 * @param named returns this validator with the given name for the validated value;
	 * {@code null} if the violations are located under the name
	 */
	DescribedValueValidator(ValueValidator<T, X> delegate, Supplier<ValueDescription> description,
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			@Nullable Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed,
			@Nullable Function<String, ? extends ValueValidator<T, X>> named) {
		this(delegate, description, untyped, preprocessed, named, null);
	}

	DescribedValueValidator(ValueValidator<T, X> delegate, Supplier<ValueDescription> description,
			Function<InputReader<Object>, ValueValidator<Object, X>> untyped,
			@Nullable Function<Function<? super T, ? extends @Nullable T>, ValueValidator<T, X>> preprocessed,
			@Nullable Function<String, ? extends ValueValidator<T, X>> named,
			@Nullable Supplier<Optional<Function<? super X, ? extends @Nullable T>>> reverse) {
		this.reverse = reverse;
		this.delegate = delegate;
		this.description = description;
		this.untyped = untyped;
		this.preprocessed = preprocessed;
		this.named = named;
	}

	@Override
	public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
		return this.delegate.validate(t, locale, constraintContext);
	}

	@Override
	public ValueDescription description() {
		return this.description.get();
	}

	/**
	 * Returns this validator with the given way to go back from its value to its input.
	 * @param reverse returns the function that goes back, if there is one
	 */
	DescribedValueValidator<T, X> reversing(Supplier<Optional<Function<? super X, ? extends @Nullable T>>> reverse) {
		return new DescribedValueValidator<>(this.delegate, this.description, this.untyped, this.preprocessed,
				this.named, reverse);
	}

	@Override
	public Optional<ReversibleValueValidator<T, X>> reversible() {
		return this.reverse == null ? Optional.empty()
				: this.reverse.get().map(reverse -> new ReversedValueValidator<>(this, reverse));
	}

	@Override
	public ValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return this.preprocessed != null ? this.preprocessed.apply(transform)
				: ValueValidator.super.preprocess(transform);
	}

	@Override
	public ValueValidator<T, X> named(String name) {
		return this.named != null ? this.named.apply(name) : ValueValidator.super.named(name);
	}

	// the node type is erased: the function is written for any reader
	@Override
	@SuppressWarnings("unchecked")
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		return (ValueValidator<N, X>) (ValueValidator<?, X>) this.untyped
			.apply((InputReader<Object>) (InputReader<?>) reader);
	}

}
