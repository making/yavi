/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.ShortValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShortValidatorTest {

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(ShortValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((short) 100);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo((short) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(ShortValidator<Price> priceValidator) {
		final Validated<Price> priceValidated = priceValidator.validate((short) -1);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(ShortValidator<Price> priceValidator) {
		final Price price = priceValidator.validated((short) 100);
		assertThat(price.value()).isEqualTo((short) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(ShortValidator<Price> priceValidator) {
		assertThatThrownBy(() -> priceValidator.validated((short) -1)).isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"price\" must be greater than or equal to 0");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(ShortValidator<Price> priceValidator) {
		final Map<String, Short> params = Collections.singletonMap("price", (short) 100);
		final Arguments1Validator<Map<String, Short>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isTrue();
		assertThat(priceValidated.value().value()).isEqualTo((short) 100);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(ShortValidator<Price> priceValidator) {
		final Map<String, Short> params = Collections.singletonMap("price", (short) -1);
		final Arguments1Validator<Map<String, Short>, Price> mapValidator = priceValidator
			.compose(map -> Objects.requireNonNull(map).get("price"));
		final Validated<Price> priceValidated = mapValidator.validate(params);
		assertThat(priceValidated.isValid()).isFalse();
		final ConstraintViolations violations = priceValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("price");
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
	}

	static Stream<ShortValidator<Price>> validators() {
		return Stream.of(
				ShortValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((short) 0))
					.build(value -> new Price(Objects.requireNonNull(value))),
				ShortValidatorBuilder.of("price", c -> c.notNull().greaterThanOrEqual((short) 0))
					.build()
					.map(value -> new Price(Objects.requireNonNull(value))));
	}

	public static class Price {

		private final short value;

		public Price(short value) {
			this.value = value;
		}

		public short value() {
			return value;
		}

	}

}