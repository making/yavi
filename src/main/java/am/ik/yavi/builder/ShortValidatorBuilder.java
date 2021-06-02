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
import am.ik.yavi.arguments.ShortValidator;
import am.ik.yavi.constraint.ShortConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class ShortValidatorBuilder {
	private final String name;

	private final Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints;

	public static ShortValidatorBuilder of(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		return new ShortValidatorBuilder(name, constraints);
	}

	ShortValidatorBuilder(String name,
			Function<ShortConstraint<Arguments1<Short>>, ShortConstraint<Arguments1<Short>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> ShortValidator<T> build(Function<? super Short, ? extends T> mapper) {
		final Validator<Arguments1<Short>> validator = ValidatorBuilder
				.<Arguments1<Short>> of().constraint(Arguments1::arg1, name, constraints)
				.build();
		return new ShortValidator<>(validator, mapper::apply);
	}

	public ShortValidator<Short> build() {
		return build(x -> x);
	}
}
