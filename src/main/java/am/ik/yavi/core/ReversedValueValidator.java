/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import am.ik.yavi.message.MessageFormatter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A validator with the function that goes back from its value to its input. The
 * validators derived from it are derived from the wrapped validator, so that what the
 * wrapped validator knows, such as how it is renamed, is kept.
 *
 * @param <T> the type of the object to be validated
 * @param <X> the type of the validated and transformed result
 */
sealed class ReversedValueValidator<T, X extends @Nullable Object>
		implements ReversibleValueValidator<T, X>permits ReversedNullAcceptingValueValidator {

	private final ValueValidator<T, X> validator;

	private final Function<? super X, ? extends @Nullable T> reverse;

	ReversedValueValidator(ValueValidator<T, X> validator, Function<? super X, ? extends @Nullable T> reverse) {
		this.validator = validator;
		this.reverse = reverse;
	}

	static <T, X extends @Nullable Object> Function<? super Optional<@NonNull X>, ? extends @Nullable T> optional(
			Function<? super X, ? extends @Nullable T> reverse) {
		final Function<? super Optional<@NonNull X>, ? extends @Nullable T> optional = value -> value.isPresent()
				? reverse.apply(value.get()) : null;
		return optional;
	}

	// a value that stands for null is null or a default value, which goes back as it is
	@SuppressWarnings({ "unchecked", "NullAway" })
	static <T, X extends @Nullable Object, Y extends @Nullable Object> Function<? super Y, ? extends @Nullable T> nullSafe(
			Function<? super X, ? extends @Nullable T> reverse) {
		final Function<? super Y, ? extends @Nullable T> nullSafe = value -> value == null ? null
				: reverse.apply((X) value);
		return nullSafe;
	}

	// the inputs may hold null, although the type of the input of a lifted validator does
	// not say so
	@SuppressWarnings({ "unchecked", "NullAway" })
	static <T> List<T> nonNullElements(List<@Nullable T> inputs) {
		return (List<T>) (List<?>) inputs;
	}

	@SuppressWarnings({ "unchecked", "NullAway" })
	static <T> Map<String, T> nonNullValues(Map<String, @Nullable T> inputs) {
		return (Map<String, T>) (Map<String, ?>) inputs;
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
	public <N> ValueValidator<N, X> readingWith(InputReader<N> reader) {
		return this.validator.readingWith(reader);
	}

	@Override
	public ReversibleValueValidator<T, X> describedAs(ValueDescription description) {
		return new ReversedValueValidator<>(this.validator.describedAs(description), this.reverse);
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
	public ReversibleValueValidator<T, X> prefixed(String name) {
		return new ReversedValueValidator<>(this.validator.prefixed(name), this.reverse);
	}

	@Override
	public ReversibleValueValidator<T, X> formattingWith(MessageFormatter messageFormatter) {
		return new ReversedValueValidator<>(this.validator.formattingWith(messageFormatter), this.reverse);
	}

	@Override
	public ReversibleValueValidator<T, X> constraintOnTarget(Predicate<? super X> predicate, String name,
			ViolationMessage violationMessage) {
		return new ReversedValueValidator<>(this.validator.constraintOnTarget(predicate, name, violationMessage),
				this.reverse);
	}

	@Override
	public NullAccepting<T, Optional<@NonNull X>> optional() {
		return new ReversedNullAcceptingValueValidator<>(this.validator.optional(), optional(this.reverse));
	}

	@Override
	public NullAccepting<T, @Nullable X> nullable() {
		return new ReversedNullAcceptingValueValidator<>(this.validator.nullable(), nullSafe(this.reverse));
	}

	@Override
	public NullAccepting<T, X> defaultValue(X defaultValue) {
		return new ReversedNullAcceptingValueValidator<>(this.validator.defaultValue(defaultValue),
				nullSafe(this.reverse));
	}

	@Override
	public NullAccepting<T, X> recover(X fallback) {
		return new ReversedNullAcceptingValueValidator<>(this.validator.recover(fallback), nullSafe(this.reverse));
	}

	@Override
	public NullAccepting<T, X> recoverWith(Function<? super ConstraintViolations, ? extends X> fallback) {
		return new ReversedNullAcceptingValueValidator<>(this.validator.recoverWith(fallback), nullSafe(this.reverse));
	}

}
