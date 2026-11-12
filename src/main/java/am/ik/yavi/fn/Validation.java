/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.fn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import static java.util.stream.Collectors.toList;

/**
 * An applicative result type that either holds a success value or accumulates errors.
 *
 * <p>
 * This type is modeled after Vavr's
 * <a href="https://docs.vavr.io/#_validation">Validation</a> control. A successful
 * validation holds a value, a failed one holds a non-empty list of errors.
 * {@link #combine(Validation)} and {@link #apply(Validation)} combine validation results
 * in applicative style and accumulate the errors of all failed validations instead of
 * stopping at the first one.
 *
 * <p>
 * YAVI's own validators return the specialized {@link am.ik.yavi.core.Validated}, whose
 * errors are constraint violations. This generic type is the functional core of that API.
 *
 * <p>
 * Example: <pre>{@code
 * Validation<String, String> name = Validation.success("Taro");
 * Validation<String, Integer> age = Validation.success(20);
 * Validation<String, String> label = name.combine(age).apply((n, a) -> n + " (" + a + ")");
 * }</pre>
 *
 * <p>
 * Instances are immutable. {@link #failure(List)} wraps the given error list without
 * copying it.
 *
 * @param <E> type of the errors in the case of failure
 * @param <T> type of the value in the case of success
 * @since 0.6.0
 */
public interface Validation<E, T> extends Serializable {

	/**
	 * Returns whether this validation is successful.
	 * @return {@code true} if this validation holds a value, {@code false} if it holds
	 * errors
	 */
	boolean isValid();

	/**
	 * Returns the value of this validation.
	 *
	 * <p>
	 * The value may be {@code null}; prefer {@link #valueNullable()} if a successful
	 * validation can hold {@code null}.
	 * @return the value of a successful validation
	 * @throws NoSuchElementException if this validation is a failure or the value is
	 * {@code null}
	 */
	T value();

	/**
	 * Returns the value of this validation, which may be {@code null}.
	 * @return the value of a successful validation, possibly {@code null}
	 * @throws NoSuchElementException if this validation is a failure
	 * @since 0.8.3
	 */
	@Nullable T valueNullable();

	/**
	 * Returns the errors of this validation. The returned list is unmodifiable.
	 * @return the errors of a failed validation
	 * @throws NoSuchElementException if this validation is successful
	 */
	List<E> errors();

	/**
	 * Returns a validation holding the result of applying the given function to the value
	 * of this validation. The function is not applied if this validation is a failure; in
	 * that case the errors are kept.
	 *
	 * <p>
	 * An implementation with its own type overrides this method to return that type, as
	 * {@link am.ik.yavi.core.Validated} does.
	 * @param mapper mapper applied to the value of a successful validation
	 * @param <T2> type of the mapped value
	 * @return the mapped successful validation or this failure
	 */
	@SuppressWarnings("unchecked")
	default <T2> Validation<E, T2> map(Function<? super T, ? extends T2> mapper) {
		return isValid() ? this.yieldSuccess(mapper.apply(value())) : (Validation<E, T2>) this;
	}

	/**
	 * Returns the validation produced by the given function from the value of this
	 * validation. The function is not applied if this validation is a failure; in that
	 * case the errors are kept.
	 *
	 * <p>
	 * An implementation with its own type overrides this method to return that type, as
	 * {@link am.ik.yavi.core.Validated} does.
	 * @param mapper mapper from the value of a successful validation to the next
	 * validation
	 * @param <T2> type of the value of the next validation
	 * @return the validation produced by the mapper or this failure
	 */
	@SuppressWarnings("unchecked")
	default <T2> Validation<E, T2> flatMap(Function<? super T, ? extends Validation<E, ? extends T2>> mapper) {
		return isValid() ? (Validation<E, T2>) mapper.apply(value()) : (Validation<E, T2>) this;
	}

	/**
	 * Applies the given consumer to the value if this validation is successful and
	 * returns this validation unchanged. The consumer is not called if this validation is
	 * a failure.
	 * @param consumer consumer of the value of a successful validation
	 * @return this validation
	 */
	default Validation<E, T> peek(Consumer<? super T> consumer) {
		if (isValid()) {
			consumer.accept(value());
		}
		return this;
	}

	/**
	 * Returns a failed validation whose errors are the result of applying the given
	 * function to the error list, or this validation unchanged if it is successful. The
	 * function is not applied on success.
	 * @param errorsMapper mapper applied to the error list of a failed validation
	 * @param <E2> type of the mapped errors
	 * @return the mapped failure or this successful validation
	 */
	@SuppressWarnings("unchecked")
	default <E2> Validation<E2, T> mapErrors(Function<? super List<E>, ? extends List<E2>> errorsMapper) {
		return isValid() ? (Validation<E2, T>) this : Validation.failure(errorsMapper.apply(errors()));
	}

