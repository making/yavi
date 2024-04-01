/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.fn.Validation;
import am.ik.yavi.fn.Validations;
import am.ik.yavi.jsr305.Nullable;

/**
 * a specialized {@code Validation} type that regards {@code List<E>} as
 * {@code ConstraintViolations}
 *
 * @param <T> value type in the case of success
 * @since 0.6.0
 */
public class Validated<T> extends Validation<ConstraintViolation, T> {
	private final Validation<ConstraintViolation, ? extends T> delegate;

	@SuppressWarnings("unchecked")
	public static <T> Validated<T> of(
			Validation<ConstraintViolation, ? extends T> delegate) {
		if (delegate instanceof Validated) {
			return ((Validated<T>) delegate);
		}
		return new Validated<>(delegate);
	}

	/**
	 * A wrapper of {@link am.ik.yavi.fn.Validation#success(Object)}
	 *
	 * @since 0.12.0
	 */
	public static <T> Validated<T> successWith(@Nullable T value) {
		return new Validated<>(Validation.success(value));
	}

	/**
	 * A wrapper of {@link am.ik.yavi.fn.Validation#failure(List)}
	 *
	 * @since 0.12.0
	 */
	public static <T> Validated<T> failureWith(List<ConstraintViolation> errors) {
		return new Validated<>(Validation.failure(errors));
	}

	/**
	 * A wrapper of {@link am.ik.yavi.fn.Validation#failure(Object[])}
	 *
	 * @since 0.12.0
	 */
	public static <T> Validated<T> failureWith(ConstraintViolation... errors) {
		return new Validated<>(Validation.failure(errors));
	}

	Validated(Validation<ConstraintViolation, ? extends T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isValid() {
		return this.delegate.isValid();
	}

	@Override
	public T value() {
		return this.delegate.value();
	}

	@Override
	public T valueNullable() {
		return this.delegate.valueNullable();
	}

	@Override
	public ConstraintViolations errors() {
		return ConstraintViolations.of(this.delegate.errors());
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldSuccess(
			@Nullable U value) {
		return (V) Validated.of(Validation.success(value));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <U, V extends Validation<ConstraintViolation, U>> V yieldFailure(
			List<ConstraintViolation> errors) {
		return (V) Validated.of(Validation.failure(errors));
	}

	@Override
	public Validated<T> peek(Consumer<? super T> consumer) {
		return Validated.of(super.peek(consumer));
	}

	@Override
	public Validated<T> peekErrors(Consumer<? super List<ConstraintViolation>> consumer) {
		return Validated.of(super.peekErrors(consumer));
	}

	/**
	 * @since 0.7.0
	 */
	public Validated<T> indexed(int index) {
		return Validated.of(this.mapError(violation -> violation.indexed(index)));
	}

	/**
	 * @since 0.7.0
	 */
	public static <T> Validated<List<T>> sequence(
			Iterable<Validated<? extends T>> values) {
		return Validated.of(Validations.sequence(values));
	}

	/**
	 * @since 0.7.0
	 */
	public static <T, U> Validated<List<U>> traverse(Iterable<T> values,
			Function<? super T, Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverse(values, mapper));
	}

	/**
	 * @since 0.7.0
	 */
	public static <T, U> Validated<List<U>> traverseIndexed(Iterable<T> values,
			Validations.IndexedTraverser<? super T, Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseIndexed(values, mapper));
	}

	/**
	 * @since 0.8.0
	 */
	public static <T, U, C extends Collection<U>> Validated<C> traverseIndexed(
			Iterable<T> values,
			Validations.IndexedTraverser<? super T, Validated<? extends U>> mapper,
			Supplier<C> factory) {
		return Validated.of(Validations.traverseIndexed(values, mapper, factory));
	}

	/**
	 * @since 0.8.0
	 */
	public static <T, U> Validated<Optional<U>> traverseOptional(Optional<T> value,
			Function<? super T, ? extends Validated<? extends U>> mapper) {
		return Validated.of(Validations.traverseOptional(value, mapper));
	}

}
