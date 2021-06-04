/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
public class DoubleValidatorBuilder {
	private final String name;

	private final Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints;

	public static DoubleValidatorBuilder of(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		return new DoubleValidatorBuilder(name, constraints);
	}

	DoubleValidatorBuilder(String name,
			Function<DoubleConstraint<Arguments1<Double>>, DoubleConstraint<Arguments1<Double>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> DoubleValidator<T> build(Function<? super Double, ? extends T> mapper) {
		final Validator<Arguments1<Double>> validator = ValidatorBuilder
				.<Arguments1<Double>> of().constraint(Arguments1::arg1, name, constraints)
				.build();
		return new DoubleValidator<>(validator, mapper::apply);
	}

	public DoubleValidator<Double> build() {
		return build(x -> x);
	}
}
