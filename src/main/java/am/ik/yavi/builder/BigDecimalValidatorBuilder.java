/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
public class BigDecimalValidatorBuilder {
	private final String name;

	private final Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints;

	public static BigDecimalValidatorBuilder of(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		return new BigDecimalValidatorBuilder(name, constraints);
	}

	BigDecimalValidatorBuilder(String name,
			Function<BigDecimalConstraint<Arguments1<BigDecimal>>, BigDecimalConstraint<Arguments1<BigDecimal>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> BigDecimalValidator<T> build(
			Function<? super BigDecimal, ? extends T> mapper) {
		final Validator<Arguments1<BigDecimal>> validator = ValidatorBuilder
				.<Arguments1<BigDecimal>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new BigDecimalValidator<>(validator, mapper::apply);
	}

	public BigDecimalValidator<BigDecimal> build() {
		return build(x -> x);
	}
}
