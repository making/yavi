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
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.BiConsumer;

import am.ik.yavi.fn.Validation;

@FunctionalInterface
public interface Validatable<T> {

	/**
	 * Validates all constraints on {@code target}.
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @param constraintContext constraint context to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	ConstraintViolations validate(T target, Locale locale, ConstraintContext constraintContext);

	/**
	 * Validates all constraints on {@code target}. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint context.
	 * @param target target to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target) {
		return this.validate(target, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}.<br>
	 * {@code ConstraintGroup.DEFAULT} is used as a constraint context.
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target, Locale locale) {
		return this.validate(target, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}. <br>
	 * {@code Locale.getDefault()} is used to locate the violation messages.
	 * @param target target to validate
	 * @param constraintContext constraint context to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target, ConstraintContext constraintContext) {
		return this.validate(target, Locale.getDefault(), constraintContext);
	}

	/**
	 * Returns the corresponding applicative validator
	 * @return applicative validator
	 * @since 0.6.0
	 */
	default ApplicativeValidator<T> applicative() {
		return (target, locale, constraintContext) -> {
			final ConstraintViolations violations = Validatable.this.validate(target, locale, constraintContext);
			if (violations.isValid()) {
				return Validated.of(Validation.success(target));
			}
			else {
				return Validated.of(Validation.failure(violations));
			}
		};
	}

	/**
	 * Converts given applicative validator to a regular validator.
	 * @param applicative applicative validator to convert
	 * @return regular validator
	 * @since 0.11.0
	 */
	static <A1, R> Validatable<A1> from(ValueValidator<? super A1, ? extends R> applicative) {
		return (target, locale, constraintGroup) -> applicative.validate(target, locale, constraintGroup)
			.fold(ConstraintViolations::of, result -> new ConstraintViolations());
	}

	/**
	 * Convert the validator to a biconsumer
	 * @param errorHandler error handler
	 * @param <E> error type
	 * @return bi consumer
	 * @since 0.13.0
	 */
	default <E> BiConsumer<T, E> toBiConsumer(ErrorHandler<E> errorHandler) {
		return (target, errors) -> {
			final ConstraintViolations violations = Validatable.this.validate(target);
			if (!violations.isValid()) {
				violations.apply((name, messageKey, args, defaultMessage) -> errorHandler.handleError(errors, name,
						messageKey, args, defaultMessage));
			}
		};
	}

	/**
	 * Set whether to enable fail fast mode. If enabled, Validatable returns from the
	 * current validation as soon as the first constraint violation occurs.
	 * @param failFast whether to enable fail fast mode
	 * @since 0.13.0
	 */
	default Validatable<T> failFast(boolean failFast) {
		throw new UnsupportedOperationException("failFast is not implemented!");
	}

	/**
	 * Returns whether it is failFast.
	 * @return whether it is failFast
	 * @since 0.13.0
	 */
	default boolean isFailFast() {
		return false;
	}

}
