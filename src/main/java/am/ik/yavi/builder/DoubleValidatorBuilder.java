/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.DoubleValidator;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class DoubleValidatorBuilder implements ValueValidatorBuilder<Double, Double> {

	private final Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder;

	public static DoubleValidatorBuilder of(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static DoubleValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder) {
		return new DoubleValidatorBuilder(builder);
	}

	DoubleValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> DoubleValidator<T> build(Function<? super Double, ? extends T> mapper) {
		final Validator<Arguments1<Double>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new DoubleValidator<>(validator, mapper::apply);
	}

	@Override
	public DoubleValidator<Double> build() {
		return build(x -> x);
	}

}
