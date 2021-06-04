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

import java.util.function.Function;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class StringValidatorBuilder {
	private final String name;

	private final Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints;

	public static StringValidatorBuilder of(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		return new StringValidatorBuilder(name, constraints);
	}

	StringValidatorBuilder(String name,
			Function<CharSequenceConstraint<Arguments1<String>, String>, CharSequenceConstraint<Arguments1<String>, String>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> StringValidator<T> build(Function<? super String, ? extends T> mapper) {
		final Validator<Arguments1<String>> validator = ValidatorBuilder
				.<Arguments1<String>> of().constraint(Arguments1::arg1, name, constraints)
				.build();
		return new StringValidator<>(validator, mapper::apply);
	}

	public StringValidator<String> build() {
		return build(x -> x);
	}
}
