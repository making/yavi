/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.ShortValidator;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class ShortValidatorBuilder implements ValueValidatorBuilder<Short, Short> {

	private final Function<ValidatorBuilder<Arguments1<Short>>, ValidatorBuilder<Arguments1<Short>>> builder;

	public static ShortValidatorBuilder of(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static ShortValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Short>>, ValidatorBuilder<Arguments1<Short>>> builder) {
		return new ShortValidatorBuilder(builder);
	}

	ShortValidatorBuilder(Function<ValidatorBuilder<Arguments1<Short>>, ValidatorBuilder<Arguments1<Short>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> ShortValidator<T> build(Function<? super Short, ? extends T> mapper) {
		final Validator<Arguments1<Short>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new ShortValidator<>(validator, mapper::apply);
	}

	@Override
	public ShortValidator<Short> build() {
		return build(x -> x);
	}

}
