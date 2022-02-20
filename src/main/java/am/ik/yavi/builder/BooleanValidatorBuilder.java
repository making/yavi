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

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.BooleanValidator;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class BooleanValidatorBuilder {
	private final String name;

	private final Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints;

	public static BooleanValidatorBuilder of(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return new BooleanValidatorBuilder(name, constraints);
	}

	BooleanValidatorBuilder(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> BooleanValidator<T> build(Function<? super Boolean, ? extends T> mapper) {
		final Validator<Arguments1<Boolean>> validator = ValidatorBuilder
				.<Arguments1<Boolean>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new BooleanValidator<>(validator, mapper::apply);
	}

	public BooleanValidator<Boolean> build() {
		return build(x -> x);
	}
}
