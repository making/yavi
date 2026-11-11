/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.math.BigDecimal;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.7.0
 */
public class BigDecimalValidator<T> extends DefaultArguments1Validator<BigDecimal, T> {

	@Override
	public <T2> BigDecimalValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new BigDecimalValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public BigDecimalValidator(Validator<Arguments1<BigDecimal>> validator,
			Function1<? super BigDecimal, ? extends T> mapper) {
		super(validator, mapper);
	}

}
