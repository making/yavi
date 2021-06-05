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
package am.ik.yavi;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ApplicativeValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;

import java.util.Objects;

public class PhoneNumber {
	private final String value;

	static final ApplicativeValidator<PhoneNumber> applicativeValidator = validator()
			.prefixed("phoneNumber").applicative();

	public PhoneNumber(String value) {
		this.value = value;
	}

	public static Validator<PhoneNumber> validator() {
		return ValidatorBuilder.<PhoneNumber> of()
				.constraint((PhoneNumber p) -> p.value, "value",
						c -> c.notBlank().greaterThanOrEqual(8).lessThanOrEqual(16))
				.build();
	}

	public String value() {
		return this.value;
	}

	public static Validated<PhoneNumber> of(String value) {
		return applicativeValidator.validate(new PhoneNumber(value));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PhoneNumber that = (PhoneNumber) o;
		return value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}
