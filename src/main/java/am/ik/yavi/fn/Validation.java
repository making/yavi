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

/**
 * An implementation similar to Vavr's
 * <a href="https://docs.vavr.io/#_validation">Validation</a> control.<br>
 * The Validation control is an applicative functor and facilitates accumulating errors.
 *
 * @param <E> value type in the case of failure
 * @param <T> value type in the case of success
 * @since 0.6.0
 */
public interface Validation<E, T> extends Serializable {
	boolean isValid();

	/**
	 * Returns the value of this {@code Validation} if is a {@code Success} or throws if
	 * this is an {@code Failure}.
	 *
	 * @return The value of this {@code Validation}
	 * @throws NoSuchElementException if this is an {@code Failure}
	 */
	T value();

	/**
	 * Returns the errors of this {@code Validation} if it is an {@code Failure} or throws
	 * if this is a {@code Success}.
	 *
	 * @return The errors, if present
	 * @throws NoSuchElementException if this is a {@code Success}
	 */
	List<E> errors();

	@SuppressWarnings("unchecked")
	default <T2> Validation<E, T2> map(Function<? super T, ? extends T2> mapper) {
		return isValid() ? Validation.success(mapper.apply(value()))
				: (Validation<E, T2>) this;
	}

	@SuppressWarnings("unchecked")
	default <T2> Validation<? super E, ? extends T2> flatMap(
			Function<? super T, Validation<? super E, ? extends T2>> mapper) {
		return isValid() ? mapper.apply(value()) : (Validation<E, T2>) this;
	}

	default Validation<E, T> peek(Consumer<? super T> consumer) {
		if (isValid()) {
			consumer.accept(value());
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	default <E2> Validation<E2, T> mapErrors(
			Function<? super List<E>, ? extends List<E2>> errorsMapper) {
		return isValid() ? (Validation<E2, T>) this
				: Validation.failure(errorsMapper.apply(errors()));
	}

	default <E2, T2> Validation<E2, T2> bimap(
			Function<? super List<E>, ? extends List<E2>> errorsMapper,
			Function<? super T, ? extends T2> mapper) {
		return isValid() ? Validation.success(mapper.apply(value()))
				: Validation.failure(errorsMapper.apply(errors()));
	}

	default Validation<E, T> peekErrors(Consumer<? super List<E>> consumer) {
		if (!isValid()) {
			consumer.accept(errors());
		}
		return this;
	}

	default <X extends Throwable> T orElseThrow(
			Function<? super List<E>, ? extends X> exceptionMapper) throws X {
		if (isValid()) {
			return value();
		}
		else {
			throw exceptionMapper.apply(errors());
		}
	}

	default <U> U fold(Function<? super List<E>, ? extends U> errorsMapper,
			Function<? super T, ? extends U> mapper) {
		return isValid() ? mapper.apply(value()) : errorsMapper.apply(errors());
	}

	default <U> Validation<E, U> apply(
			Validation<E, ? extends Function1<? super T, ? extends U>> validation) {
		if (isValid()) {
			if (validation.isValid()) {
				final Function1<? super T, ? extends U> f = validation.value();
				final U u = f.apply(this.value());
				return Validation.success(u);
			}
			else {
				final List<E> errors = validation.errors();
				return Validation.failure(errors);
			}
		}
		else {
			final List<E> errors = this.errors();
			if (validation.isValid()) {
				return Validation.failure(errors);
			}
			else {
				final List<E> errorsList = new ArrayList<>(validation.errors());
				errorsList.addAll(errors);
				return Validation.failure(errorsList);
			}
		}
	}

	default Either<List<E>, T> toEither() {
		return isValid() ? Either.right(value()) : Either.left(errors());
	}

	default <T2> Composing2<E, T, T2> compose(Validation<E, T2> validation) {
		return new Composing2<>(this, validation);
	}

	static <E, T> Validation<E, T> fromEither(Either<List<E>, T> either) {
		return either.fold(Validation::failure, Validation::success);
	}

	static <E, T> Validation<E, T> success(T value) {
		return new Success<>(value);
	}

	static <E, T> Validation<E, T> failure(List<E> errors) {
		return new Failure<>(errors);
	}

	static <E, T> Validation<E, T> failure(E... errors) {
		return new Failure<>(Arrays.asList(errors));
	}

	class Success<E, T> implements Validation<E, T> {
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
			throw new NoSuchElementException("errors of 'Success' Validation");
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

	class Failure<E, T> implements Validation<E, T> {
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
			throw new NoSuchElementException("get of 'Failure' Validation");
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
