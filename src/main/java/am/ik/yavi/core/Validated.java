/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.fn.Function1;
import am.ik.yavi.fn.Validation;
import am.ik.yavi.fn.Validations;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A specialized {@link Validation} whose errors are {@link ConstraintViolation}s.
 *
 * <p>
 * A {@code Validated} is either a success that holds a value or a failure that holds
 * {@link ConstraintViolations}. It is returned by applicative validators
 * ({@link ApplicativeValidator}, {@link ValueValidator}) and the generated
 * {@code am.ik.yavi.arguments.ArgumentsNValidator} types. {@link #combine(Validation)}
 * accumulates the violations of all combined validations and produces a
 * {@code Validated}.
 *
 * <p>
 * Example: <pre>{@code
 * Validated<Car> validated = Car.of("Morris", "DD-AB-123", 2);
 * if (validated.isValid()) {
 *     Car car = validated.value();
 * }
 * else {
 *     ConstraintViolations violations = validated.errors();
 * }
 * }</pre>
 *
 * <p>
 * The interface is sealed: an instance is either a {@link Valid} or an {@link Invalid}
 * record, so both cases can be handled exhaustively with pattern matching: <pre>{@code
 * switch (Car.of("Morris", "DD-AB-123", 2)) {
 *     case Validated.Valid<Car>(var car) -> save(car);
 *     case Validated.Invalid<Car>(var violations) -> report(violations);
 * }
 * }</pre>
 *
 * @param <T> value type in the case of success
 * @since 0.6.0
 * @see Validation
 * @see ConstraintViolations
 */
public sealed interface Validated<T extends @Nullable Object> extends Validation<ConstraintViolation, T> {

	/**
	 * A successful validation result.
	 *
	 * @param value the validated value; {@code null} if the validation succeeded with
	 * {@code null}
	 * @param <T> value type
	 * @since 0.18.0
	 */
	record Valid<T extends @Nullable Object> (T value) implements Validated<T> {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ConstraintViolations errors() {
			throw new NoSuchElementException("errors of 'Valid' does not exist.");
		}

	}

	/**
	 * A failed validation result.
	 *
	 * @param errors the constraint violations, which are not empty
	 * @param <T> value type in the case of success
	 * @since 0.18.0
	 */
	record Invalid<T extends @Nullable Object> (ConstraintViolations errors) implements Validated<T> {

		/**
		 * Creates a failed validation result. The violations are wrapped as an
		 * unmodifiable view and are not copied.
		 * @throws IllegalArgumentException if {@code errors} is empty
		 */
		public Invalid {
			if (errors.isEmpty()) {
				throw new IllegalArgumentException("'errors' must not be empty.");
			}
			errors = new ConstraintViolations(Collections.unmodifiableList(errors));
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public T value() {
			throw new NoSuchElementException("value of 'Invalid' does not exist. Errors=" + this.errors);
		}

	}

	/**
	 * Converts the given validation result to a {@code Validated}. If the argument
	 * already is a {@code Validated}, it is returned as is.
	 * @param delegate the validation result to convert
	 * @param <T> value type in the case of success
	 * @return the validation result as a {@code Validated}
	 */
	@SuppressWarnings("unchecked")
	static <T extends @Nullable Object> Validated<T> of(Validation<ConstraintViolation, ? extends T> delegate) {
		if (delegate instanceof Validated) {
			return ((Validated<T>) delegate);
		}
		return delegate.isValid() ? successWith(delegate.value()) : failureWith(delegate.errors());
	}

	/**
	 * Returns a successful validation result that holds the given value.
	 *
	 * <p>
	 * The value may be {@code null} if the value type {@code T} is nullable. In that case
	 * {@link #isValid()} returns {@code true} and {@link #value()} returns {@code null}.
	 * @param value the value produced by the successful validation
	 * @param <T> value type in the case of success
	 * @return a successful validation result
	 * @since 0.12.0
	 */
	static <T extends @Nullable Object> Validated<T> successWith(T value) {
		return new Valid<>(value);
	}

	/**
	 * Returns a failed validation result that holds the given violations. The list is not
	 * copied.
	 * @param errors the constraint violations
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @throws IllegalArgumentException if {@code errors} is empty
	 * @since 0.12.0
	 */
	static <T extends @Nullable Object> Validated<T> failureWith(List<ConstraintViolation> errors) {
		return new Invalid<>(ConstraintViolations.of(errors));
	}

	/**
	 * Returns a failed validation result that holds the given violations.
	 * @param errors the constraint violations
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @throws IllegalArgumentException if {@code errors} is empty
	 * @since 0.12.0
	 */
	static <T extends @Nullable Object> Validated<T> failureWith(ConstraintViolation... errors) {
		return failureWith(Arrays.asList(errors));
	}

	/**
	 * Returns a failed validation result that holds a single violation of the given
	 * message, created with
	 * {@link ConstraintViolation#of(ViolationMessage, Object, Locale, Object...)}. This
	 * is the short way to fail in a hand-written validator: <pre>{@code
	 * ValueValidator<String, URI> uri = (s, locale, context) -> {
	 *     try {
	 *         return Validated.successWith(new URI(s));
	 *     }
	 *     catch (URISyntaxException e) {
	 *         return Validated.failureWith(ViolationMessage.of("uri.invalid", "\"{0}\" must be a URI"), s, locale);
	 *     }
	 * };
	 * }</pre>
	 *
	 * <p>
	 * The violation has an empty name, so it is reported on the validated value itself
	 * and takes the name of the property the validator is applied to.
	 * @param message the message key and the default message format
	 * @param violatedValue the value that violated the rule, possibly {@code null}
	 * @param locale the locale of the message
	 * @param args the message arguments from <code>{1}</code> on, each possibly
	 * {@code null}
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @since 0.18.0
	 */
	static <T extends @Nullable Object> Validated<T> failureWith(ViolationMessage message,
			@Nullable Object violatedValue, Locale locale, @Nullable Object... args) {
		return failureWith(ConstraintViolation.of(message, violatedValue, locale, args));
	}

	/**
	 * Returns a failed validation result that holds a single violation of the given
	 * message, created with {@link ConstraintViolation#of(ViolationMessage, Object...)}.
	 * This is the way to fail in a function that is not a validator and so is given
	 * neither the violated value nor the locale of the validation, such as a factory
	 * method that checks a rule of the domain: <pre>{@code
	 * record DateRange(LocalDate start, LocalDate end) {
	 *     static Validated<DateRange> of(LocalDate start, LocalDate end) {
	 *         return start.isBefore(end) ? Validated.successWith(new DateRange(start, end))
	 *                 : Validated.failureWith(ViolationMessage.of("dateRange.invalid", "start must be before end"));
	 *     }
	 * }
	 * }</pre>
	 *
	 * <p>
	 * The violation has an empty name and no violated value. Its message is formatted for
	 * {@link Locale#getDefault()} until a validator receives the result: the function of
	 * {@code flatFromMap} and {@code flatApply} and the validator given to
	 * {@code andThen} report it with the locale of the validation, as
	 * {@link #localizedWith(Locale)} does. A validator that is called on its own passes
	 * the locale it is given with
	 * {@link #failureWith(ViolationMessage, Object, Locale, Object...)}.
	 * @param message the message key and the default message format
	 * @param args the message arguments from <code>{1}</code> on, each possibly
	 * {@code null}
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @since 0.18.0
	 */
	static <T extends @Nullable Object> Validated<T> failureWith(ViolationMessage message, @Nullable Object... args) {
		return failureWith(ConstraintViolation.of(message, args));
	}

	/**
	 * Returns the validated value.
	 * @return the value produced by the successful validation; {@code null} if the
	 * validation succeeded with {@code null}
	 * @throws java.util.NoSuchElementException if the validation failed
	 */
	@Override
	T value();

	/**
	 * Returns the constraint violations of the failed validation.
	 * @return the constraint violations
	 * @throws java.util.NoSuchElementException if the validation succeeded
	 */
	@Override
	ConstraintViolations errors();

	/**
	 * Returns a validation result that holds the result of applying the given function to
	 * the validated value. The function is not applied if the validation failed; in that
	 * case the violations are kept.
	 * @param mapper the function applied to the validated value
	 * @param <T2> the type of the mapped value
	 * @return the mapped validation result or this failure
	 */
	@Override
	@SuppressWarnings("unchecked")
	default <T2 extends @Nullable Object> Validated<T2> map(Function<? super T, ? extends T2> mapper) {
		return isValid() ? Validated.successWith(mapper.apply(value())) : (Validated<T2>) this;
	}

	/**
	 * Returns the validation result produced by the given function from the validated
	 * value. The function is not applied if the validation failed; in that case the
	 * violations are kept.
	 * @param mapper the function from the validated value to the next validation result
	 * @param <T2> the value type of the next validation result
	 * @return the validation result produced by the function or this failure
	 */
	@Override
	@SuppressWarnings("unchecked")
	default <T2 extends @Nullable Object> Validated<T2> flatMap(
			Function<? super T, ? extends Validation<ConstraintViolation, ? extends T2>> mapper) {
		return isValid() ? Validated.of(mapper.apply(value())) : (Validated<T2>) this;
	}

	/**
	 * Applies the function held by the given validation result to the validated value. If
	 * both validations failed, their violations are accumulated with those of the given
	 * validation result first.
	 * @param validation the validation result holding the function to apply
	 * @param <U> the type of the result
	 * @return the successful result or the accumulated violations
	 */
	@Override
	default <U extends @Nullable Object> Validated<U> apply(
			Validation<ConstraintViolation, ? extends Function1<? super T, ? extends U>> validation) {
		return Validated.of(Validation.super.apply(validation));
	}

	/**
	 * Returns a combination of this validation result and the given one. Its
	 * {@code apply} method returns a {@code Validated} that holds the result of the
	 * function or the accumulated violations of both. Further validation results can be
	 * added with the {@code combine} method of the returned combination, up to 16.
	 * @param validation the validation result to combine
	 * @param <T2> the value type of the given validation result
	 * @return the combination of the two validation results
	 */
	@Override
	default <T2 extends @Nullable Object> ValidatedCombining2<T, T2> combine(
			Validation<ConstraintViolation, T2> validation) {
		return new ValidatedCombining2<>(this, Validated.of(validation));
	}

	/**
	 * Applies the given consumer to the validated value if the validation succeeded.
	 * @param consumer the consumer of the validated value
	 * @return this validation result
	 */
	@Override
	default Validated<T> peek(Consumer<? super T> consumer) {
		return Validated.of(Validation.super.peek(consumer));
	}

	/**
	 * Applies the given consumer to the constraint violations if the validation failed.
	 * @param consumer the consumer of the constraint violations
	 * @return this validation result
	 */
	@Override
	default Validated<T> peekErrors(Consumer<? super List<ConstraintViolation>> consumer) {
		return Validated.of(Validation.super.peekErrors(consumer));
	}

	/**
	 * Returns a validation result in which every constraint violation is located under
	 * the property with the given name.
	 * @param name the name of the property that contains the validated value
	 * @return a validation result with prefixed violation names and paths
	 * @since 0.18.0
	 * @see ConstraintViolation#prefixed(String)
	 */
	default Validated<T> prefixed(String name) {
		return Validated.of(this.mapError(violation -> violation.prefixed(name)));
	}

	/**
	 * Returns a validation result in which every constraint violation is located under
	 * the element at the given index.
	 * @param index the index of the element that contains the validated value
	 * @return a validation result with prefixed violation names and paths
	 * @since 0.18.0
	 * @see ConstraintViolation#prefixed(int)
	 */
	default Validated<T> prefixed(int index) {
		return Validated.of(this.mapError(violation -> violation.prefixed(index)));
	}

	/**
	 * Returns a validation result in which every constraint violation has the given
	 * locale, so that its message is formatted for it. A successful result, and a failed
	 * one whose violations have the locale already, is returned as it is.
	 *
	 * <p>
	 * A validator does this to a result that it does not create itself, so that the
	 * violations of one validation have one locale: the result of the function of
	 * {@code flatFromMap} and {@code flatApply}, which is not given the locale, and the
	 * result of the validator given to {@code andThen}. A hand-written validator does the
	 * same to the result of a function it calls: <pre>{@code
	 * ValueValidator<String, DateRange> dateRange = (s, locale, context) -> DateRange.parse(s).localizedWith(locale);
	 * }</pre>
	 * @param locale the locale of the violations
	 * @return a validation result whose violations have the locale
	 * @since 0.18.0
	 * @see ConstraintViolation#localizedWith(Locale)
	 */
	default Validated<T> localizedWith(Locale locale) {
		if (isValid()) {
			return this;
		}
		final ConstraintViolations errors = errors();
		final ConstraintViolations localized = errors.localizedWith(locale);
		return localized == errors ? this : new Invalid<>(localized);
	}

	/**
	 * Combines the given validation results into a validation result that holds a list of
	 * the values in iteration order. The violations of all failed validations are
	 * accumulated.
	 * @param values the validation results to combine
	 * @param <T> value type in the case of success
	 * @return a validation result holding the list of validated values or the accumulated
	 * violations
	 * @since 0.7.0
	 */
	static <T extends @Nullable Object> Validated<List<T>> sequence(Iterable<Validated<? extends T>> values) {
		return Validated.of(Validations.sequence(values));
	}

	/**
	 * Validates the given values with the given mapper and combines the results into a
	 * validation result that holds a list of the produced values in iteration order. The
	 * violations of all failed validations are accumulated.
	 * @param values the values to validate
	 * @param mapper the validator for each value
	 * @param <T> the type of the values to validate
	 * @param <U> the type of the produced values
	 * @return a validation result holding the list of produced values or the accumulated
	 * violations
	 * @since 0.7.0
	 */
	static <T, U extends @Nullable Object> Validated<List<U>> traverse(Iterable<T> values,
			Function<? super T, Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverse(values, mapper));
	}

	/**
	 * Validates the given values with the given mapper, passing the position of each
	 * value to the mapper, and combines the results into a validation result that holds a
	 * list of the produced values in iteration order. The violations of all failed
	 * validations are accumulated.
	 * @param values the values to validate
	 * @param mapper the validator for each value and its index
	 * @param <T> the type of the values to validate
	 * @param <U> the type of the produced values
	 * @return a validation result holding the list of produced values or the accumulated
	 * violations
	 * @since 0.7.0
	 */
	static <T, U extends @Nullable Object> Validated<List<U>> traverseIndexed(@Nullable Iterable<T> values,
			Validations.IndexedTraverser<? super T, Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseIndexed(values, mapper));
	}

	/**
	 * Validates the given values with the given mapper, passing the position of each
	 * value to the mapper, and collects the produced values into a collection created by
	 * the given factory. The violations of all failed validations are accumulated.
	 * @param values the values to validate
	 * @param mapper the validator for each value and its index
	 * @param factory supplier of the collection that receives the produced values
	 * @param <T> the type of the values to validate
	 * @param <U> the type of the produced values
	 * @param <C> the type of the resulting collection
	 * @return a validation result holding the collection of produced values or the
	 * accumulated violations
	 * @since 0.8.0
	 */
	static <T, U extends @Nullable Object, C extends Collection<U>> Validated<C> traverseIndexed(
			@Nullable Iterable<T> values, Validations.IndexedTraverser<? super T, Validated<? extends U>> mapper,
			Supplier<C> factory) {
		return Validated.of(Validations.traverseIndexed(values, mapper, factory));
	}

	/**
	 * Validates the value contained in the given optional, if any, and returns a
	 * validation result that holds an optional with the produced value. An empty optional
	 * validates successfully with an empty result.
	 * @param value the optional value to validate
	 * @param mapper the validator for the contained value
	 * @param <T> the contained type of the optional
	 * @param <U> the type of the produced value
	 * @return a validation result holding the optional produced value or the violations
	 * @since 0.8.0
	 */
	static <T, U extends @Nullable Object> Validated<Optional<@NonNull U>> traverseOptional(@Nullable Optional<T> value,
			Function<? super T, ? extends Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseOptional(value, mapper));
	}

}
