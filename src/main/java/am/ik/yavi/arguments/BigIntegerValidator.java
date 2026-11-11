/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.math.BigInteger;
import java.util.function.Function;

import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;

/**
 * @since 0.7.0
 */
public class BigIntegerValidator<T> extends DefaultArguments1Validator<BigInteger, T> {

	@Override
	public <T2> BigIntegerValidator<T2> map(Function<? super T, ? extends T2> mapper) {
		return new BigIntegerValidator<>(super.validator, s -> mapper.apply(super.mapper.apply(s)));
	}

	public BigIntegerValidator(Validator<Arguments1<BigInteger>> validator,
			Function1<? super BigInteger, ? extends T> mapper) {
		super(validator, mapper);
	}

}
