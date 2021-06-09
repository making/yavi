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
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.8.0
 */
public class ObjectValidatorBuilder<X> {
	private final String name;

	private final Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints;

	public static <X> ObjectValidatorBuilder<X> of(String name,
			Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints) {
		return new ObjectValidatorBuilder<>(name, constraints);
	}

	ObjectValidatorBuilder(String name,
			Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> ObjectValidator<X, T> build(Function<? super X, ? extends T> mapper) {
		final Validator<Arguments1<X>> validator = ValidatorBuilder.<Arguments1<X>> of()
				.constraintOnObject(Arguments1::arg1, name, constraints).build();
		return new ObjectValidator<>(validator, mapper::apply);
	}

	public ObjectValidator<X, X> build() {
		return build(x -> x);
	}
}
