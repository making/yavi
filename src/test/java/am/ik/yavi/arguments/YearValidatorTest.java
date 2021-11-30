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

import java.time.Year;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.YearValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(YearValidator<FiscalYear> fiscalYearValidator) {
		final Validated<FiscalYear> fiscalYearValidated = fiscalYearValidator
				.validate(Year.now().minusYears(5));
		assertThat(fiscalYearValidated.isValid()).isTrue();
		assertThat(fiscalYearValidated.value().value()).isEqualTo(Year.now().minusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(YearValidator<FiscalYear> fiscalYearValidator) {
		final Validated<FiscalYear> fiscalYearValidated = fiscalYearValidator
				.validate(Year.now().plusYears(3));
		assertThat(fiscalYearValidated.isValid()).isFalse();
		final ConstraintViolations violations = fiscalYearValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("fiscal year");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.before");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(YearValidator<FiscalYear> fiscalYearValidator) {
		final FiscalYear fiscalYear = fiscalYearValidator.validated(Year.now().minusYears(5));
		assertThat(fiscalYear.value()).isEqualTo(Year.now().minusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(YearValidator<FiscalYear> fiscalYearValidator) {
		Year today = Year.now();
		assertThatThrownBy(() -> fiscalYearValidator.validated(today.plusYears(3)))
				.isInstanceOf(ConstraintViolationsException.class)
				.hasMessageContaining("\"fiscal year\" must be before " + today);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(YearValidator<FiscalYear> fiscalYearValidator) {
		final Map<String, Year> params = Collections.singletonMap("fiscal year",
				Year.now().minusYears(5));
		final Arguments1Validator<Map<String, Year>, FiscalYear> mapValidator = fiscalYearValidator
				.compose(map -> map.get("fiscal year"));
		final Validated<FiscalYear> fiscalYearValidated = mapValidator.validate(params);
		assertThat(fiscalYearValidated.isValid()).isTrue();
		assertThat(fiscalYearValidated.value().value()).isEqualTo(Year.now().minusYears(5));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(YearValidator<FiscalYear> fiscalYearValidator) {
		final Map<String, Year> params = Collections.singletonMap("fiscal year",
				Year.now().plusYears(3));
		final Arguments1Validator<Map<String, Year>, FiscalYear> mapValidator = fiscalYearValidator
				.compose(map -> map.get("fiscal year"));
		final Validated<FiscalYear> fiscalYearValidated = mapValidator.validate(params);
		assertThat(fiscalYearValidated.isValid()).isFalse();
		final ConstraintViolations violations = fiscalYearValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("fiscal year");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("temporal.before");
	}

	static Stream<YearValidator<FiscalYear>> validators() {
		Year today = Year.now();
		return Stream.of(
				YearValidatorBuilder
						.of("fiscal year",
								c -> c.notNull().before(today))
						.build(FiscalYear::new),
				YearValidatorBuilder
						.of("fiscal year",
								c -> c.notNull().before(today))
						.build().andThen(FiscalYear::new));
	}

	public static class FiscalYear {
		private final Year value;

		public FiscalYear(Year value) {
			this.value = value;
		}

		public Year value() {
			return value;
		}
	}
}
