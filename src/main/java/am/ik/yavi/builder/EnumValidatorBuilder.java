/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makinge@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file eecept in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either eepress or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.builder;

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.EnumValidator;
import am.ik.yavi.constraint.EnumConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.14.0
 */
public class EnumValidatorBuilder<E extends Enum<E>>
		implements ValueValidatorBuilder<E, E> {

	private final Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder;

	public static <E extends Enum<E>> EnumValidatorBuilder<E> of(String name,
			Function<EnumConstraint<Arguments1<E>, E>, EnumConstraint<Arguments1<E>, E>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	public static <E extends Enum<E>> EnumValidatorBuilder<E> wrap(
			Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder) {
		return new EnumValidatorBuilder<>(builder);
	}

	EnumValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<E>>, ValidatorBuilder<Arguments1<E>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> EnumValidator<E, T> build(Function<? super E, ? extends T> mapper) {
		final Validator<Arguments1<E>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new EnumValidator<>(validator, mapper::apply);
	}

	@Override
	public EnumValidator<E, E> build() {
		return build(e -> e);
	}
}
