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
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function1;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class DefaultArguments1Validator<A1, X> implements Arguments1Validator<A1, X> {
  protected final Validator<Arguments1<A1>> validator;
	protected final Function1<? super A1, ? extends X> mapper;

	public DefaultArguments1Validator(Validator<Arguments1<A1>> validator, Function1<? super A1, ? extends X> mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * @since 0.10.0
	 */
	@Override
	public DefaultArguments1Validator<A1, Supplier<X>> lazy() {
		return new DefaultArguments1Validator<>(this.validator, (a1) -> () -> this.mapper.apply(a1));
	}

  @Override
	public Validated<X> validate(@Nullable A1 a1,
			Locale locale, ConstraintContext constraintContext) {
		return this.validator.applicative()
		    .validate(Arguments.of(a1), locale, constraintContext)
				.map(values -> values.map(this.mapper));
	}
}
