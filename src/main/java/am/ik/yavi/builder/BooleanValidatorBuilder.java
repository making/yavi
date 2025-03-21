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
import am.ik.yavi.arguments.BooleanValidator;
import am.ik.yavi.constraint.BooleanConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class BooleanValidatorBuilder implements ValueValidatorBuilder<Boolean, Boolean> {

	private final Function<ValidatorBuilder<Arguments1<Boolean>>, ValidatorBuilder<Arguments1<Boolean>>> builder;

	public static BooleanValidatorBuilder of(String name,
			Function<BooleanConstraint<Arguments1<Boolean>>, BooleanConstraint<Arguments1<Boolean>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static BooleanValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<Boolean>>, ValidatorBuilder<Arguments1<Boolean>>> builder) {
		return new BooleanValidatorBuilder(builder);
	}

	BooleanValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<Boolean>>, ValidatorBuilder<Arguments1<Boolean>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> BooleanValidator<T> build(Function<? super Boolean, ? extends T> mapper) {
		final Validator<Arguments1<Boolean>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new BooleanValidator<>(validator, mapper::apply);
	}

	@Override
	public BooleanValidator<Boolean> build() {
		return build(x -> x);
	}

}
