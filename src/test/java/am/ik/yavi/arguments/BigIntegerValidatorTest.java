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

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.BigIntegerValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigIntegerValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(BigIntegerValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(BigInteger.valueOf(100));
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(BigInteger.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(BigIntegerValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(BigInteger.valueOf(-1));
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(BigIntegerValidator<Price> priceValidator) {
		final Price price = priceValidator.validated(BigInteger.valueOf(100));
		assertThat(price.value()).isEqualTo(BigInteger.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(BigIntegerValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated(BigInteger.valueOf(-1)))
			.isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(BigIntegerValidator<Price> priceValidator) {
		final Map<String, BigInteger> params = Collections.singletonMap("price", BigInteger.valueOf(100));
		final Arguments1Validator<Map<String, BigInteger>, Price> mapValidator = priceValidator
			.compose(map -> map.get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(BigInteger.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(BigIntegerValidator<Price> priceValidator) {
		final Map<String, BigInteger> params = Collections.singletonMap("price", BigInteger.valueOf(-1));
		final Arguments1Validator<Map<String, BigInteger>, Price> mapValidator = priceValidator
			.compose(map -> map.get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<BigIntegerValidator<Price>> validators() {
		return Stream.of(
				BigIntegerValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(BigInteger.valueOf(0)))
					.build(Price::new),
				BigIntegerValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(BigInteger.valueOf(0)))
					.build()
					.andThen(Price::new));
	}

	public static class Price {

		private final BigInteger value;

		public Price(BigInteger value) {
			this.value = value;
		}

		public BigInteger value() {
			return value;
		}

	}

}