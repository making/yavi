/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.time.LocalDate;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalDateValidator;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class LocalDateValidatorBuilder implements ValueValidatorBuilder<LocalDate, LocalDate> {

	private final Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder;

	public static LocalDateValidatorBuilder of(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static LocalDateValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder) {
		return new LocalDateValidatorBuilder(builder);
	}

	LocalDateValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> LocalDateValidator<T> build(Function<? super LocalDate, ? extends T> mapper) {
		final Validator<Arguments1<LocalDate>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalDateValidator<>(validator, mapper::apply);
	}

	@Override
	public LocalDateValidator<LocalDate> build() {
		return build(x -> x);
	}

}
