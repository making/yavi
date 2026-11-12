/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi;

import org.jspecify.annotations.Nullable;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ApplicativeValidator;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.Validator;

import java.util.Objects;

public class PhoneNumber {

	private final @Nullable String value;

	static final ApplicativeValidator<PhoneNumber> applicativeValidator = validator().prefixed("phoneNumber")
		.applicative();

	public PhoneNumber(@Nullable String value) {
		this.value = value;
	}

	public static Validator<PhoneNumber> validator() {
		return ValidatorBuilder.<PhoneNumber>of()
			.constraint((PhoneNumber p) -> p.value, "value",
					c -> c.notBlank().greaterThanOrEqual(8).lessThanOrEqual(16))
			.build();
	}

	public @Nullable String value() {
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
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public String toString() {
		return "PhoneNumber{" + "value='" + value + '\'' + '}';
	}

}
