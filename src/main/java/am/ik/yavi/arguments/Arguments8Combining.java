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
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validated;
import am.ik.yavi.fn.Function8;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments8Combining<A1, A2, A3, A4, A5, A6, A7, A8, X1, X2, X3, X4, X5, X6, X7, X8> {
	protected final Arguments1Validator<A1, X1> v1;

	protected final Arguments1Validator<A2, X2> v2;

	protected final Arguments1Validator<A3, X3> v3;

	protected final Arguments1Validator<A4, X4> v4;

	protected final Arguments1Validator<A5, X5> v5;

	protected final Arguments1Validator<A6, X6> v6;

	protected final Arguments1Validator<A7, X7> v7;

	protected final Arguments1Validator<A8, X8> v8;

	public Arguments8Combining(Arguments1Validator<A1, X1> v1,
			Arguments1Validator<A2, X2> v2, Arguments1Validator<A3, X3> v3,
			Arguments1Validator<A4, X4> v4, Arguments1Validator<A5, X5> v5,
			Arguments1Validator<A6, X6> v6, Arguments1Validator<A7, X7> v7,
			Arguments1Validator<A8, X8> v8) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
		this.v5 = v5;
		this.v6 = v6;
		this.v7 = v7;
		this.v8 = v8;
	}

	public <X> Arguments8Validator<A1, A2, A3, A4, A5, A6, A7, A8, X> apply(
			Function8<X1, X2, X3, X4, X5, X6, X7, X8, X> f) {
		return (a1, a2, a3, a4, a5, a6, a7, a8, locale,
				constraintGroup) -> Validated.of(Validations.apply(f,
						this.v1.validate(a1, locale, constraintGroup),
						this.v2.validate(a2, locale, constraintGroup),
						this.v3.validate(a3, locale, constraintGroup),
						this.v4.validate(a4, locale, constraintGroup),
						this.v5.validate(a5, locale, constraintGroup),
						this.v6.validate(a6, locale, constraintGroup),
						this.v7.validate(a7, locale, constraintGroup),
						this.v8.validate(a8, locale, constraintGroup)));
	}

	public <A9, X9> Arguments9Combining<A1, A2, A3, A4, A5, A6, A7, A8, A9, X1, X2, X3, X4, X5, X6, X7, X8, X9> combine(
			Arguments1Validator<A9, X9> v9) {
		return new Arguments9Combining<>(v1, v2, v3, v4, v5, v6, v7, v8, v9);
	}
}
