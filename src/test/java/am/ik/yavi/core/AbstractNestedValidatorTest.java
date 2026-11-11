/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;

abstract class AbstractNestedValidatorTest {

	@Test
	void invalid() {
		Validator<Address> addressValidator = validator();
		Address address = new Address(new Country(null), null, new PhoneNumber(""));
		ConstraintViolations violations = addressValidator.validate(address);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(4);
		assertThat(violations.get(0).message()).isEqualTo("\"street\" must not be blank");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).message()).isEqualTo("\"country.name\" must not be blank");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).message()).isEqualTo("\"phoneNumber.value\" must not be blank");
		assertThat(violations.get(2).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(3).message())
			.isEqualTo("The size of \"phoneNumber.value\" must be greater than or equal to 8. The given size is 0");
		assertThat(violations.get(3).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void nestedValueIsNull() {
		Validator<Address> addressValidator = validator();
		Address address = new Address(null, null, null /* nullValidity */);
		ConstraintViolations violations = addressValidator.validate(address);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message()).isEqualTo("\"street\" must not be blank");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).message()).isEqualTo("\"country\" must not be null");
		assertThat(violations.get(1).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	void valid() {
		Validator<Address> addressValidator = validator();
		Address address = new Address(new Country("JP"), "tokyo", new PhoneNumber("0123456789"));
		ConstraintViolations violations = addressValidator.validate(address);
		assertThat(violations.isValid()).isTrue();
	}

	abstract protected Validator<Address> validator();

}
