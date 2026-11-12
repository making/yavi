/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.FloatValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloatValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(FloatValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((float) 100);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo((float) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(FloatValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((float) -1);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(FloatValidator<Price> priceValidator) {
		final Price price = priceValidator.validated((float) 100);
		assertThat(price.value()).isEqualTo((float) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(FloatValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated((float) -1)).isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(FloatValidator<Price> priceValidator) {
		final Map<String, Float> params = Collections.singletonMap("price", (float) 100);
		final Arguments1Validator<Map<String, Float>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo((float) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(FloatValidator<Price> priceValidator) {
		final Map<String, Float> params = Collections.singletonMap("price", (float) -1);
		final Arguments1Validator<Map<String, Float>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<FloatValidator<Price>> validators() {
		return Stream.of(
				FloatValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((float) 0))
					.build(value -> new Price(Objects.requireNonNull(value))),
				FloatValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((float) 0))
					.build()
					.map(value -> new Price(Objects.requireNonNull(value))));
	}

	public static class Price {

		private final float value;

		public Price(float value) {
			this.value = value;
		}

		public float value() {
			return value;
		}

	}

}