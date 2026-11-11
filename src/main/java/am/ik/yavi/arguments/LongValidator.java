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
public class LongValidator<T> extends DefaultArguments1Validator<Long, T> {

	@Override
	public <T2> LongValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new LongValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public LongValidator(Validator<Arguments1<Long>> validator, Function1<? super Long, ? extends T> mapper) {
		super(validator, mapper);
	}

}
