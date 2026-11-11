/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.YearMonth;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.11.0
 */
public class YearMonthValidator<T> extends DefaultArguments1Validator<YearMonth, T> {

	@Override
	public <T2> YearMonthValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new YearMonthValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public YearMonthValidator(Validator<Arguments1<YearMonth>> validator,
			Function1<? super YearMonth, ? extends T> mapper) {
		super(validator, mapper);
	}

}
