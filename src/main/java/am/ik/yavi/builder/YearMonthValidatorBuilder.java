/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.YearMonth;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.YearMonthValidator;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.11.0
 */
public class YearMonthValidatorBuilder implements ValueValidatorBuilder<YearMonth, YearMonth> {

	private final Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder;

	public static YearMonthValidatorBuilder of(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static YearMonthValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder) {
		return new YearMonthValidatorBuilder(builder);
	}

	YearMonthValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> YearMonthValidator<T> build(Function<? super YearMonth, ? extends T> mapper) {
		final Validator<Arguments1<YearMonth>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new YearMonthValidator<>(validator, mapper::apply);
	}

	@Override
	public YearMonthValidator<YearMonth> build() {
		return build(x -> x);
	}

}
