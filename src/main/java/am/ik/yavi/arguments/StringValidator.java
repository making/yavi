/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.7.0
 */
public class StringValidator<T> extends DefaultArguments1Validator<String, T> {

	@Override
	public <T2> StringValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new StringValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public StringValidator(Validator<Arguments1<String>> validator, Function1<? super String, ? extends T> mapper) {
		super(validator, mapper);
	}

}
