/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.OffsetDateTime;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.10.0
 */
public class OffsetDateTimeValidator<T> extends DefaultArguments1Validator<OffsetDateTime, T> {

	@Override
	public <T2> OffsetDateTimeValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new OffsetDateTimeValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public OffsetDateTimeValidator(Validator<Arguments1<OffsetDateTime>> validator,
			Function1<? super OffsetDateTime, ? extends T> mapper) {
		super(validator, mapper);
	}

}
