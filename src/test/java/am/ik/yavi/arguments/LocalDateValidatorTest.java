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

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.LocalDateValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(LocalDateValidator<Birthday> birthdayValidator) {
		final Validated<Birthday> birthdayValidated = birthdayValidator
				.validate(LocalDate.of(2003, Month.NOVEMBER, 30));
		assertThat(birthdayValidated.isValid()).isTrue();
		assertThat(birthdayValidated.value().value()).isEqualTo(LocalDate.of(2003, Month.NOVEMBER, 30));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(LocalDateValidator<Birthday> birthdayValidator) {
		final Validated<Birthday> birthdayValidated = birthdayValidator
				.validate(LocalDate.now().plusYears(3));
		assertThat(birthdayValidated.isValid()).isFalse();
		final ConstraintViolations violations = birthdayValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("birthday");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.onOrBefore");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(LocalDateValidator<Birthday> birthdayValidator) {
		final Birthday birthday = birthdayValidator.validated(LocalDate.of(2003, Month.NOVEMBER, 30));
		assertThat(birthday.value()).isEqualTo(LocalDate.of(2003, Month.NOVEMBER, 30));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(LocalDateValidator<Birthday> birthdayValidator) {
		LocalDate today = LocalDate.now();
		assertThatThrownBy(() -> birthdayValidator.validated(today.plusYears(3)))
				.isInstanceOf(ConstraintViolationsException.class)
				.hasMessageContaining("\"birthday\" must be on or before " + today);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(LocalDateValidator<Birthday> birthdayValidator) {
		final Map<String, LocalDate> params = Collections.singletonMap("birthday",
				LocalDate.of(2003, Month.NOVEMBER, 30));
		final Arguments1Validator<Map<String, LocalDate>, Birthday> mapValidator = birthdayValidator
				.compose(map -> map.get("birthday"));
		final Validated<Birthday> birthdayValidated = mapValidator.validate(params);
		assertThat(birthdayValidated.isValid()).isTrue();
		assertThat(birthdayValidated.value().value()).isEqualTo(LocalDate.of(2003, Month.NOVEMBER, 30));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(LocalDateValidator<Birthday> birthdayValidator) {
		final Map<String, LocalDate> params = Collections.singletonMap("birthday",
				LocalDate.now().plusYears(3));
		final Arguments1Validator<Map<String, LocalDate>, Birthday> mapValidator = birthdayValidator
				.compose(map -> map.get("birthday"));
		final Validated<Birthday> birthdayValidated = mapValidator.validate(params);
		assertThat(birthdayValidated.isValid()).isFalse();
		final ConstraintViolations violations = birthdayValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("birthday");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.onOrBefore");
	}

	static Stream<LocalDateValidator<Birthday>> validators() {
		LocalDate today = LocalDate.now();
		return Stream.of(
				LocalDateValidatorBuilder
						.of("birthday",
								c -> c.notNull().onOrBefore(today))
						.build(Birthday::new),
				LocalDateValidatorBuilder
						.of("birthday",
								c -> c.notNull().onOrBefore(today))
						.build().andThen(Birthday::new));
	}

	public static class Birthday {
		private final LocalDate value;

		public Birthday(LocalDate value) {
			this.value = value;
		}

		public LocalDate value() {
			return value;
		}
	}
}
