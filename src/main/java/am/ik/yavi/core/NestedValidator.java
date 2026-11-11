/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Locale;
import java.util.function.Function;

public class NestedValidator<T, N> implements Validatable<T> {

	private final Function<T, N> nested;

	private final Validatable<N> validator;

	private final String prefix;

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

	public String getPrefix() {
		return prefix;
	}

	/**
	 * @since 0.13.1
	 */
	@Override
	public Validatable<T> failFast(boolean failFast) {
		final Validatable<N> validatable = this.validator.failFast(failFast);
		return new NestedValidator<>(this.nested, validatable, prefix);
	}

	/**
	 * @since 0.13.1
	 */
	@Override
	public boolean isFailFast() {
		return this.validator.isFailFast();
	}

}
