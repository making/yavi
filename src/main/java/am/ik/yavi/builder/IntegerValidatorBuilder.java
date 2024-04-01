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

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.constraint.IntegerConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class IntegerValidatorBuilder implements ValueValidatorBuilder<Integer, Integer> {

	private final Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder;

	public static IntegerValidatorBuilder of(String name,
			Function<IntegerConstraint<Arguments1<Integer>>, IntegerConstraint<Arguments1<Integer>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static IntegerValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
		return new IntegerValidatorBuilder(builder);
	}

	IntegerValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Integer>>, ValidatorBuilder<Arguments1<Integer>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> IntegerValidator<T> build(Function<? super Integer, ? extends T> mapper) {
		final Validator<Arguments1<Integer>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new IntegerValidator<>(validator, mapper::apply);
	}

	@Override
	public IntegerValidator<Integer> build() {
		return build(x -> x);
	}
}
