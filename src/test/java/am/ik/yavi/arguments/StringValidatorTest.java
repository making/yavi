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
		assertThat(violations.get(1).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
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
		assertThatThrownBy(() -> countryValidator.validated(" "))
				.isInstanceOf(ConstraintViolationsException.class)
				.hasMessageContaining("\"country\" must not be blank")
				.hasMessageContaining(
						"The size of \"country\" must be greater than or equal to 2. The given size is 1");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void contramapValid(StringValidator<Country> countryValidator) {
		final Map<String, String> params = Collections.singletonMap("country", "JP");
		final Arguments1Validator<Map<String, String>, Country> mapValidator = countryValidator
				.contramap(map -> map.get("country"));
		final Validated<Country> countryValidated = mapValidator.validate(params);
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void contramapInvalid(StringValidator<Country> countryValidator) {
		final Map<String, String> params = Collections.singletonMap("country", " ");
		final Arguments1Validator<Map<String, String>, Country> mapValidator = countryValidator
				.contramap(map -> map.get("country"));
		final Validated<Country> countryValidated = mapValidator.validate(params);
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).name()).isEqualTo("country");
		assertThat(violations.get(1).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
	}

	static Stream<StringValidator<Country>> validators() {
		return Stream.of(
				StringValidatorBuilder
						.of("country", c -> c.notBlank().greaterThanOrEqual(2))
						.build(Country::new),
				StringValidatorBuilder
						.of("country", c -> c.notBlank().greaterThanOrEqual(2)).build()
						.map(Country::new));
	}
}