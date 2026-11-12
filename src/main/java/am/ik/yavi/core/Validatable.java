/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.BiConsumer;

import am.ik.yavi.fn.Validation;

/**
 * Validates a target object and reports constraint violations.
 *
 * <p>
 * Implementations are normally obtained from {@link am.ik.yavi.builder.ValidatorBuilder};
 * {@link Validator} is the standard implementation. A {@code Validatable} can be applied
 * to an object directly, converted to an {@link ApplicativeValidator} or to a
 * {@link BiConsumer} for error handlers.
 *
 * @param <T> the type of the object to validate
 * @since 0.10.0
 * @see Validator
 * @see am.ik.yavi.builder.ValidatorBuilder
 */
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
	 * Validates all constraints on {@code target}.
	 *
	 * <p>
	 * {@link Locale#getDefault()} is used to locate the violation messages and
	 * {@link ConstraintGroup#DEFAULT} is used as a constraint context.
	 * @param target target to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target) {
		return this.validate(target, Locale.getDefault(), ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}.
	 *
	 * <p>
	 * {@link ConstraintGroup#DEFAULT} is used as a constraint context.
	 * @param target target to validate
	 * @param locale the locale targeted for the violation messages.
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target, Locale locale) {
		return this.validate(target, locale, ConstraintGroup.DEFAULT);
	}

	/**
	 * Validates all constraints on {@code target}.
	 *
	 * <p>
	 * {@link Locale#getDefault()} is used to locate the violation messages.
	 * @param target target to validate
	 * @param constraintContext constraint context to validate
	 * @return constraint violations
	 * @throws IllegalArgumentException if target is {@code null}
	 */
	default ConstraintViolations validate(T target, ConstraintContext constraintContext) {
		return this.validate(target, Locale.getDefault(), constraintContext);
	}

	/**
	 * Returns an {@link ApplicativeValidator} that applies the same constraints as this
	 * validator and wraps the result in a {@link Validated}.
	 * @return applicative validator
	 * @since 0.6.0
	 */
	default ApplicativeValidator<T> applicative() {
		return (target, locale, constraintContext) -> {
			if (target == null) {
				throw new IllegalArgumentException("target must not be null");
			}
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
	 * Converts the given value validator to a regular validator. The value produced by
	 * the value validator is discarded; only the constraint violations are reported.
	 * @param applicative applicative validator to convert
	 * @param <A1> the type of the input to validate
	 * @param <R> the type of the value produced by the value validator
	 * @return regular validator
	 * @since 0.11.0
	 */
	static <A1, R> Validatable<A1> from(ValueValidator<? super A1, ? extends R> applicative) {
		return (target, locale, constraintGroup) -> applicative.validate(target, locale, constraintGroup)
			.fold(ConstraintViolations::of, result -> new ConstraintViolations());
	}

	/**
	 * Converts the validator to a {@link BiConsumer} that validates the first argument
	 * with the default locale and constraint group and passes each violation to the given
	 * error handler. The consumer does nothing when validation succeeds.
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
	 * Returns a validator with fail-fast mode enabled or disabled. If enabled, the
	 * validator returns from the current validation as soon as the first constraint
	 * violation occurs.
	 *
	 * <p>
	 * The default implementation does not support this operation and throws
	 * {@link UnsupportedOperationException}; implementations that support fail-fast mode
	 * override it and return a new instance.
	 * @param failFast whether to enable fail fast mode
	 * @return a validator with the specified fail-fast mode
	 * @throws UnsupportedOperationException if the implementation does not support
	 * fail-fast mode
	 * @since 0.13.0
	 */
	default Validatable<T> failFast(boolean failFast) {
		throw new UnsupportedOperationException("failFast is not implemented!");
	}

	/**
	 * Returns whether this validator is in fail-fast mode. The default implementation
	 * returns {@code false}.
	 * @return whether it is failFast
	 * @since 0.13.0
	 */
	default boolean isFailFast() {
		return false;
	}

}
