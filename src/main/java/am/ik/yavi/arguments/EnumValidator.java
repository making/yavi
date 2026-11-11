/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.14.0
 */
public class EnumValidator<E extends Enum<E>, T> extends DefaultArguments1Validator<E, T> {

	@Override
	public <T2> EnumValidator<E, T2> map(Function<? super T, ? extends T2> mapper) {
		return new EnumValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public EnumValidator(Validator<Arguments1<E>> validator, Function1<? super E, ? extends T> mapper) {
		super(validator, mapper);
	}

}
