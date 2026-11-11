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
public class IntegerValidator<T> extends DefaultArguments1Validator<Integer, T> {

	@Override
	public <T2> IntegerValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new IntegerValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public IntegerValidator(Validator<Arguments1<Integer>> validator, Function1<? super Integer, ? extends T> mapper) {
		super(validator, mapper);
	}

}
