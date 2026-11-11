/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;

/**
 * A {@link Validatable} that applies another validator to a value nested in the target
 * object.
 *
 * <p>
 * The nested value is extracted with the given function and validated with the given
 * validator. If the extracted value is {@code null}, validation succeeds without applying
 * the nested validator. Violation names are prefixed with the given prefix.
 *
 * <p>
 * Instances are created by {@link am.ik.yavi.builder.ValidatorBuilder} when a conditional
 * validator is nested in another validator.
 *
 * @param <T> the type of the target object
 * @param <N> the type of the nested value
 * @since 0.10.0
 */
public class NestedValidator<T, N> implements Validatable<T> {

	private final Function<T, N> nested;

	private final Validatable<N> validator;

	private final String prefix;

	/**
	 * Creates a validator for a nested value.
	 *
	 * <p>
	 * If the given validator is a {@link Validator}, the prefix is applied to it with
	 * {@link Validator#prefixed(String)}. If it is a {@code NestedValidator}, the given
	 * prefix replaces its prefix.
	 * @param nested function that extracts the nested value from the target
	 * @param validator validator applied to the nested value
	 * @param prefix prefix for the names of the reported violations
	 */
	public NestedValidator(Function<T, N> nested, Validatable<N> validator, String prefix) {
		this.nested = nested;
		this.prefix = prefix;
		this.validator = prefixedValidatorIfNeeded(validator, prefix);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Validatable<N> prefixedValidatorIfNeeded(Validatable<N> validator, String prefix) {
		if (validator instanceof NestedValidator) {
			final NestedValidator<?, N> nestedValidator = (NestedValidator<?, N>) validator;
			return new NestedValidator(nestedValidator.nested, nestedValidator.validator, prefix);
		}
		return (validator instanceof Validator) ? ((Validator<N>) validator).prefixed(prefix) : validator;
	}

	/**
	 * Validates the nested value extracted from the target. If the nested value is
	 * {@code null}, no violations are returned.
	 * @param target the target object
	 * @param locale the locale for message formatting
	 * @param constraintContext constraint context used by conditional constraints
	 * @return constraint violations of the nested value
	 */
	@Override
	public ConstraintViolations validate(T target, Locale locale, ConstraintContext constraintContext) {
		final N n = this.nested.apply(target);
		if (n != null) {
			return this.validator.validate(n, locale, constraintContext);
		}
		else {
			return new ConstraintViolations();
		}
	}

	/**
	 * Returns the prefix applied to violation names.
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Returns a new nested validator whose nested validator is in the specified fail-fast
	 * mode.
	 * @param failFast whether to enable fail fast mode
	 * @return a new nested validator with the specified fail-fast mode
	 * @since 0.13.1
	 */
	@Override
	public Validatable<T> failFast(boolean failFast) {
		final Validatable<N> validatable = this.validator.failFast(failFast);
		return new NestedValidator<>(this.nested, validatable, prefix);
	}

	/**
	 * Returns whether the nested validator is in fail-fast mode.
	 * @return whether the nested validator is in fail-fast mode
	 * @since 0.13.1
	 */
	@Override
	public boolean isFailFast() {
		return this.validator.isFailFast();
	}

}
