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

import am.ik.yavi.fn.Function2;
import am.ik.yavi.fn.Validations;

/**
 * Generated by https://github.com/making/yavi/blob/develop/scripts/generate-args.sh
 *
 * @since 0.7.0
 */
public class Arguments2Combining<A, R1, R2> {
	protected final Arguments1Validator<? super A, ? extends R1> v1;

	protected final Arguments1Validator<? super A, ? extends R2> v2;

	public Arguments2Combining(Arguments1Validator<? super A, ? extends R1> v1,
			Arguments1Validator<? super A, ? extends R2> v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public <X> Arguments1Validator<A, X> apply(
			Function2<? super R1, ? super R2, ? extends X> f) {
		return (a, locale, constraintGroup) -> Validations.apply(f::apply,
				this.v1.validate(a, locale, constraintGroup),
				this.v2.validate(a, locale, constraintGroup));
	}

	public <R3> Arguments3Combining<A, R1, R2, R3> combine(
			Arguments1Validator<? super A, ? extends R3> v3) {
		return new Arguments3Combining<>(v1, v2, v3);
	}
}
