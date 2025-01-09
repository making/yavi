/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.builder.BooleanValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BooleanValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(BooleanValidator<Checked> checkedValidator) {
		final Validated<Checked> checkedValidated = checkedValidator.validate(true);
		assertThat(checkedValidated.isValid()).isTrue();
		assertThat(checkedValidated.value().isChecked()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(BooleanValidator<Checked> checkedValidator) {
		final Validated<Checked> checkedValidated = checkedValidator.validate(false);
		assertThat(checkedValidated.isValid()).isFalse();
		final ConstraintViolations violations = checkedValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("checked");
		assertThat(violations.get(0).messageKey()).isEqualTo("boolean.isTrue");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(BooleanValidator<Checked> checkedValidator) {
		final Checked checked = checkedValidator.validated(true);
		assertThat(checked.isChecked()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(BooleanValidator<Checked> checkedValidator) {
		assertThatThrownBy(() -> checkedValidator.validated(false)).isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"checked\" must be true");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(BooleanValidator<Checked> checkedValidator) {
		final Map<String, Boolean> params = Collections.singletonMap("checked", true);
		final Arguments1Validator<Map<String, Boolean>, Checked> mapValidator = checkedValidator
			.compose(map -> map.get("checked"));
		final Validated<Checked> checkedValidated = mapValidator.validate(params);
		assertThat(checkedValidated.isValid()).isTrue();
		assertThat(checkedValidated.value().isChecked()).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(BooleanValidator<Checked> checkedValidator) {
		final Map<String, Boolean> params = Collections.singletonMap("checked", false);
		final Arguments1Validator<Map<String, Boolean>, Checked> mapValidator = checkedValidator
			.compose(map -> map.get("checked"));
		final Validated<Checked> checkedValidated = mapValidator.validate(params);
		assertThat(checkedValidated.isValid()).isFalse();
		final ConstraintViolations violations = checkedValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("checked");
		assertThat(violations.get(0).messageKey()).isEqualTo("boolean.isTrue");
	}

	static Stream<BooleanValidator<Checked>> validators() {
		return Stream.of(BooleanValidatorBuilder.of("checked", c -> c.notNull().isTrue()).build(Checked::new),
				BooleanValidatorBuilder.of("checked", c -> c.notNull().isTrue()).build().andThen(Checked::new));
	}

	public static class Checked {

		private final boolean checked;

		public Checked(boolean checked) {
			this.checked = checked;
		}

		public boolean isChecked() {
			return checked;
		}

	}

}