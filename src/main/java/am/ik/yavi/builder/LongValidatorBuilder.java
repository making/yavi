/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LongValidator;
import am.ik.yavi.constraint.LongConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class LongValidatorBuilder implements ValueValidatorBuilder<Long, Long> {

	private final Function<ValidatorBuilder<Arguments1<Long>>, ValidatorBuilder<Arguments1<Long>>> builder;

	public static LongValidatorBuilder of(String name,
			Function<LongConstraint<Arguments1<Long>>, LongConstraint<Arguments1<Long>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static LongValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Long>>, ValidatorBuilder<Arguments1<Long>>> builder) {
		return new LongValidatorBuilder(builder);
	}

	LongValidatorBuilder(Function<ValidatorBuilder<Arguments1<Long>>, ValidatorBuilder<Arguments1<Long>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> LongValidator<T> build(Function<? super Long, ? extends T> mapper) {
		final Validator<Arguments1<Long>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LongValidator<>(validator, mapper::apply);
	}

	@Override
	public LongValidator<Long> build() {
		return build(x -> x);
	}

}
