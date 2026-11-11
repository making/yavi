/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.Instant;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.InstantValidator;
import am.ik.yavi.constraint.InstantConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class InstantValidatorBuilder implements ValueValidatorBuilder<Instant, Instant> {

	private final Function<ValidatorBuilder<Arguments1<Instant>>, ValidatorBuilder<Arguments1<Instant>>> builder;

	public static InstantValidatorBuilder of(String name,
			Function<InstantConstraint<Arguments1<Instant>>, InstantConstraint<Arguments1<Instant>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static InstantValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Instant>>, ValidatorBuilder<Arguments1<Instant>>> builder) {
		return new InstantValidatorBuilder(builder);
	}

	InstantValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Instant>>, ValidatorBuilder<Arguments1<Instant>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> InstantValidator<T> build(Function<? super Instant, ? extends T> mapper) {
		final Validator<Arguments1<Instant>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new InstantValidator<>(validator, mapper::apply);
	}

	@Override
	public InstantValidator<Instant> build() {
		return build(x -> x);
	}

}
