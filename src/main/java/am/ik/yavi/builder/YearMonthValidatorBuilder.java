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

import java.time.YearMonth;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.YearMonthValidator;
import am.ik.yavi.constraint.YearMonthConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.11.0
 */
public class YearMonthValidatorBuilder {

	private final Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder;

	public static YearMonthValidatorBuilder of(String name,
			Function<YearMonthConstraint<Arguments1<YearMonth>>, YearMonthConstraint<Arguments1<YearMonth>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static YearMonthValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder) {
		return new YearMonthValidatorBuilder(builder);
	}

	YearMonthValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<YearMonth>>, ValidatorBuilder<Arguments1<YearMonth>>> builder) {
		this.builder = builder;
	}

	public <T> YearMonthValidator<T> build(
			Function<? super YearMonth, ? extends T> mapper) {
		final Validator<Arguments1<YearMonth>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new YearMonthValidator<>(validator, mapper::apply);
	}

	public YearMonthValidator<YearMonth> build() {
		return build(x -> x);
	}
}
