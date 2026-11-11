/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

import java.time.ZonedDateTime;
import java.util.function.Function;

/**
 * @since 0.10.0
 */
public class ZonedDateTimeValidator<T> extends DefaultArguments1Validator<ZonedDateTime, T> {

	@Override
	public <T2> ZonedDateTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new ZonedDateTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public ZonedDateTimeValidator(Validator<Arguments1<ZonedDateTime>> validator,
			Function1<? super ZonedDateTime, ? extends T> mapper) {
		super(validator, mapper);
	}

}
