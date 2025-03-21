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

import java.time.LocalDate;
import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.LocalDateValidator;
import am.ik.yavi.constraint.LocalDateConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.10.0
 */
public class LocalDateValidatorBuilder implements ValueValidatorBuilder<LocalDate, LocalDate> {

	private final Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder;

	public static LocalDateValidatorBuilder of(String name,
			Function<LocalDateConstraint<Arguments1<LocalDate>>, LocalDateConstraint<Arguments1<LocalDate>>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static LocalDateValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder) {
		return new LocalDateValidatorBuilder(builder);
	}

	LocalDateValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<LocalDate>>, ValidatorBuilder<Arguments1<LocalDate>>> builder) {
		this.builder = builder;
	}

	@Override
	public <T> LocalDateValidator<T> build(Function<? super LocalDate, ? extends T> mapper) {
		final Validator<Arguments1<LocalDate>> validator = this.builder.apply(ValidatorBuilder.of()).build();
		return new LocalDateValidator<>(validator, mapper::apply);
	}

	@Override
	public LocalDateValidator<LocalDate> build() {
		return build(x -> x);
	}

}
