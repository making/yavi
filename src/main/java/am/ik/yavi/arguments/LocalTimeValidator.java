/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

import java.time.LocalTime;
import java.util.function.Function;

/**
 * @since 0.10.0
 */
public class LocalTimeValidator<T> extends DefaultArguments1Validator<LocalTime, T> {

	@Override
	public <T2> LocalTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new LocalTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public LocalTimeValidator(Validator<Arguments1<LocalTime>> validator,
			Function1<? super LocalTime, ? extends T> mapper) {
		super(validator, mapper);
	}

}
