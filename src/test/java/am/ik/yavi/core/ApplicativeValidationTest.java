/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Objects;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.fn.Validations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.jspecify.annotations.Nullable;

class ApplicativeValidationTest {

	static final ApplicativeValidator<String> streetValidator = ValidatorBuilder.of(String.class)
		._string(s -> s, "street", c -> c.notBlank())
		.build()
		.applicative();

	static final ValueValidator<Map<String, String>, Country> mapCountryValidator = Country.validator()
		.prefixed("country")
		.applicative()
		.compose(map -> new Country(Objects.requireNonNull(map).get("country")));

	static final ValueValidator<Country, @Nullable String> countryValidator = Country.validator()
		.prefixed("country")
		.applicative()
		.<@Nullable String>map(Country::name);

	@ParameterizedTest
	@MethodSource("validValidations")
	void validated_valid(Validated<Address> validation) {
		assertThat(validation.isValid()).isTrue();
		final Address address = validation.value();
		assertThat(address).isNotNull();
		assertThat(Objects.requireNonNull(address.country()).name()).isEqualTo("jp");
		assertThat(address.street()).isEqualTo("xyz");
		assertThat(Objects.requireNonNull(address.phoneNumber()).value()).isEqualTo("12345678");
	}

	@ParameterizedTest
	@MethodSource("invalidValidations")
	void validated_invalid(Validated<Address> validation) {
		assertThat(validation.isValid()).isFalse();
		final List<ConstraintViolation> violations = validation.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("country.name");
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("street");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("phoneNumber.value");
		assertThat(violations.get(2).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void compose_valid() {
		final Validated<Country> countryValidated = mapCountryValidator
			.validate(Collections.singletonMap("country", "JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@Test
	void compose_invalid() {
		final Validated<Country> countryValidated = mapCountryValidator
			.validate(Collections.singletonMap("country", "J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}

	@Test
	void andThen_valid() {
		final Validated<@Nullable String> countryValidated = countryValidator.validate(new Country("JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value()).isEqualTo("JP");
	}

	@Test
	void andThen_invalid() {
		final Validated<@Nullable String> countryValidated = countryValidator.validate(new Country("J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}

	static Stream<Arguments> validValidations() {
		final Validated<Country> countryValidation = Country.of("jp");
		final Validated<String> streetValidation = streetValidator.validate("xyz");
		final Validated<PhoneNumber> phoneNumberValidation = PhoneNumber.of("12345678");
		return Stream.of(
				arguments(
						countryValidation.combine(streetValidation).combine(phoneNumberValidation).apply(Address::new)),
				arguments(Validations.apply(Address::new, countryValidation, streetValidation, phoneNumberValidation)));
	}

	static Stream<Arguments> invalidValidations() {
		final Validated<Country> countryValidation = Country.of("j");
		final Validated<String> streetValidation = streetValidator.validate("");
		final Validated<PhoneNumber> phoneNumberValidation = PhoneNumber.of("1234567");
		return Stream.of(
				arguments(
						countryValidation.combine(streetValidation).combine(phoneNumberValidation).apply(Address::new)),
				arguments(Validations.apply(Address::new, countryValidation, streetValidation, phoneNumberValidation)));
	}

}