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

import java.time.LocalDateTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalDateTimeValidator;
import am.ik.yavi.constraint.LocalDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class LocalDateTimeValidatorBuilder {

	private final Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder;

	public static LocalDateTimeValidatorBuilder of(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static LocalDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder) {
		return new LocalDateTimeValidatorBuilder(builder);
	}

	LocalDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalDateTime>>, ValidatorBuilder<Arguments1<LocalDateTime>>> builder) {
		this.builder = builder;
	}

	public <T> LocalDateTimeValidator<T> build(Function<? super LocalDateTime, ? extends T> mapper) {
		final Validator<Arguments1<LocalDateTime>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalDateTimeValidator<>(validator, mapper::apply);
	}

	public LocalDateTimeValidator<LocalDateTime> build() {
		return build(x -> x);
	}

}
