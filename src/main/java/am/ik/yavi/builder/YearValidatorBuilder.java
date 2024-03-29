/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

import java.time.Year;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.YearValidator;
import am.ik.yavi.constraint.YearConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.11.0
 */
public class YearValidatorBuilder implements ValueValidatorBuilder<Year, Year> {

	private final Function<ValidatorBuilder<Arguments1<Year>>, ValidatorBuilder<Arguments1<Year>>> builder;

	public static YearValidatorBuilder of(String name,
			Function<YearConstraint<Arguments1<Year>>, YearConstraint<Arguments1<Year>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static YearValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Year>>, ValidatorBuilder<Arguments1<Year>>> builder) {
		return new YearValidatorBuilder(builder);
	}

	YearValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Year>>, ValidatorBuilder<Arguments1<Year>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> YearValidator<T> build(Function<? super Year, ? extends T> mapper) {
		final Validator<Arguments1<Year>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new YearValidator<>(validator, mapper::apply);
	}

	@Override
	public YearValidator<Year> build() {
		return build(x -> x);
	}
}
