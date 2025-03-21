/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
import am.ik.yavi.arguments.DoubleValidator;
import am.ik.yavi.constraint.DoubleConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class DoubleValidatorBuilder implements ValueValidatorBuilder<Double, Double> {

	private final Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder;

	public static DoubleValidatorBuilder of(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static DoubleValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder) {
		return new DoubleValidatorBuilder(builder);
	}

	DoubleValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Double>>, ValidatorBuilder<Arguments1<Double>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> DoubleValidator<T> build(Function<? super Double, ? extends T> mapper) {
		final Validator<Arguments1<Double>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new DoubleValidator<>(validator, mapper::apply);
	}

	@Override
	public DoubleValidator<Double> build() {
		return build(x -> x);
	}

}
