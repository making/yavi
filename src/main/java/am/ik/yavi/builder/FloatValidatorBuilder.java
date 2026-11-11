/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.FloatValidator;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class FloatValidatorBuilder implements ValueValidatorBuilder<Float, Float> {

	private final Function<ValidatorBuilder<Arguments1<Float>>, ValidatorBuilder<Arguments1<Float>>> builder;

	public static FloatValidatorBuilder of(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static FloatValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Float>>, ValidatorBuilder<Arguments1<Float>>> builder) {
		return new FloatValidatorBuilder(builder);
	}

	FloatValidatorBuilder(Function<ValidatorBuilder<Arguments1<Float>>, ValidatorBuilder<Arguments1<Float>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> FloatValidator<T> build(Function<? super Float, ? extends T> mapper) {
		final Validator<Arguments1<Float>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new FloatValidator<>(validator, mapper::apply);
	}

	@Override
	public FloatValidator<Float> build() {
		return build(x -> x);
	}

}
