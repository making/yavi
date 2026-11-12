/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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
public sealed interface Validated<T> extends Validation<ConstraintViolation, T> {

	/**
	 * A successful validation result.
	 *
	 * @param value the validated value; {@code null} if the validation succeeded with
	 * {@code null}
	 * @param <T> value type
	 * @since 0.18.0
	 */
	record Valid<T> (T value) implements Validated<T> {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public @Nullable T valueNullable() {
			return this.value;
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
	record Invalid<T> (ConstraintViolations errors) implements Validated<T> {

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

		@Override
		public @Nullable T valueNullable() {
			return this.value();
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
	static <T> Validated<T> of(Validation<ConstraintViolation, ? extends T> delegate) {
		if (delegate instanceof Validated) {
			return ((Validated<T>) delegate);
		}
		return delegate.isValid() ? successWith(delegate.valueNullable()) : failureWith(delegate.errors());
	}

	/**
	 * Returns a successful validation result that holds the given value.
	 *
	 * <p>
	 * The value may be {@code null}. In that case {@link #isValid()} returns {@code true}
	 * and both {@link #value()} and {@link #valueNullable()} return {@code null}.
	 * @param value the value produced by the successful validation
	 * @param <T> value type in the case of success
	 * @return a successful validation result
	 * @since 0.12.0
	 */
	// A successful validation may hold null; T does not express that.
	@SuppressWarnings("NullAway")
	static <T> Validated<T> successWith(@Nullable T value) {
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
	static <T> Validated<T> failureWith(List<ConstraintViolation> errors) {
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
	static <T> Validated<T> failureWith(ConstraintViolation... errors) {
		return failureWith(Arrays.asList(errors));
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

	@Override
	@SuppressWarnings("unchecked")
	default <U, V extends Validation<ConstraintViolation, U>> V yieldSuccess(@Nullable U value) {
		return (V) Validated.successWith(value);
	}

	@Override
	@SuppressWarnings("unchecked")
	default <U, V extends Validation<ConstraintViolation, U>> V yieldFailure(List<ConstraintViolation> errors) {
		return (V) Validated.failureWith(errors);
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
	 * Returns a validation result in which {@code [index]} is appended to the name of
	 * every constraint violation.
	 * @param index the index to append to violation names
	 * @return a validation result with indexed violation names
	 * @since 0.7.0
	 */
	default Validated<T> indexed(int index) {
		return Validated.of(this.mapError(violation -> violation.indexed(index)));
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
	 * Combines the given validation results into a validation result that holds a list of
	 * the values in iteration order. The violations of all failed validations are
	 * accumulated.
	 * @param values the validation results to combine
	 * @param <T> value type in the case of success
	 * @return a validation result holding the list of validated values or the accumulated
	 * violations
	 * @since 0.7.0
	 */
	static <T> Validated<List<T>> sequence(Iterable<Validated<? extends T>> values) {
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
	static <T, U> Validated<List<U>> traverse(Iterable<T> values, Function<? super T, Validated<? extends U>> mapper) {
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
	static <T, U> Validated<List<U>> traverseIndexed(@Nullable Iterable<T> values,
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
	static <T, U, C extends Collection<U>> Validated<C> traverseIndexed(@Nullable Iterable<T> values,
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
	static <T, U> Validated<Optional<U>> traverseOptional(@Nullable Optional<T> value,
			Function<? super T, ? extends Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseOptional(value, mapper));
	}

}
