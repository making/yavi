/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.8.0
 */
public class ObjectValidator<X, T> extends DefaultArguments1Validator<X, T> {

	@Override
	public <T2> ObjectValidator<X, T2> map(Function<? super T, ? extends T2> mapper) {
		return new ObjectValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public ObjectValidator(Validator<Arguments1<X>> validator, Function1<? super X, ? extends T> mapper) {
		super(validator, mapper);
	}

}
