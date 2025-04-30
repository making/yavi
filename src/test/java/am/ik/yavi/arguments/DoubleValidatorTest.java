/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.builder.DoubleValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DoubleValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(DoubleValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((double) 100);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(DoubleValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((double) -1);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(DoubleValidator<Price> priceValidator) {
		final Price price = priceValidator.validated((double) 100);
		assertThat(price.value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(DoubleValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated((double) -1))
			.isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(DoubleValidator<Price> priceValidator) {
		final Map<String, Double> params = Collections.singletonMap("price", (double) 100);
		final Arguments1Validator<Map<String, Double>, Price> mapValidator = priceValidator
			.compose(map -> map.get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(DoubleValidator<Price> priceValidator) {
		final Map<String, Double> params = Collections.singletonMap("price", (double) -1);
		final Arguments1Validator<Map<String, Double>, Price> mapValidator = priceValidator
			.compose(map -> map.get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<DoubleValidator<Price>> validators() {
		return Stream.of(
				DoubleValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((double) 0)).build(Price::new),
				DoubleValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((double) 0))
					.build()
					.map(Price::new));
	}

	public static class Price {

		private final double value;

		public Price(double value) {
			this.value = value;
		}

		public double value() {
			return value;
		}

	}

}