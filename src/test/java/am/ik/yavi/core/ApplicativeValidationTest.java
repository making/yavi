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
import java.util.stream.Stream;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.fn.Validation;
import am.ik.yavi.fn.Validations;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ApplicativeValidationTest {
	static final ApplicativeValidator<Country> countryValidator = Country.validator()
			.prefixed("country").applicative();

	static final ApplicativeValidator<String> streetValidator = ValidatorBuilder
			.of(String.class)._string(s -> s, "street", c -> c.notBlank()).build()
			.applicative();

	static final ApplicativeValidator<PhoneNumber> phoneNumberValidator = PhoneNumber
			.validator().prefixed("phoneNumber").applicative();

	@ParameterizedTest
	@MethodSource("validValidations")
	void validated_valid(Validation<ConstraintViolation, Address> validation) {
		assertThat(validation.isValid()).isTrue();
		final Address address = validation.value();
		assertThat(address).isNotNull();
		assertThat(address.country().name()).isEqualTo("jp");
		assertThat(address.street()).isEqualTo("xyz");
		assertThat(address.phoneNumber().value()).isEqualTo("12345678");
	}

	@ParameterizedTest
	@MethodSource("invalidValidations")
	void validated_invalid(Validation<ConstraintViolation, Address> validation) {
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

	static Stream<Arguments> validValidations() {
		final Validation<ConstraintViolation, Country> countryValidation = countryValidator
				.validate(new Country("jp"));
		final Validation<ConstraintViolation, String> streetValidation = streetValidator
				.validate("xyz");
		final Validation<ConstraintViolation, PhoneNumber> phoneNumberValidation = phoneNumberValidator
				.validate(new PhoneNumber("12345678"));
		return Stream.of(
				arguments(countryValidation.compose(streetValidation)
						.compose(phoneNumberValidation).apply(Address::new)),
				arguments(Validations.apply(Address::new, countryValidation, streetValidation,
						phoneNumberValidation)));
	}

	static Stream<Arguments> invalidValidations() {
		final Validation<ConstraintViolation, Country> countryValidation = countryValidator
				.validate(new Country("j"));
		final Validation<ConstraintViolation, String> streetValidation = streetValidator
				.validate("");
		final Validation<ConstraintViolation, PhoneNumber> phoneNumberValidation = phoneNumberValidator
				.validate(new PhoneNumber("1234567"));
		return Stream.of(
				arguments(countryValidation.compose(streetValidation)
						.compose(phoneNumberValidation).apply(Address::new)),
				arguments(Validations.apply(Address::new, countryValidation, streetValidation,
						phoneNumberValidation)));
	}
}