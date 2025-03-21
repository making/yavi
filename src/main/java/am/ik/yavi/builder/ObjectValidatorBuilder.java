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
import am.ik.yavi.arguments.ObjectValidator;
import am.ik.yavi.constraint.ObjectConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.8.0
 */
public class ObjectValidatorBuilder<X> implements ValueValidatorBuilder<X, X> {

	private final Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder;

	public static <X> ObjectValidatorBuilder<X> of(String name,
			Function<ObjectConstraint<Arguments1<X>, X>, ObjectConstraint<Arguments1<X>, X>> constraints) {
		return wrap(b -> b.constraintOnObject(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static <X> ObjectValidatorBuilder<X> wrap(
			Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder) {
		return new ObjectValidatorBuilder<>(builder);
	}

	ObjectValidatorBuilder(Function<ValidatorBuilder<Arguments1<X>>, ValidatorBuilder<Arguments1<X>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> ObjectValidator<X, T> build(Function<? super X, ? extends T> mapper) {
		final Validator<Arguments1<X>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new ObjectValidator<>(validator, mapper::apply);
	}

	@Override
	public ObjectValidator<X, X> build() {
		return build(x -> x);
	}

}
