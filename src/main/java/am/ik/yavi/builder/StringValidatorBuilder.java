/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class StringValidatorBuilder implements ValueValidatorBuilder<String, String> {

	private final Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder;

	public static StringValidatorBuilder of(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static StringValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
		return new StringValidatorBuilder(builder);
	}

	StringValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> StringValidator<T> build(Function<? super String, ? extends T> mapper) {
		final Validator<Arguments1<String>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new StringValidator<>(validator, mapper::apply);
	}

	@Override
	public StringValidator<String> build() {
		return build(x -> x);
	}

}
