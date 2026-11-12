/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.BigDecimalValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(BigDecimalValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(BigDecimal.valueOf(100));
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(BigDecimal.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(BigDecimalValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(BigDecimal.valueOf(-1));
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(BigDecimalValidator<Price> priceValidator) {
		final Price price = priceValidator.validated(BigDecimal.valueOf(100));
		assertThat(price.value()).isEqualTo(BigDecimal.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(BigDecimalValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated(BigDecimal.valueOf(-1)))
			.isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(BigDecimalValidator<Price> priceValidator) {
		final Map<String, BigDecimal> params = Collections.singletonMap("price", BigDecimal.valueOf(100));
		final Arguments1Validator<Map<String, BigDecimal>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(BigDecimal.valueOf(100));
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(BigDecimalValidator<Price> priceValidator) {
		final Map<String, BigDecimal> params = Collections.singletonMap("price", BigDecimal.valueOf(-1));
		final Arguments1Validator<Map<String, BigDecimal>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<BigDecimalValidator<Price>> validators() {
		return Stream.of(
				BigDecimalValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(BigDecimal.valueOf(0)))
					.build(value -> new Price(Objects.requireNonNull(value))),
				BigDecimalValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(BigDecimal.valueOf(0)))
					.build()
					.map(value -> new Price(Objects.requireNonNull(value))));
	}

	public static class Price {

		private final BigDecimal value;

		public Price(BigDecimal value) {
			this.value = value;
		}

		public BigDecimal value() {
			return value;
		}

	}

}