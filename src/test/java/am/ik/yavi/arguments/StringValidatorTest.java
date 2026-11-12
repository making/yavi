/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.Country;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(StringValidator<Country> countryValidator) {
		final Validated<Country> countryValidated = countryValidator.validate("JP");
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(StringValidator<Country> countryValidator) {
		final Validated<Country> countryValidated = countryValidator.validate(" ");
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("country");
		assertThat(violations.get(1).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(StringValidator<Country> countryValidator) {
		final Country country = countryValidator.validated("JP");
		assertThat(country.name()).isEqualTo("JP");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(StringValidator<Country> countryValidator) {
		assertThatThrownBy(() -> countryValidator.validated(" ")).isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"country\" must not be blank")
			.hasMessageContaining("The size of \"country\" must be greater than or equal to 2. The given size is 1");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(StringValidator<Country> countryValidator) {
		final Map<String, String> params = Collections.singletonMap("country", "JP");
		final Arguments1Validator<Map<String, String>, Country> mapValidator = countryValidator
			.compose(map -> Objects.requireNonNull(map).get("country"));
		final Validated<Country> countryValidated = mapValidator.validate(params);
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(StringValidator<Country> countryValidator) {
		final Map<String, String> params = Collections.singletonMap("country", " ");
		final Arguments1Validator<Map<String, String>, Country> mapValidator = countryValidator
			.compose(map -> Objects.requireNonNull(map).get("country"));
		final Validated<Country> countryValidated = mapValidator.validate(params);
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("country");
		assertThat(violations.get(1).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	static Stream<StringValidator<Country>> validators() {
		return Stream.of(
				StringValidatorBuilder.of("country", c -> c.notBlank().greaterThanOrEqual(2)).build(Country::new),
				StringValidatorBuilder.of("country", c -> c.notBlank().greaterThanOrEqual(2))
					.build()
					.map(Country::new));
	}

}