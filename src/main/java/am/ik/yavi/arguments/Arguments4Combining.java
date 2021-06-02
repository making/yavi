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
import am.ik.yavi.fn.Function4;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments4Combining<A1, A2, A3, A4, X1, X2, X3, X4> {
	protected final Arguments1Validator<A1, X1> v1;

	protected final Arguments1Validator<A2, X2> v2;

	protected final Arguments1Validator<A3, X3> v3;

	protected final Arguments1Validator<A4, X4> v4;

	public Arguments4Combining(Arguments1Validator<A1, X1> v1,
			Arguments1Validator<A2, X2> v2, Arguments1Validator<A3, X3> v3,
			Arguments1Validator<A4, X4> v4) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
	}

	public <X> Arguments4Validator<A1, A2, A3, A4, X> apply(
			Function4<X1, X2, X3, X4, X> f) {
		return (a1, a2, a3, a4, locale,
				constraintGroup) -> Validated.of(Validations.apply(f,
						this.v1.validate(a1, locale, constraintGroup),
						this.v2.validate(a2, locale, constraintGroup),
						this.v3.validate(a3, locale, constraintGroup),
						this.v4.validate(a4, locale, constraintGroup)));
	}

	public <A5, X5> Arguments5Combining<A1, A2, A3, A4, A5, X1, X2, X3, X4, X5> combine(
			Arguments1Validator<A5, X5> v5) {
		return new Arguments5Combining<>(v1, v2, v3, v4, v5);
	}
}
