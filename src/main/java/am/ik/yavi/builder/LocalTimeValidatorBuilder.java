/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.LocalTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalTimeValidator;
import am.ik.yavi.constraint.LocalTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.14.0
 */
public class LocalTimeValidatorBuilder implements ValueValidatorBuilder<LocalTime, LocalTime> {

	private final Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder;

	public static LocalTimeValidatorBuilder of(String name,
			Function<LocalTimeConstraint<Arguments1<LocalTime>>, LocalTimeConstraint<Arguments1<LocalTime>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static LocalTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder) {
		return new LocalTimeValidatorBuilder(builder);
	}

	LocalTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalTime>>, ValidatorBuilder<Arguments1<LocalTime>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> LocalTimeValidator<T> build(Function<? super LocalTime, ? extends T> mapper) {
		final Validator<Arguments1<LocalTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalTimeValidator<>(validator, mapper::apply);
	}

	@Override
	public LocalTimeValidator<LocalTime> build() {
		return build(x -> x);
	}

}
