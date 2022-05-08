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

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class StringValidatorBuilder {

	private final Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder;

	public static StringValidatorBuilder of(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return wrap(b -> b.constraint(Arguments1::arg1, name, constraints));
	}

	/**
	 * @since 0.11.3
	 */
	public static StringValidatorBuilder wrap(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
		return new StringValidatorBuilder(builder);
	}

	StringValidatorBuilder(
			Function<ValidatorBuilder<Arguments1<String>>, ValidatorBuilder<Arguments1<String>>> builder) {
		this.builder = builder;
	}

	public <T> StringValidator<T> build(Function<? super String, ? extends T> mapper) {
		final Validator<Arguments1<String>> validator = this.builder
				.apply(ValidatorBuilder.of()).build();
		return new StringValidator<>(validator, mapper::apply);
	}

	public StringValidator<String> build() {
		return build(x -> x);
	}
}
