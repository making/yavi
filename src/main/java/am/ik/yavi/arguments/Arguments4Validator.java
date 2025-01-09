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
package am.ik.yavi.arguments;

import java.util.Locale;
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
public interface Arguments4Validator<A1, A2, A3, A4, X> {

	Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, Locale locale,
			ConstraintContext constraintContext);

	/**
	 * @since 0.7.0
	 */
	default <X2> Arguments4Validator<A1, A2, A3, A4, X2> andThen(Function<? super X, ? extends X2> mapper) {
		return (a1, a2, a3, a4, locale, constraintContext) -> Arguments4Validator.this
			.validate(a1, a2, a3, a4, locale, constraintContext)
			.map(mapper);
	}

	/**
	 * @since 0.11.0
	 */
	default <X2> Arguments4Validator<A1, A2, A3, A4, X2> andThen(ValueValidator<? super X, X2> validator) {
		return (a1, a2, a3, a4, locale, constraintContext) -> Arguments4Validator.this
			.validate(a1, a2, a3, a4, locale, constraintContext)
			.flatMap(v -> validator.validate(v, locale, constraintContext));
	}

	/**
	 * @since 0.7.0
	 */
	default <A> Arguments1Validator<A, X> compose(
			Function<? super A, ? extends Arguments4<? extends A1, ? extends A2, ? extends A3, ? extends A4>> mapper) {
		return (a, locale, constraintContext) -> {
			final Arguments4<? extends A1, ? extends A2, ? extends A3, ? extends A4> args = mapper.apply(a);
			return Arguments4Validator.this.validate(args.arg1(), args.arg2(), args.arg3(), args.arg4(), locale,
					constraintContext);
		};
	}

	/**
	 * @since 0.10.0
	 */
	default Arguments4Validator<A1, A2, A3, A4, Supplier<X>> lazy() {
		throw new UnsupportedOperationException("lazy is not implemented!");
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4) {
		return this.validate(a1, a2, a3, a4, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4,
			ConstraintContext constraintContext) {
		return this.validate(a1, a2, a3, a4, Locale.getDefault(), constraintContext);
	}

	default Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, Locale locale) {
		return this.validate(a1, a2, a3, a4, locale, ConstraintGroup.DEFAULT);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4,
			ConstraintContext constraintContext) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, constraintContext).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, Locale locale)
			throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, locale).orElseThrow(ConstraintViolationsException::new);
	}

	default X validated(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, Locale locale,
			ConstraintContext constraintContext) throws ConstraintViolationsException {
		return this.validate(a1, a2, a3, a4, locale, constraintContext).orElseThrow(ConstraintViolationsException::new);
	}

}
