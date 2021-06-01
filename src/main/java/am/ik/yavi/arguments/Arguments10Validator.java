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
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Function;

import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
@FunctionalInterface
public interface Arguments10Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X> {

	Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9,
			A10 a10, Locale locale, ConstraintGroup constraintGroup);

	/**
	 * @since 0.7.0
	 */
	default <X2> Arguments10Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, X2> map(
			Function<? super X, ? extends X2> mapper) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale,
				constraintGroup) -> Arguments10Validator.this.validate(a1, a2, a3, a4, a5,
						a6, a7, a8, a9, a10, locale, constraintGroup).map(mapper);
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8,
			A9 a9, A10 a10) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, Locale.getDefault(),
				ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8,
			A9 a9, A10 a10, ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, Locale.getDefault(),
				constraintGroup);
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8,
			A9 a9, A10 a10, Locale locale) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale,
				ConstraintGroup.DEFAULT);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9,
			A10 a10) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9,
			A10 a10, ConstraintGroup constraintGroup)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, constraintGroup)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9,
			A10 a10, Locale locale) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9,
			A10 a10, Locale locale, ConstraintGroup constraintGroup)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale,
				constraintGroup).orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Use
	 * {@link #validate(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7,
			A8 a8, A9 a9, A10 a10) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10);
	}

	/**
	 * Use
	 * {@link #validate(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7,
			A8 a8, A9 a9, A10 a10, ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, constraintGroup);
	}

	/**
	 * Use
	 * {@link #validate(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Locale)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7,
			A8 a8, A9 a9, A10 a10, Locale locale) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale);
	}

	/**
	 * Use
	 * {@link #validate(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Locale, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7,
			A8 a8, A9 a9, A10 a10, Locale locale, ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, locale,
				constraintGroup);
	}

	/**
	 * Use
	 * {@link #validated(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10, ConstraintGroup constraintGroup) {
		throw new UnsupportedOperationException(
				"validateAndThrowIfInvalid is not supported. Consider using validated method instead.");
	}

	/**
	 * Use
	 * {@link #validated(Object, Object, Object, Object, Object, Object, Object, Object, Object, Object)}
	 * instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6,
			A7 a7, A8 a8, A9 a9, A10 a10) {
		this.validateAndThrowIfInvalid(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10,
				ConstraintGroup.DEFAULT);
	}
}