	/**
	 * Returns a failed validation whose errors are the result of applying the given
	 * function to each error, or this validation unchanged if it is successful. The
	 * function is not applied on success.
	 * @param errorMapper mapper applied to each error of a failed validation
	 * @param <E2> type of the mapped errors
	 * @return the mapped failure or this successful validation
	 * @since 0.8.2
	 */
	@SuppressWarnings("unchecked")
	default <E2> Validation<E2, T> mapError(Function<? super E, ? extends E2> errorMapper) {
		return isValid() ? (Validation<E2, T>) this
				: Validation.failure(errors().stream().map(errorMapper).collect(toList()));
	}

	/**
	 * Returns a validation with the value mapped by the given value mapper if this
	 * validation is successful, or with the errors mapped by the given errors mapper if
	 * it is a failure. Only one of the functions is applied.
	 * @param errorsMapper mapper applied to the error list of a failed validation
	 * @param mapper mapper applied to the value of a successful validation
	 * @param <E2> type of the mapped errors
	 * @param <T2> type of the mapped value
	 * @return the mapped successful validation or the mapped failure
	 */
	default <E2, T2> Validation<E2, T2> bimap(Function<? super List<E>, ? extends List<E2>> errorsMapper,
			Function<? super T, ? extends T2> mapper) {
		return isValid() ? Validation.success(mapper.apply(value())) : Validation.failure(errorsMapper.apply(errors()));
	}

	/**
	 * Applies the given consumer to the error list if this validation is a failure and
	 * returns this validation unchanged. The consumer is not called if this validation is
	 * successful.
	 * @param consumer consumer of the errors of a failed validation
	 * @return this validation
	 */
	default Validation<E, T> peekErrors(Consumer<? super List<E>> consumer) {
		if (!isValid()) {
			consumer.accept(errors());
		}
		return this;
	}

	/**
	 * Returns the value if this validation is successful, otherwise throws the exception
	 * produced by the given function from the error list.
	 * @param exceptionMapper mapper from the errors to the exception to throw
	 * @param <X> type of the thrown exception
	 * @return the value of a successful validation
	 * @throws X if this validation is a failure
	 * @throws NoSuchElementException if this validation is successful and its value is
	 * {@code null}
	 */
	default <X extends Throwable> T orElseThrow(Function<? super List<E>, ? extends X> exceptionMapper) throws X {
		if (isValid()) {
			return value();
		}
		else {
			throw exceptionMapper.apply(errors());
		}
	}

	/**
	 * Throws the exception produced by the given function from the error list if this
	 * validation is a failure. Does nothing if this validation is successful.
	 * @param exceptionMapper mapper from the errors to the exception to throw
	 * @param <X> type of the thrown exception
	 * @throws X if this validation is a failure
	 * @since 0.10.0
	 */
	default <X extends Throwable> void throwIfInvalid(Function<? super List<E>, ? extends X> exceptionMapper) throws X {
		if (!isValid()) {
			throw exceptionMapper.apply(errors());
		}
	}

	/**
	 * Returns the value if this validation is successful, otherwise the result of
	 * applying the given function to the error list.
	 * @param other function producing an alternative value from the errors
	 * @return the value of a successful validation or the alternative value
	 */
	default T orElseGet(Function<? super List<E>, ? extends T> other) {
		if (isValid()) {
			return value();
		}
		else {
			return other.apply(errors());
		}
	}

	/**
	 * Returns the result of applying one of the given functions depending on the outcome:
	 * the value mapper if this validation is successful, the errors mapper if it is a
	 * failure.
	 * @param errorsMapper mapper applied to the error list of a failed validation
	 * @param mapper mapper applied to the value of a successful validation
	 * @param <U> type of the result
	 * @return the result produced by the applied mapper
	 */
	default <U> U fold(Function<? super List<E>, ? extends U> errorsMapper, Function<? super T, ? extends U> mapper) {
		return isValid() ? mapper.apply(value()) : errorsMapper.apply(errors());
	}

	/**
	 * Returns a successful validation holding the given value. Implementations can
	 * override this to return their own type, as {@link am.ik.yavi.core.Validated} does.
	 * @param value the value, possibly {@code null}
	 * @param <U> type of the value
	 * @param <V> the concrete validation type returned
	 * @return a successful validation
	 */
	@SuppressWarnings("unchecked")
	default <U, V extends Validation<E, U>> V yieldSuccess(@Nullable U value) {
		return (V) Validation.success(value);
	}

	/**
	 * Returns a failed validation holding the given errors. Implementations can override
	 * this to return their own type, as {@link am.ik.yavi.core.Validated} does.
	 * @param errors the errors
	 * @param <U> type of the value in the case of success
	 * @param <V> the concrete validation type returned
	 * @return a failed validation
	 */
	@SuppressWarnings("unchecked")
	default <U, V extends Validation<E, U>> V yieldFailure(List<E> errors) {
		return (V) Validation.failure(errors);
	}

