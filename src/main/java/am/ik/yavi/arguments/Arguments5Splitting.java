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

import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.fn.Function5;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments5Splitting<A1, A2, A3, A4, A5, R1, R2, R3, R4, R5> {
	protected final ValueValidator<? super A1, ? extends R1> v1;

	protected final ValueValidator<? super A2, ? extends R2> v2;

	protected final ValueValidator<? super A3, ? extends R3> v3;

	protected final ValueValidator<? super A4, ? extends R4> v4;

	protected final ValueValidator<? super A5, ? extends R5> v5;

	public Arguments5Splitting(ValueValidator<? super A1, ? extends R1> v1,
			ValueValidator<? super A2, ? extends R2> v2,
			ValueValidator<? super A3, ? extends R3> v3,
			ValueValidator<? super A4, ? extends R4> v4,
			ValueValidator<? super A5, ? extends R5> v5) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
	}

	public <X> Arguments5Validator<A1, A2, A3, A4, A5, X> apply(
			Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends X> f) {
		return (a1, a2, a3, a4, a5, locale, constraintContext) -> Validations.apply(
				f::apply, this.v1.validate(a1, locale, constraintContext),
				this.v2.validate(a2, locale, constraintContext),
				this.v3.validate(a3, locale, constraintContext),
				this.v4.validate(a4, locale, constraintContext),
				this.v5.validate(a5, locale, constraintContext));
	}

	public <A6, R6> Arguments6Splitting<A1, A2, A3, A4, A5, A6, R1, R2, R3, R4, R5, R6> split(
			ValueValidator<? super A6, ? extends R6> v6) {
		return new Arguments6Splitting<>(v1, v2, v3, v4, v5, v6);
	}
}
