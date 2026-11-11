/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

import java.time.LocalDate;
import java.util.function.Function;

/**
 * @since 0.10.0
 */
public class LocalDateValidator<T> extends DefaultArguments1Validator<LocalDate, T> {

	@Override
	public <T2> LocalDateValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new LocalDateValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public LocalDateValidator(Validator<Arguments1<LocalDate>> validator,
			Function1<? super LocalDate, ? extends T> mapper) {
		super(validator, mapper);
	}

}
