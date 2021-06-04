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
public interface Arguments5Validator<A1, A2, A3, A4, A5, X> {

	Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale,
			ConstraintGroup constraintGroup);

	/**
	 * @since 0.7.0
	 */
	default <X2> Arguments5Validator<A1, A2, A3, A4, A5, X2> andThen(
			Function<? super X, ? extends X2> mapper) {
		return (a1, a2, a3, a4, a5, locale, constraintGroup) -> Arguments5Validator.this
				.validate(a1, a2, a3, a4, a5, locale, constraintGroup).map(mapper);
	}

	/**
	 * @since 0.7.0
	 */
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends Arguments5<? extends A1, ? extends A2, ? extends A3, ? extends A4, ? extends A5>> mapper) {
		return (a, locale, constraintGroup) -> {
			final Arguments5<? extends A1, ? extends A2, ? extends A3, ? extends A4, ? extends A5> args = mapper
					.apply(a);
			return Arguments5Validator.this.validate(args.arg1(), args.arg2(),
					args.arg3(), args.arg4(), args.arg5(), locale, constraintGroup);
		};
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5) {
		return this.validate(a1, a2, a3, a4, a5, Locale.getDefault(),
				ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5,
			ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, Locale.getDefault(), constraintGroup);
	}

	default Validated<X> validate(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale) {
		return this.validate(a1, a2, a3, a4, a5, locale, ConstraintGroup.DEFAULT);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5,
			ConstraintGroup constraintGroup) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, constraintGroup)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, locale)
				.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale,
			ConstraintGroup constraintGroup) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, locale, constraintGroup)
				.orElseThrow(ConstraintViolationsException::new);
	}

	/**
	 * Use {@link #validate(Object, Object, Object, Object, Object)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5) {
		return this.validate(a1, a2, a3, a4, a5);
	}

	/**
	 * Use {@link #validate(Object, Object, Object, Object, Object, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5,
			ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, constraintGroup);
	}

	/**
	 * Use {@link #validate(Object, Object, Object, Object, Object, Locale)} instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale) {
		return this.validate(a1, a2, a3, a4, a5, locale);
	}

	/**
	 * Use
	 * {@link #validate(Object, Object, Object, Object, Object, Locale, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default Validated<X> validateArgs(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, Locale locale,
			ConstraintGroup constraintGroup) {
		return this.validate(a1, a2, a3, a4, a5, locale, constraintGroup);
	}

	/**
	 * Use {@link #validated(Object, Object, Object, Object, Object, ConstraintGroup)}
	 * instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5,
			ConstraintGroup constraintGroup) {
		throw new UnsupportedOperationException(
				"validateAndThrowIfInvalid is not supported. Consider using validated method instead.");
	}

	/**
	 * Use {@link #validated(Object, Object, Object, Object, Object)} instead
	 */
	@Deprecated
	default void validateAndThrowIfInvalid(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5) {
		this.validateAndThrowIfInvalid(a1, a2, a3, a4, a5, ConstraintGroup.DEFAULT);
	}
}
