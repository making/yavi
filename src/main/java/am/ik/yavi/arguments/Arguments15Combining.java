/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
import am.ik.yavi.fn.Function15;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments15Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15> {

	protected final ValueValidator<? super A, ? extends R1> v1;

	protected final ValueValidator<? super A, ? extends R2> v2;

	protected final ValueValidator<? super A, ? extends R3> v3;

	protected final ValueValidator<? super A, ? extends R4> v4;

	protected final ValueValidator<? super A, ? extends R5> v5;

	protected final ValueValidator<? super A, ? extends R6> v6;

	protected final ValueValidator<? super A, ? extends R7> v7;

	protected final ValueValidator<? super A, ? extends R8> v8;

	protected final ValueValidator<? super A, ? extends R9> v9;

	protected final ValueValidator<? super A, ? extends R10> v10;

	protected final ValueValidator<? super A, ? extends R11> v11;

	protected final ValueValidator<? super A, ? extends R12> v12;

	protected final ValueValidator<? super A, ? extends R13> v13;

	protected final ValueValidator<? super A, ? extends R14> v14;

	protected final ValueValidator<? super A, ? extends R15> v15;

	public Arguments15Combining(ValueValidator<? super A, ? extends R1> v1, ValueValidator<? super A, ? extends R2> v2,
			ValueValidator<? super A, ? extends R3> v3, ValueValidator<? super A, ? extends R4> v4,
			ValueValidator<? super A, ? extends R5> v5, ValueValidator<? super A, ? extends R6> v6,
			ValueValidator<? super A, ? extends R7> v7, ValueValidator<? super A, ? extends R8> v8,
			ValueValidator<? super A, ? extends R9> v9, ValueValidator<? super A, ? extends R10> v10,
			ValueValidator<? super A, ? extends R11> v11, ValueValidator<? super A, ? extends R12> v12,
			ValueValidator<? super A, ? extends R13> v13, ValueValidator<? super A, ? extends R14> v14,
			ValueValidator<? super A, ? extends R15> v15) {
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
		this.v13 = v13;
		this.v14 = v14;
		this.v15 = v15;
	}

	public <X> Arguments1Validator<A, X> apply(
			Function15<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? super R9, ? super R10, ? super R11, ? super R12, ? super R13, ? super R14, ? super R15, ? extends X> f) {
		return (a, locale, constraintContext) -> Validations.apply(f::apply,
				this.v1.validate(a, locale, constraintContext), this.v2.validate(a, locale, constraintContext),
				this.v3.validate(a, locale, constraintContext), this.v4.validate(a, locale, constraintContext),
				this.v5.validate(a, locale, constraintContext), this.v6.validate(a, locale, constraintContext),
				this.v7.validate(a, locale, constraintContext), this.v8.validate(a, locale, constraintContext),
				this.v9.validate(a, locale, constraintContext), this.v10.validate(a, locale, constraintContext),
				this.v11.validate(a, locale, constraintContext), this.v12.validate(a, locale, constraintContext),
				this.v13.validate(a, locale, constraintContext), this.v14.validate(a, locale, constraintContext),
				this.v15.validate(a, locale, constraintContext));
	}

	public <R16> Arguments16Combining<A, R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16> combine(
			ValueValidator<? super A, ? extends R16> v16) {
		return new Arguments16Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
	}

}
