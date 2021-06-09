/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import static java.util.stream.Collectors.toList;

/**
 * An implementation similar to Vavr's
 * <a href="https://docs.vavr.io/#_validation">Validation</a> control.<br>
 * The Validation control is an applicative functor and facilitates accumulating errors.
 *
 * @param <E> value type in the case of failure
 * @param <T> value type in the case of success
 * @since 0.6.0
 */
public abstract class Validation<E, T> implements Serializable {
	private static final long serialVersionUID = 1L;

	public abstract boolean isValid();

	/**
	 * Returns the value of this {@code Validation} if is a {@code Success} or throws if
	 * this is an {@code Failure}.
	 *
	 * @return The value of this {@code Validation}
	 * @throws NoSuchElementException if this is an {@code Failure}
	 */
	public abstract T value();

	/**
	 * Returns the errors of this {@code Validation} if it is an {@code Failure} or throws
	 * if this is a {@code Success}.
	 *
	 * @return The errors, if present
	 * @throws NoSuchElementException if this is a {@code Success}
	 */
	public abstract List<E> errors();

	@SuppressWarnings("unchecked")
	public <T2, V extends Validation<E, T2>> V map(
			Function<? super T, ? extends T2> mapper) {
		return isValid() ? this.yieldSuccess(mapper.apply(value())) : (V) this;
	}

	@SuppressWarnings("unchecked")
	public <T2> Validation<? super E, ? extends T2> flatMap(
			Function<? super T, Validation<E, T2>> mapper) {
		return isValid() ? mapper.apply(value()) : (Validation<E, T2>) this;
	}

	public Validation<E, T> peek(Consumer<? super T> consumer) {
		if (isValid()) {
			consumer.accept(value());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public <E2> Validation<E2, T> mapErrors(
			Function<? super List<E>, ? extends List<E2>> errorsMapper) {
		return isValid() ? (Validation<E2, T>) this
				: Validation.failure(errorsMapper.apply(errors()));
	}

	/**
	 * @since 0.7.0
	 */
	@SuppressWarnings("unchecked")
	public <E2> Validation<E2, T> mapErrorsF(
			Function<? super E, ? extends E2> errorMapper) {
		return isValid() ? (Validation<E2, T>) this
				: Validation
				.failure(errors().stream().map(errorMapper).collect(toList()));
	}

	public <E2, T2> Validation<E2, T2> bimap(
			Function<? super List<E>, ? extends List<E2>> errorsMapper,
			Function<? super T, ? extends T2> mapper) {
		return isValid() ? Validation.success(mapper.apply(value()))
				: Validation.failure(errorsMapper.apply(errors()));
	}

	public Validation<E, T> peekErrors(Consumer<? super List<E>> consumer) {
		if (!isValid()) {
			consumer.accept(errors());
		}
		return this;
	}

	public <X extends Throwable> T orElseThrow(
			Function<? super List<E>, ? extends X> exceptionMapper) throws X {
		if (isValid()) {
			return value();
		}
		else {
			throw exceptionMapper.apply(errors());
		}
	}

	public T orElseGet(Function<? super List<E>, ? extends T> other) {
		if (isValid()) {
			return value();
		}
		else {
			return other.apply(errors());
		}
	}

	public <U> U fold(Function<? super List<E>, ? extends U> errorsMapper,
			Function<? super T, ? extends U> mapper) {
		return isValid() ? mapper.apply(value()) : errorsMapper.apply(errors());
	}

	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<E, U>> V yieldSuccess(U value) {
		return (V) Validation.success(value);
	}

	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<E, U>> V yieldFailure(List<E> errors) {
		return (V) Validation.failure(errors);
	}

	public <U, V extends Validation<E, U>> V apply(
			Validation<E, ? extends Function1<? super T, ? extends U>> validation) {
		if (isValid()) {
			if (validation.isValid()) {
				final Function1<? super T, ? extends U> f = validation.value();
				final U u = f.apply(this.value());
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

	public Either<List<E>, T> toEither() {
		return isValid() ? Either.right(value()) : Either.left(errors());
	}

	public <T2> Combining2<E, T, T2> combine(Validation<E, T2> validation) {
		return new Combining2<>(this, validation);
	}

	public static <E, T> Validation<E, T> fromEither(Either<List<E>, T> either) {
		return either.fold(Validation::failure, Validation::success);
	}

	public static <E, T> Validation<E, T> success(T value) {
		return new Success<>(value);
	}

	public static <E, T> Validation<E, T> failure(List<E> errors) {
		return new Failure<>(errors);
	}

	public static <E, T> Validation<E, T> failure(E... errors) {
		return new Failure<>(Arrays.asList(errors));
	}

	public static class Success<E, T> extends Validation<E, T> {
		private static final long serialVersionUID = 1L;

		public final T value;

		Success(T value) {
			if (value == null) {
				throw new IllegalArgumentException("'value' must not be null.");
			}
			this.value = value;
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public T value() {
			return this.value;
		}

		@Override
		public List<E> errors() throws RuntimeException {
			throw new NoSuchElementException(
					"errors of 'Success' Validation does not exist.");
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Success<?, ?> success = (Success<?, ?>) o;
			return value.equals(success.value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}
	}

	public static class Failure<E, T> extends Validation<E, T> {
		private static final long serialVersionUID = 1L;

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
			throw new NoSuchElementException(
					"value of 'Failure' Validation does not exists. Errors=" + errors);
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