	/**
	 * Applies the function held by the given validation to the value of this validation.
	 *
	 * <p>
	 * If both validations are successful, the function is called with the value (which
	 * may be {@code null}) and the result is returned as a success; a {@code null}
	 * function produces a success holding {@code null}. If exactly one validation fails,
	 * its errors are returned. If both fail, their errors are accumulated with the errors
	 * of the given validation first, that is, in the order in which the validations were
	 * combined.
	 * @param validation validation holding the function to apply
	 * @param <U> type of the result
	 * @param <V> the concrete validation type returned
	 * @return the successful result or the accumulated errors
	 */
	default <U, V extends Validation<E, U>> V apply(
			Validation<E, ? extends Function1<? super T, ? extends U>> validation) {
		if (isValid()) {
			if (validation.isValid()) {
				final Function1<? super T, ? extends U> f = validation.valueNullable();
				final U u = f == null ? null : f.apply(this.valueNullable());
				return this.yieldSuccess(u);
			}
			else {
				final List<E> errors = validation.errors();
				return this.yieldFailure(errors);
			}
		}
		else {
			final List<E> errors = this.errors();
			if (validation.isValid()) {
				return this.yieldFailure(errors);
			}
			else {
				final List<E> errorsList = new ArrayList<>(validation.errors());
				errorsList.addAll(errors);
				return this.yieldFailure(errorsList);
			}
		}
	}

	/**
	 * Returns a combination of this validation and the given one, which can be applied to
	 * a function with {@link Combining2#apply(Function2)}. Further validations can be
	 * added with the {@code combine} method of the returned combination, up to 16.
	 * @param validation the validation to combine
	 * @param <T2> type of the value of the given validation
	 * @return the combination of the two validations
	 */
	default <T2> Combining2<E, T, T2> combine(Validation<E, T2> validation) {
		return new Combining2<>(this, validation);
	}

	/**
	 * Returns a successful validation holding the given value.
	 * @param value the value, possibly {@code null}
	 * @param <E> type of the errors in the case of failure
	 * @param <T> type of the value
	 * @return a successful validation
	 */
	static <E, T> Validation<E, T> success(@Nullable T value) {
		return new Success<>(value);
	}

	/**
	 * Returns a failed validation holding the given errors. The list is wrapped without
	 * copying and must not be {@code null} or empty.
	 * @param errors the errors
	 * @param <E> type of the errors
	 * @param <T> type of the value in the case of success
	 * @return a failed validation
	 * @throws IllegalArgumentException if {@code errors} is {@code null} or empty
	 */
	static <E, T> Validation<E, T> failure(List<E> errors) {
		return new Failure<>(errors);
	}

	/**
	 * Returns a failed validation holding the given errors.
	 * @param errors the errors, must not be empty
	 * @param <E> type of the errors
	 * @param <T> type of the value in the case of success
	 * @return a failed validation
	 * @throws IllegalArgumentException if no error is given
	 */
	@SuppressWarnings("unchecked")
	static <E, T> Validation<E, T> failure(E... errors) {
		return new Failure<>(Arrays.asList(errors));
	}

	/**
	 * A successful {@link Validation} that holds a value.
	 *
	 * <p>
	 * The value may be {@code null}; in that case use {@link #valueNullable()}, because
	 * {@link #value()} throws for a {@code null} value.
	 *
	 * @param <E> type of the errors in the case of failure
	 * @param <T> type of the value
	 * @since 0.6.0
	 */
	class Success<E, T> implements Validation<E, T> {

		private static final long serialVersionUID = 1L;

		/**
		 * The value held by this successful validation. May be {@code null}.
		 */
		public final @Nullable T value;

		Success(@Nullable T value) {
			this.value = value;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public T value() {
			if (this.value == null) {
				throw new NoSuchElementException("'value' is null.");
			}
			return this.value;
		}

		@Override
		@Nullable public T valueNullable() {
			return this.value;
		}

		@Override
		public List<E> errors() throws RuntimeException {
			throw new NoSuchElementException("errors of 'Success' Validation does not exist.");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Success<?, ?> success = (Success<?, ?>) o;
			return Objects.equals(value, success.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

	}

	/**
	 * A failed {@link Validation} that holds errors.
	 *
	 * <p>
	 * The error list must contain at least one error.
	 *
	 * @param <E> type of the errors
	 * @param <T> type of the value in the case of success
	 * @since 0.6.0
	 */
	class Failure<E, T> implements Validation<E, T> {

		private static final long serialVersionUID = 1L;

		/**
		 * The errors held by this failed validation.
		 */
		private final List<E> errors;

		Failure(List<E> errors) {
			if (errors == null) {
				throw new IllegalArgumentException("'errors' must not be null.");
			}
			if (errors.isEmpty()) {
				throw new IllegalArgumentException("'errors' must not be empty.");
			}
			this.errors = Collections.unmodifiableList(errors);
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public T value() {
			throw new NoSuchElementException("value of 'Failure' Validation does not exists. Errors=" + errors);
		}

		@Override
		@Nullable public T valueNullable() {
			return this.value();
		}

		@Override
		public List<E> errors() {
			return this.errors;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Failure<?, ?> failure = (Failure<?, ?>) o;
			return errors.equals(failure.errors);
		}

		@Override
		public int hashCode() {
			return Objects.hash(errors);
		}

	}

}
