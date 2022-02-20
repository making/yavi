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

import java.time.ZonedDateTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.ZonedDateTimeValidator;
import am.ik.yavi.constraint.ZonedDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class ZonedDateTimeValidatorBuilder {
	private final String name;

	private final Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints;

	public static ZonedDateTimeValidatorBuilder of(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return new ZonedDateTimeValidatorBuilder(name, constraints);
	}

	ZonedDateTimeValidatorBuilder(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> ZonedDateTimeValidator<T> build(
			Function<? super ZonedDateTime, ? extends T> mapper) {
		final Validator<Arguments1<ZonedDateTime>> validator = ValidatorBuilder
				.<Arguments1<ZonedDateTime>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new ZonedDateTimeValidator<>(validator, mapper::apply);
	}

	public ZonedDateTimeValidator<ZonedDateTime> build() {
		return build(x -> x);
	}
}
