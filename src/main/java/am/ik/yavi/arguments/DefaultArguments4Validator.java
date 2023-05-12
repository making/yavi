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
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;
import am.ik.yavi.fn.Function4;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class DefaultArguments4Validator<A1, A2, A3, A4, X> implements Arguments4Validator<A1, A2, A3, A4, X> {

	protected final Validator<Arguments4<A1, A2, A3, A4>> validator;

	protected final Function4<? super A1, ? super A2, ? super A3, ? super A4, ? extends X> mapper;

	public DefaultArguments4Validator(Validator<Arguments4<A1, A2, A3, A4>> validator,
			Function4<? super A1, ? super A2, ? super A3, ? super A4, ? extends X> mapper) {
		this.validator = validator;
		this.mapper = mapper;
	}

	/**
	 * @since 0.10.0
	 */
	@Override
	public DefaultArguments4Validator<A1, A2, A3, A4, Supplier<X>> lazy() {
		return new DefaultArguments4Validator<>(this.validator,
				(a1, a2, a3, a4) -> () -> this.mapper.apply(a1, a2, a3, a4));
	}

	@Override
	public Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4, Locale locale,
			ConstraintContext constraintContext) {
		return this.validator.applicative()
			.validate(Arguments.of(a1, a2, a3, a4), locale, constraintContext)
			.map(values -> values.map(this.mapper));
	}

}
