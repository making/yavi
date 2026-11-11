/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * Validates a single {@link Boolean} value and maps it to a result.
 *
 * <p>
 * Validators of this type are usually created with
 * {@link am.ik.yavi.builder.BooleanValidatorBuilder}, which applies a
 * {@link am.ik.yavi.constraint.BooleanConstraint} under a given name. Violations carry
 * that name and the message key of the violated constraint, for example
 * {@code boolean.isTrue}. A {@code null} value is accepted unless the configured
 * constraints require otherwise (for example {@code notNull()}).
 *
 * <p>
 * The mapper passed to the constructor or to the builder's {@code build(Function)} method
 * is applied only when validation succeeds.
 *
 * @param <T> the type of the value produced from the validated input
 * @since 0.7.0
 */
public class BooleanValidator<T> extends DefaultArguments1Validator<Boolean, T> {

	/**
	 * Returns a new validator that applies the given function to the result of this
	 * validator's mapper.
	 *
	 * <p>
	 * The function is applied only when validation succeeds.
	 * @param <T2> the type of the value produced after mapping
	 * @param mapper the function applied to the validated value
	 * @return a new validator with the mapped result type
	 */
	@Override
	public <T2> BooleanValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new BooleanValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	/**
	 * Creates a validator that applies the given {@link Validator} to {@code Arguments1}
	 * and maps the validated value with the given function.
	 * @param validator the validator applied to the value
	 * @param mapper the function applied to the validated value
	 */
	public BooleanValidator(Validator<Arguments1<Boolean>> validator, Function1<? super Boolean, ? extends T> mapper) {
		super(validator, mapper);
	}

}
