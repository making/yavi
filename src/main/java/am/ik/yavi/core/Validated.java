/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.fn.Validation;
import am.ik.yavi.fn.Validations;
import org.jspecify.annotations.Nullable;

/**
 * A specialized {@link Validation} whose errors are {@link ConstraintViolation}s.
 *
 * <p>
 * A {@code Validated} is either a success that holds a value or a failure that holds
 * {@link ConstraintViolations}. It is returned by applicative validators
 * ({@link ApplicativeValidator}, {@link ValueValidator}) and the generated
 * {@code am.ik.yavi.arguments.ArgumentsNValidator} types. The {@code combine} and
 * {@code apply} methods inherited from {@link Validation} accumulate the violations of
 * all combined validations.
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
 * @param <T> value type in the case of success
 * @since 0.6.0
 * @see Validation
 * @see ConstraintViolations
 */
public class Validated<T> extends Validation<ConstraintViolation, T> {

	/**
	 * The validation result that this instance delegates to.
	 */
	private final Validation<ConstraintViolation, ? extends T> delegate;

	/**
	 * Wraps the given validation result. If the argument already is a {@code Validated},
	 * it is returned as is.
	 * @param delegate the validation result to wrap
	 * @param <T> value type in the case of success
	 * @return the wrapped validation result
	 */
	@SuppressWarnings("unchecked")
	public static <T> Validated<T> of(Validation<ConstraintViolation, ? extends T> delegate) {
		if (delegate instanceof Validated) {
			return ((Validated<T>) delegate);
		}
		return new Validated<>(delegate);
	}

	/**
	 * Returns a successful validation result that holds the given value.
	 *
	 * <p>
	 * The value may be {@code null}. In that case {@link #isValid()} returns {@code true}
	 * but {@link #value()} throws {@link java.util.NoSuchElementException}; use
	 * {@link #valueNullable()} to obtain the {@code null} value.
	 * @param value the value produced by the successful validation
	 * @param <T> value type in the case of success
	 * @return a successful validation result
	 * @since 0.12.0
	 */
	public static <T> Validated<T> successWith(@Nullable T value) {
		return new Validated<>(Validation.success(value));
	}

	/**
	 * Returns a failed validation result that holds the given violations. The list is
	 * used as is and is not copied.
	 * @param errors the constraint violations
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @since 0.12.0
	 */
	public static <T> Validated<T> failureWith(List<ConstraintViolation> errors) {
		return new Validated<>(Validation.failure(errors));
	}

	/**
	 * Returns a failed validation result that holds the given violations.
	 * @param errors the constraint violations
	 * @param <T> value type in the case of success
	 * @return a failed validation result
	 * @since 0.12.0
	 */
	public static <T> Validated<T> failureWith(ConstraintViolation... errors) {
		return new Validated<>(Validation.failure(errors));
	}

	Validated(Validation<ConstraintViolation, ? extends T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Returns whether the validation succeeded.
	 * @return {@code true} if the validation succeeded, {@code false} otherwise
	 */
	@Override
	public boolean isValid() {
		return this.delegate.isValid();
	}

	/**
	 * Returns the validated value.
	 * @return the value produced by the successful validation
	 * @throws java.util.NoSuchElementException if the validation failed or the value is
	 * {@code null}
	 */
	@Override
	public T value() {
		return this.delegate.value();
	}

	/**
	 * Returns the validated value, which may be {@code null}.
	 * @return the value produced by the successful validation, possibly {@code null}
	 * @throws java.util.NoSuchElementException if the validation failed
	 */
	@Override
	public @Nullable T valueNullable() {
		return this.delegate.valueNullable();
	}

	/**
	 * Returns the constraint violations of the failed validation.
	 * @return the constraint violations
	 * @throws java.util.NoSuchElementException if the validation succeeded
	 */
	@Override
	public ConstraintViolations errors() {
		return ConstraintViolations.of(this.delegate.errors());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldSuccess(@Nullable U value) {
		return (V) Validated.of(Validation.success(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldFailure(List<ConstraintViolation> errors) {
		return (V) Validated.of(Validation.failure(errors));
	}

	/**
	 * Applies the given consumer to the validated value if the validation succeeded.
	 * @param consumer the consumer of the validated value
	 * @return this validation result
	 */
	@Override
	public Validated<T> peek(Consumer<? super T> consumer) {
		return Validated.of(super.peek(consumer));
	}

	/**
	 * Applies the given consumer to the constraint violations if the validation failed.
	 * @param consumer the consumer of the constraint violations
	 * @return this validation result
	 */
	@Override
	public Validated<T> peekErrors(Consumer<? super List<ConstraintViolation>> consumer) {
		return Validated.of(super.peekErrors(consumer));
	}

	/**
	 * Returns a validation result in which {@code [index]} is appended to the name of
	 * every constraint violation.
	 * @param index the index to append to violation names
	 * @return a validation result with indexed violation names
	 * @since 0.7.0
	 */
	public Validated<T> indexed(int index) {
		return Validated.of(this.mapError(violation -> violation.indexed(index)));
	}

	/**
	 * Converts this validation result to a sealed {@link ParseResult}, so that the
	 * success and failure cases can be handled exhaustively with pattern matching.
	 *
	 * <pre>{@code
	 * switch (validated.toParseResult()) {
	 * 	case ParseResult.Success<User>(var user) -> save(user);
	 * 	case ParseResult.Failure<User>(var issues) -> issues.forEach(issue -> report(issue.path(), issue.code()));
	 * }
	 * }</pre>
	 * @return a {@link ParseResult.Success} holding the validated value, which is
	 * {@code null} if this validation succeeded with {@code null}, or a
	 * {@link ParseResult.Failure} holding the {@link Issue}s of the violations
	 * @since 0.18.0
	 */
	// A successful validation may hold null; T does not express that.
	@SuppressWarnings("NullAway")
	public ParseResult<T> toParseResult() {
		if (this.isValid()) {
			return new ParseResult.Success<>(this.valueNullable());
		}
		return new ParseResult.Failure<>(this.errors().issues());
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
	public static <T> Validated<List<T>> sequence(Iterable<Validated<? extends T>> values) {
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
	public static <T, U> Validated<List<U>> traverse(Iterable<T> values,
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
	public static <T, U> Validated<List<U>> traverseIndexed(@Nullable Iterable<T> values,
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
	public static <T, U, C extends Collection<U>> Validated<C> traverseIndexed(@Nullable Iterable<T> values,
			Validations.IndexedTraverser<? super T, Validated<? extends U>> mapper, Supplier<C> factory) {
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
	public static <T, U> Validated<Optional<U>> traverseOptional(@Nullable Optional<T> value,
			Function<? super T, ? extends Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseOptional(value, mapper));
	}

}
