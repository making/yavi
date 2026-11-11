/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * @since 0.10.0
 */
public class LocalDateTimeValidator<T> extends DefaultArguments1Validator<LocalDateTime, T> {

	@Override
	public <T2> LocalDateTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new LocalDateTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public LocalDateTimeValidator(Validator<Arguments1<LocalDateTime>> validator,
			Function1<? super LocalDateTime, ? extends T> mapper) {
		super(validator, mapper);
	}

}
