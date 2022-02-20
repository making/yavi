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
import am.ik.yavi.arguments.FloatValidator;
import am.ik.yavi.constraint.FloatConstraint;
import am.ik.yavi.core.Validator;

/**
 * @since 0.7.0
 */
public class FloatValidatorBuilder {
	private final String name;

	private final Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints;

	public static FloatValidatorBuilder of(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		return new FloatValidatorBuilder(name, constraints);
	}

	FloatValidatorBuilder(String name,
			Function<FloatConstraint<Arguments1<Float>>, FloatConstraint<Arguments1<Float>>> constraints) {
		this.name = name;
		this.constraints = constraints;
	}

	public <T> FloatValidator<T> build(Function<? super Float, ? extends T> mapper) {
		final Validator<Arguments1<Float>> validator = ValidatorBuilder
				.<Arguments1<Float>> of().constraint(Arguments1::arg1, name, constraints)
				.build();
		return new FloatValidator<>(validator, mapper::apply);
	}

	public FloatValidator<Float> build() {
		return build(x -> x);
	}
}
