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
public class FloatValidator<T> extends DefaultArguments1Validator<Float, T> {

	@Override
	public <T2> FloatValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new FloatValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public FloatValidator(Validator<Arguments1<Float>> validator, Function1<? super Float, ? extends T> mapper) {
		super(validator, mapper);
	}

}
