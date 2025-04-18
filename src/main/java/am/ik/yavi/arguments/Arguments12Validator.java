/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.3.0
 */
@FunctionalInterface
public interface Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> {

	/**
	 * Convert an Arguments1Validator that validates Arguments12 to an
	 * Arguments12Validator
	 * @param validator validator for Arguments12
	 * @param <A1> type of first argument
	 * @param <A2> type of argument at position 2
	 * @param <A3> type of argument at position 3
	 * @param <A4> type of argument at position 4
	 * @param <A5> type of argument at position 5
	 * @param <A6> type of argument at position 6
	 * @param <A7> type of argument at position 7
	 * @param <A8> type of argument at position 8
	 * @param <A9> type of argument at position 9
	 * @param <A10> type of argument at position 10
	 * @param <A11> type of argument at position 11
	 * @param <A12> type of argument at position 12
	 * @param <X> target result type
	 * @return arguments12 validator that takes arguments directly
	 * @since 0.16.0
	 */
	static <A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> unwrap(
			Arguments1Validator<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>, X> validator) {
		return new Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X>() {
			@Override
			public Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4,
					@Nullable A5 a5, @Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9,
					@Nullable A10 a10, @Nullable A11 a11, @Nullable A12 a12, Locale locale,
					ConstraintContext constraintContext) {
				return validator.validate(Arguments.of(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12), locale,
						constraintContext);
			}

			@Override
			public Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Supplier<X>> lazy() {
				return Arguments12Validator.unwrap(validator.lazy());
			}
		};
	}

	Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, Locale locale, ConstraintContext constraintContext);

	/**
	 * Convert this validator to one that validates Arguments12 as a single object.
	 * @return a validator that takes an Arguments12
	 * @since 0.16.0
	 */
	default Arguments1Validator<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>, X> wrap() {
		Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Supplier<X>> lazy = this.lazy();
		return new Arguments1Validator<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>, X>() {
			@Override
			public Validated<X> validate(Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12> args,
					Locale locale, ConstraintContext constraintContext) {
				final Arguments12<? extends A1, ? extends A2, ? extends A3, ? extends A4, ? extends A5, ? extends A6, ? extends A7, ? extends A8, ? extends A9, ? extends A10, ? extends A11, ? extends A12> nonNullArgs = Objects
					.requireNonNull(args);
				return Arguments12Validator.this.validate(nonNullArgs.arg1(), nonNullArgs.arg2(), nonNullArgs.arg3(),
						nonNullArgs.arg4(), nonNullArgs.arg5(), nonNullArgs.arg6(), nonNullArgs.arg7(),
						nonNullArgs.arg8(), nonNullArgs.arg9(), nonNullArgs.arg10(), nonNullArgs.arg11(),
						nonNullArgs.arg12(), locale, constraintContext);
			}

			@Override
			public Arguments1Validator<Arguments12<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12>, Supplier<X>> lazy() {
				return lazy.wrap();
			}
		};
	}

	/**
	 * @since 0.7.0
	 */
	default <X2> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X2> andThen(
			Function<? super X, ? extends X2> mapper) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale,
				constraintContext) -> Arguments12Validator.this
					.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale, constraintContext)
					.map(mapper);
	}

	/**
	 * @since 0.11.0
	 */
	default <X2> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X2> andThen(
			ValueValidator<? super X, X2> validator) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale,
				constraintContext) -> Arguments12Validator.this
					.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale, constraintContext)
					.flatMap(v -> validator.validate(v, locale, constraintContext));
	}

	/**
	 * @since 0.7.0
	 */
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends Arguments12<? extends A1, ? extends A2, ? extends A3, ? extends A4, ? extends A5, ? extends A6, ? extends A7, ? extends A8, ? extends A9, ? extends A10, ? extends A11, ? extends A12>> mapper) {
		return (a, locale, constraintContext) -> {
			final Arguments12<? extends A1, ? extends A2, ? extends A3, ? extends A4, ? extends A5, ? extends A6, ? extends A7, ? extends A8, ? extends A9, ? extends A10, ? extends A11, ? extends A12> args = mapper
				.apply(a);
			return Arguments12Validator.this.validate(args.arg1(), args.arg2(), args.arg3(), args.arg4(), args.arg5(),
					args.arg6(), args.arg7(), args.arg8(), args.arg9(), args.arg10(), args.arg11(), args.arg12(),
					locale, constraintContext);
		};
	}

	/**
	 * @since 0.10.0
	 */
	default Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Supplier<X>> lazy() {
		throw new UnsupportedOperationException("lazy is not implemented!");
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, Locale.getDefault(),
				ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, ConstraintContext constraintContext) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, Locale.getDefault(), constraintContext);
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, Locale locale) {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale, ConstraintGroup.DEFAULT);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12)
			.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, ConstraintContext constraintContext) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, constraintContext)
			.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, Locale locale) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale)
			.orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, @Nullable A5 a5,
			@Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9, @Nullable A10 a10, @Nullable A11 a11,
			@Nullable A12 a12, Locale locale, ConstraintContext constraintContext)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale, constraintContext)
			.orElseThrow(ConstraintViolationsException::new);
	}

}
