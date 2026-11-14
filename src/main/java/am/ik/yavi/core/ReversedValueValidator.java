/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/**
 * A {@link ReversibleValueValidator} that validates like another validator and goes back
 * with a function. What is derived from it without changing the value keeps the function.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
final class ReversedValueValidator<T, X extends @Nullable Object> implements ReversibleValueValidator<T, X> {

	private final ValueValidator<T, X> validator;

	private final Function<? super X, ? extends @Nullable T> reverse;

	ReversedValueValidator(ValueValidator<T, X> validator, Function<? super X, ? extends @Nullable T> reverse) {
		this.validator = validator;
		this.reverse = reverse;
	}

	@Override
	public Validated<X> validate(@Nullable T t, Locale locale, ConstraintContext constraintContext) {
		return this.validator.validate(t, locale, constraintContext);
	}

	@Override
	public @Nullable T reverse(X value) {
		return this.reverse.apply(value);
	}

	@Override
	public ValueDescription description() {
		return this.validator.description();
	}

	@Override
	public ReversibleValueValidator<T, X> describedAs(ValueDescription description) {
		return new ReversedValueValidator<>(this.validator.describedAs(description), this.reverse);
	}

	@Override
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		return this.validator.readingWith(reader);
	}

	@Override
	public ReversibleValueValidator<T, X> preprocess(Function<? super T, ? extends @Nullable T> transform) {
		return new ReversedValueValidator<>(this.validator.preprocess(transform), this.reverse);
	}

	@Override
	public ReversibleValueValidator<T, X> named(String name) {
		return new ReversedValueValidator<>(this.validator.named(name), this.reverse);
	}

	@Override
	public ReversibleValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return new ReversedValueValidator<>(this.validator.constraintOnTarget(predicate, name, violationMessage),
				this.reverse);
	}

}
