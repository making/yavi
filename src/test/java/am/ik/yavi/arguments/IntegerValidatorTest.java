/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.IntegerValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntegerValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(IntegerValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(100);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(IntegerValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate(-1);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(IntegerValidator<Price> priceValidator) {
		final Price price = priceValidator.validated(100);
		assertThat(price.value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(IntegerValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated(-1)).isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(IntegerValidator<Price> priceValidator) {
		final Map<String, Integer> params = Collections.singletonMap("price", 100);
		final Arguments1Validator<Map<String, Integer>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo(100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(IntegerValidator<Price> priceValidator) {
		final Map<String, Integer> params = Collections.singletonMap("price", -1);
		final Arguments1Validator<Map<String, Integer>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<IntegerValidator<Price>> validators() {
		return Stream.of(IntegerValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(0)).build(Price::new),
				IntegerValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual(0)).build().map(Price::new));
	}

	public static class Price {

		private final int value;

		public Price(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}

	}

}