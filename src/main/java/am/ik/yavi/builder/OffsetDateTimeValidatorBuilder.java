/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.OffsetDateTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.OffsetDateTimeValidator;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class OffsetDateTimeValidatorBuilder implements ValueValidatorBuilder<OffsetDateTime, OffsetDateTime> {

	private final Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder;

	public static OffsetDateTimeValidatorBuilder of(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static OffsetDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder) {
		return new OffsetDateTimeValidatorBuilder(builder);
	}

	OffsetDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<OffsetDateTime>>, ValidatorBuilder<Arguments1<OffsetDateTime>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> OffsetDateTimeValidator<T> build(Function<? super OffsetDateTime, ? extends T> mapper) {
		final Validator<Arguments1<OffsetDateTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new OffsetDateTimeValidator<>(validator, mapper::apply);
	}

	@Override
	public OffsetDateTimeValidator<OffsetDateTime> build() {
		return build(x -> x);
	}

}
