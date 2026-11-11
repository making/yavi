/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.Instant;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.10.0
 */
public class InstantValidator<T> extends DefaultArguments1Validator<Instant, T> {

	@Override
	public <T2> InstantValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new InstantValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public InstantValidator(Validator<Arguments1<Instant>> validator, Function1<? super Instant, ? extends T> mapper) {
		super(validator, mapper);
	}

}
