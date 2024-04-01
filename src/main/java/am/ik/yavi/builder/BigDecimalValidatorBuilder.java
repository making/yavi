/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.builder;

import java.math.BigDecimal;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.BigDecimalValidator;
import am.ik.yavi.constraint.BigDecimalConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class BigDecimalValidatorBuilder
		implements ValueValidatorBuilder<BigDecimal, BigDecimal> {

	private final Function<ValidatorBuilder<Arguments1<BigDecimal>>, ValidatorBuilder<Arguments1<BigDecimal>>> builder;

	public static BigDecimalValidatorBuilder of(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static BigDecimalValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<BigDecimal>>, ValidatorBuilder<Arguments1<BigDecimal>>> builder) {
		return new BigDecimalValidatorBuilder(builder);
	}

	BigDecimalValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<BigDecimal>>, ValidatorBuilder<Arguments1<BigDecimal>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> BigDecimalValidator<T> build(
			Function<? super BigDecimal, ? extends T> mapper) {
		final Validator<Arguments1<BigDecimal>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new BigDecimalValidator<>(validator, mapper::apply);
	}

	@Override
	public BigDecimalValidator<BigDecimal> build() {
		return build(x -> x);
	}
}
