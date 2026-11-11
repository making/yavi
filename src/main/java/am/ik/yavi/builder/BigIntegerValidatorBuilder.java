/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.math.BigInteger;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.BigIntegerValidator;
import am.ik.yavi.constraint.BigIntegerConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class BigIntegerValidatorBuilder implements ValueValidatorBuilder<BigInteger, BigInteger> {

	private final Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder;

	public static BigIntegerValidatorBuilder of(String name,
			Function<BigIntegerConstraint<Arguments1<BigInteger>>, BigIntegerConstraint<Arguments1<BigInteger>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static BigIntegerValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder) {
		return new BigIntegerValidatorBuilder(builder);
	}

	BigIntegerValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<BigInteger>>, ValidatorBuilder<Arguments1<BigInteger>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> BigIntegerValidator<T> build(Function<? super BigInteger, ? extends T> mapper) {
		final Validator<Arguments1<BigInteger>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new BigIntegerValidator<>(validator, mapper::apply);
	}

	@Override
	public BigIntegerValidator<BigInteger> build() {
		return build(x -> x);
	}

}
