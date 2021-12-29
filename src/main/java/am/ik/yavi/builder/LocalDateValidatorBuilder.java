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

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalDateValidator;
import am.ik.yavi.constraint.time.LocalDateConstraint;
import am.ik.yavi.core.Validator;

import java.time.LocalDate;
import java.util.function.Function;

public class LocalDateValidatorBuilder {
	private final String name;

	private final Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints;

	public static LocalDateValidatorBuilder of(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return new LocalDateValidatorBuilder(name, constraints);
	}

	LocalDateValidatorBuilder(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> LocalDateValidator<T> build(
			Function<? super LocalDate, ? extends T> mapper) {
		final Validator<Arguments1<LocalDate>> validator = ValidatorBuilder
				.<Arguments1<LocalDate>> of()
				.constraint(Arguments1::arg1, name, constraints).build();
		return new LocalDateValidator<>(validator, mapper::apply);
	}

	public LocalDateValidator<LocalDate> build() {
		return build(x -> x);
	}
}
