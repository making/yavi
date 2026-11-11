/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.Year;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.11.0
 */
public class YearValidator<T> extends DefaultArguments1Validator<Year, T> {

	@Override
	public <T2> YearValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new YearValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public YearValidator(Validator<Arguments1<Year>> validator, Function1<? super Year, ? extends T> mapper) {
		super(validator, mapper);
	}

}
