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
import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function12;
import am.ik.yavi.fn.Validations;
import am.ik.yavi.jsr305.Nullable;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments12Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> {

	protected final ValueValidator<? super A1, ? extends R1> v1;

	protected final ValueValidator<? super A2, ? extends R2> v2;

	protected final ValueValidator<? super A3, ? extends R3> v3;

	protected final ValueValidator<? super A4, ? extends R4> v4;

	protected final ValueValidator<? super A5, ? extends R5> v5;

	protected final ValueValidator<? super A6, ? extends R6> v6;

	protected final ValueValidator<? super A7, ? extends R7> v7;

	protected final ValueValidator<? super A8, ? extends R8> v8;

	protected final ValueValidator<? super A9, ? extends R9> v9;

	protected final ValueValidator<? super A10, ? extends R10> v10;

	protected final ValueValidator<? super A11, ? extends R11> v11;

	protected final ValueValidator<? super A12, ? extends R12> v12;

	public Arguments12Splitting(ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2, ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4, ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6, ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8, ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10, ValueValidator<? super A11, ? extends R11> v11,
			ValueValidator<? super A12, ? extends R12> v12) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
		this.v7 = v7;
		this.v8 = v8;
		this.v9 = v9;
		this.v10 = v10;
		this.v11 = v11;
		this.v12 = v12;
	}

	public <X> Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X> apply(
			Function12<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? extends X> f) {
		return new Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, X>() {

			@Override
			public Arguments12Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, Supplier<X>> lazy() {
				return ((a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, locale,
						constraintContext) -> Validations.apply(
								(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11,
										r12) -> () -> f.apply(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12),
								v1.validate(a1, locale, constraintContext), v2.validate(a2, locale, constraintContext),
								v3.validate(a3, locale, constraintContext), v4.validate(a4, locale, constraintContext),
								v5.validate(a5, locale, constraintContext), v6.validate(a6, locale, constraintContext),
								v7.validate(a7, locale, constraintContext), v8.validate(a8, locale, constraintContext),
								v9.validate(a9, locale, constraintContext),
								v10.validate(a10, locale, constraintContext),
								v11.validate(a11, locale, constraintContext),
								v12.validate(a12, locale, constraintContext)));
			}

			@Override
			public Validated<X> validate(@Nullable A1 a1, @Nullable A2 a2, @Nullable A3 a3, @Nullable A4 a4,
					@Nullable A5 a5, @Nullable A6 a6, @Nullable A7 a7, @Nullable A8 a8, @Nullable A9 a9,
					@Nullable A10 a10, @Nullable A11 a11, @Nullable A12 a12, Locale locale,
					ConstraintContext constraintContext) {
				return Validations.apply(f::apply, v1.validate(a1, locale, constraintContext),
						v2.validate(a2, locale, constraintContext), v3.validate(a3, locale, constraintContext),
						v4.validate(a4, locale, constraintContext), v5.validate(a5, locale, constraintContext),
						v6.validate(a6, locale, constraintContext), v7.validate(a7, locale, constraintContext),
						v8.validate(a8, locale, constraintContext), v9.validate(a9, locale, constraintContext),
						v10.validate(a10, locale, constraintContext), v11.validate(a11, locale, constraintContext),
						v12.validate(a12, locale, constraintContext));
			}
		};
	}

	public <A13, R13> Arguments13Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13> split(
			ValueValidator<? super A13, ? extends R13> v13) {
		return new Arguments13Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13);
	}

}
