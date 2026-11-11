/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class IntegerValidatorBuilder implements ValueValidatorBuilder<Integer, Integer> {

	private final Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder;

	public static IntegerValidatorBuilder of(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static IntegerValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
		return new IntegerValidatorBuilder(builder);
	}

	IntegerValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> IntegerValidator<T> build(Function<? super Integer, ? extends T> mapper) {
		final Validator<Arguments1<Integer>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new IntegerValidator<>(validator, mapper::apply);
	}

	@Override
	public IntegerValidator<Integer> build() {
		return build(x -> x);
	}

}
