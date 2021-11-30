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

import java.time.YearMonth;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.YearMonthValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearMonthValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		final Validated<CreditCardExpiryDate> expiryDateValidated = expiryDateValidator
				.validate(YearMonth.now().plusYears(5));
		assertThat(expiryDateValidated.isValid()).isTrue();
		assertThat(expiryDateValidated.value().value()).isEqualTo(YearMonth.now().plusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		final Validated<CreditCardExpiryDate> expiryDateValidated = expiryDateValidator
				.validate(YearMonth.now().minusYears(3));
		assertThat(expiryDateValidated.isValid()).isFalse();
		final ConstraintViolations violations = expiryDateValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("credit card expiry date");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.after");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		final CreditCardExpiryDate expiryDate = expiryDateValidator.validated(YearMonth.now().plusYears(5));
		assertThat(expiryDate.value()).isEqualTo(YearMonth.now().plusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		YearMonth today = YearMonth.now();
		assertThatThrownBy(() -> expiryDateValidator.validated(today.minusYears(3)))
				.isInstanceOf(ConstraintViolationsException.class)
				.hasMessageContaining("\"credit card expiry date\" must be after " + today);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		final Map<String, YearMonth> params = Collections.singletonMap("credit card expiry date",
				YearMonth.now().plusYears(5));
		final Arguments1Validator<Map<String, YearMonth>, CreditCardExpiryDate> mapValidator = expiryDateValidator
				.compose(map -> map.get("credit card expiry date"));
		final Validated<CreditCardExpiryDate> expiryDateValidated = mapValidator.validate(params);
		assertThat(expiryDateValidated.isValid()).isTrue();
		assertThat(expiryDateValidated.value().value()).isEqualTo(YearMonth.now().plusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(YearMonthValidator<CreditCardExpiryDate> expiryDateValidator) {
		final Map<String, YearMonth> params = Collections.singletonMap("credit card expiry date",
				YearMonth.now().minusYears(3));
		final Arguments1Validator<Map<String, YearMonth>, CreditCardExpiryDate> mapValidator = expiryDateValidator
				.compose(map -> map.get("credit card expiry date"));
		final Validated<CreditCardExpiryDate> expiryDateValidated = mapValidator.validate(params);
		assertThat(expiryDateValidated.isValid()).isFalse();
		final ConstraintViolations violations = expiryDateValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("credit card expiry date");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.after");
	}

	static Stream<YearMonthValidator<CreditCardExpiryDate>> validators() {
		YearMonth today = YearMonth.now();
		return Stream.of(
				YearMonthValidatorBuilder
						.of("credit card expiry date",
								c -> c.notNull().after(today))
						.build(CreditCardExpiryDate::new),
				YearMonthValidatorBuilder
						.of("credit card expiry date",
								c -> c.notNull().after(today))
						.build().andThen(CreditCardExpiryDate::new));
	}

	public static class CreditCardExpiryDate {
		private final YearMonth value;

		public CreditCardExpiryDate(YearMonth value) {
			this.value = value;
		}

		public YearMonth value() {
			return value;
		}
	}
}
