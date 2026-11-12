/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.time.Instant;
import java.util.Objects;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

import am.ik.yavi.builder.ObjectValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.CustomConstraint;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectValidatorTest {

	static final CustomConstraint<Instant> past = new CustomConstraint<Instant>() {

		@Override
		public boolean test(Instant instant) {
			return instant.isBefore(Instant.now());
		}

		@Override
		public String defaultMessageFormat() {
			return "\"{0}\" must be past";
		}

		@Override
		public String messageKey() {
			return "instant.past";
		}
	};

	@ParameterizedTest
	@MethodSource("validators")
	void validateValid(ObjectValidator<Instant, Date> dateValidator) {
		final Validated<Date> dateValidated = dateValidator.validate(Instant.ofEpochMilli(1000L));
		assertThat(dateValidated.isValid()).isTrue();
		assertThat(dateValidated.value().getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validateInvalid(ObjectValidator<Instant, Date> dateValidator) {
		final Validated<Date> dateValidated = dateValidator.validate(Instant.now().plusSeconds(1));
		assertThat(dateValidated.isValid()).isFalse();
		final ConstraintViolations violations = dateValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("createdAt");
		assertThat(violations.get(0).messageKey()).isEqualTo("instant.past");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedValid(ObjectValidator<Instant, Date> dateValidator) {
		final Date date = dateValidator.validated(Instant.ofEpochMilli(1000L));
		assertThat(date.getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void validatedInvalid(ObjectValidator<Instant, Date> dateValidator) {
		assertThatThrownBy(() -> dateValidator.validated(Instant.now().plusSeconds(1)))
			.isInstanceOf(ConstraintViolationsException.class)
			.hasMessageContaining("\"createdAt\" must be past");
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeValid(ObjectValidator<Instant, Date> dateValidator) {
		final Map<String, Instant> params = Collections.singletonMap("createdAt", Instant.ofEpochMilli(1000L));
		final Arguments1Validator<Map<String, Instant>, Date> mapValidator = dateValidator
			.compose(map -> Objects.requireNonNull(map).get("createdAt"));
		final Validated<Date> dateValidated = mapValidator.validate(params);
		assertThat(dateValidated.isValid()).isTrue();
		assertThat(dateValidated.value().getTime()).isEqualTo(1000L);
	}

	@ParameterizedTest
	@MethodSource("validators")
	void composeInvalid(ObjectValidator<Instant, Date> dateValidator) {
		final Map<String, Instant> params = Collections.singletonMap("createdAt", Instant.now().plusSeconds(1));
		final Arguments1Validator<Map<String, Instant>, Date> mapValidator = dateValidator
			.compose(map -> Objects.requireNonNull(map).get("createdAt"));
		final Validated<Date> dateValidated = mapValidator.validate(params);
		assertThat(dateValidated.isValid()).isFalse();
		final ConstraintViolations violations = dateValidated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("createdAt");
		assertThat(violations.get(0).messageKey()).isEqualTo("instant.past");
	}

	static Stream<ObjectValidator<Instant, Date>> validators() {
		return Stream.of(
				ObjectValidatorBuilder.<Instant>of("createdAt", c -> c.notNull().predicateNullable(past))
					.build(Date::from),
				ObjectValidatorBuilder.<Instant>of("createdAt", c -> c.notNull().predicateNullable(past))
					.build()
					.map(Date::from));
	}

}
