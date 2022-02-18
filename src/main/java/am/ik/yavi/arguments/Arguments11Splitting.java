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

import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function11;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments11Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11> {
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

	public Arguments11Splitting(ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5,
			ValueValidator<? super A6, ? extends R6> v6,
			ValueValidator<? super A7, ? extends R7> v7,
			ValueValidator<? super A8, ? extends R8> v8,
			ValueValidator<? super A9, ? extends R9> v9,
			ValueValidator<? super A10, ? extends R10> v10,
			ValueValidator<? super A11, ? extends R11> v11) {
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
	}

	public <X> Arguments11Validator<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, X> apply(
			Function11<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? extends X> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, locale,
				constraintContext) -> Validations.apply(f::apply,
						this.v1.validate(a1, locale, constraintContext),
						this.v2.validate(a2, locale, constraintContext),
						this.v3.validate(a3, locale, constraintContext),
						this.v4.validate(a4, locale, constraintContext),
						this.v5.validate(a5, locale, constraintContext),
						this.v6.validate(a6, locale, constraintContext),
						this.v7.validate(a7, locale, constraintContext),
						this.v8.validate(a8, locale, constraintContext),
						this.v9.validate(a9, locale, constraintContext),
						this.v10.validate(a10, locale, constraintContext),
						this.v11.validate(a11, locale, constraintContext));
	}

	public <A12, R12> Arguments12Splitting<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12> split(
			ValueValidator<? super A12, ? extends R12> v12) {
		return new Arguments12Splitting<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11,
				v12);
	}
}
