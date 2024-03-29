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
package am.ik.yavi.arguments;

import java.util.Locale;
import java.util.function.Supplier;

import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function4;
import am.ik.yavi.fn.Validations;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments4Splitting<A1, A2, A3, A4, R1, R2, R3, R4> {
	protected final ValueValidator<? super A1, ? extends R1> v1;

	protected final ValueValidator<? super A2, ? extends R2> v2;

	protected final ValueValidator<? super A3, ? extends R3> v3;

	protected final ValueValidator<? super A4, ? extends R4> v4;

	public Arguments4Splitting(ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
	}

	public <X> Validator4<A1, A2, A3, A4, X> apply(
			Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends X> f) {
		return new Validator4<A1, A2, A3, A4, X>() {

			@Override
			public Validator4<A1, A2, A3, A4, Supplier<X>> lazy() {
				return ((a1, a2, a3, a4, locale, constraintContext) -> Validations.apply(
						(r1, r2, r3, r4) -> () -> f.apply(r1, r2, r3, r4),
						v1.validate(a1, locale, constraintContext),
						v2.validate(a2, locale, constraintContext),
						v3.validate(a3, locale, constraintContext),
						v4.validate(a4, locale, constraintContext)));
			}

			@Override
			public Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2,
					@Nullable A3 a3, @Nullable A4 a4, Locale locale,
					ConstraintContext constraintContext) {
				return Validations.apply(f::apply,
						v1.validate(a1, locale, constraintContext),
						v2.validate(a2, locale, constraintContext),
						v3.validate(a3, locale, constraintContext),
						v4.validate(a4, locale, constraintContext));
			}
		};
	}

	public <A5, R5> Arguments5Splitting<A1, A2, A3, A4, A5, R1, R2, R3, R4, R5> split(
			ValueValidator<? super A5, ? extends R5> v5) {
		return new Arguments5Splitting<>(v1, v2, v3, v4, v5);
	}
}
