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
	private final String name;

	private final Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints;

	public static LocalDateTimeValidatorBuilder of(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		return new LocalDateTimeValidatorBuilder(name, constraints);
	}

	LocalDateTimeValidatorBuilder(String name,
			Function<LocalDateTimeConstraint<Arguments1<LocalDateTime>>, LocalDateTimeConstraint<Arguments1<LocalDateTime>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> LocalDateTimeValidator<T> build(
			Function<? super LocalDateTime, ? extends T> mapper) {
		final Validator<Arguments1<LocalDateTime>> validator = ValidatorBuilder
				.<Arguments1<LocalDateTime>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new LocalDateTimeValidator<>(validator, mapper::apply);
	}

	public LocalDateTimeValidator<LocalDateTime> build() {
		return build(x -> x);
	}
}
