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
package am.ik.yavi.core;

import java.util.List;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.fn.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicativeValidationTest {
	final ApplicativeValidator<Country> countryValidator = Country.validator()
			.prefixed("country").applicative();

	final ApplicativeValidator<String> streetValidator = ValidatorBuilder.of(String.class)
			._string(s -> s, "street", c -> c.notBlank()).build().applicative();

	final ApplicativeValidator<PhoneNumber> phoneNumberValidator = PhoneNumber.validator()
			.prefixed("phoneNumber").applicative();

	@Test
	void validated_valid() {
		final Validation<ConstraintViolation, Address> validation = countryValidator
				.validate(new Country("jp")).compose(streetValidator.validate("xyz"))
				.compose(phoneNumberValidator.validate(new PhoneNumber("12345678")))
				.apply(Address::new);
		assertThat(validation.isValid()).isTrue();
		final Address address = validation.value();
		assertThat(address).isNotNull();
		assertThat(address.country().name()).isEqualTo("jp");
		assertThat(address.street()).isEqualTo("xyz");
		assertThat(address.phoneNumber().value()).isEqualTo("12345678");
	}

	@Test
	void validated_invalid() {
		final Validation<ConstraintViolation, Address> validation = countryValidator
				.validate(new Country("j")).compose(streetValidator.validate(""))
				.compose(phoneNumberValidator.validate(new PhoneNumber("1234567")))
				.apply(Address::new);
		assertThat(validation.isValid()).isFalse();
		final List<ConstraintViolation> violations = validation.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("country.name");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("street");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("phoneNumber.value");
		assertThat(violations.get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
	}
}