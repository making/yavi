/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

	private final Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder;

	public static ZonedDateTimeValidatorBuilder of(String name,
			Function<ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>, ZonedDateTimeConstraint<Arguments1<ZonedDateTime>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static ZonedDateTimeValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder) {
		return new ZonedDateTimeValidatorBuilder(builder);
	}

	ZonedDateTimeValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<ZonedDateTime>>, ValidatorBuilder<Arguments1<ZonedDateTime>>> builder) {
		this.builder = builder;
	}

	public <T> ZonedDateTimeValidator<T> build(
			Function<? super ZonedDateTime, ? extends T> mapper) {
		final Validator<Arguments1<ZonedDateTime>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new ZonedDateTimeValidator<>(validator, mapper::apply);
	}

	public ZonedDateTimeValidator<ZonedDateTime> build() {
		return build(x -> x);
	}
}
