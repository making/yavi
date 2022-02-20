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

import java.time.OffsetDateTime;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.OffsetDateTimeValidator;
import am.ik.yavi.constraint.OffsetDateTimeConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class OffsetDateTimeValidatorBuilder {
	private final String name;

	private final Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints;

	public static OffsetDateTimeValidatorBuilder of(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		return new OffsetDateTimeValidatorBuilder(name, constraints);
	}

	OffsetDateTimeValidatorBuilder(String name,
			Function<OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>, OffsetDateTimeConstraint<Arguments1<OffsetDateTime>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> OffsetDateTimeValidator<T> build(
			Function<? super OffsetDateTime, ? extends T> mapper) {
		final Validator<Arguments1<OffsetDateTime>> validator = ValidatorBuilder
				.<Arguments1<OffsetDateTime>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new OffsetDateTimeValidator<>(validator, mapper::apply);
	}

	public OffsetDateTimeValidator<OffsetDateTime> build() {
		return build(x -> x);
	}
}
