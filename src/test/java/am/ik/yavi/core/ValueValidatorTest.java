/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueValidatorTest {

	@Test
	void passThrough() {
		final ValueValidator<String, String> validator = ValueValidator.passThrough();
		final Validated<String> validated = validator.validate("hello");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isEqualTo("hello");
	}

	@Test
	void andThenChainsValidators() {
		ValueValidator<String, Country> countryValidator = Country.validator().applicative().compose(Country::new);
		ValueValidator<String, String> streetValidator = ValueValidator.passThrough();
		ValueValidator<String, PhoneNumber> phoneValidator = PhoneNumber.validator()
			.applicative()
			.compose(PhoneNumber::new);

		Arguments3Validator<String, String, String, Address> addressValidator = Arguments1Validator
			.from(countryValidator)
			.split(streetValidator)
			.split(phoneValidator)
			.apply(Address::new);

		ValueValidator<Address, Address> foreignAddressValidator = ValidatorBuilder.<Address>of()
			.constraintOnCondition(
					(address, group) -> !"JP".equalsIgnoreCase(Objects.requireNonNull(address.country()).name()),
					ValidatorBuilder.<Address>of()
						._string(a -> Objects.requireNonNull(a.phoneNumber()).value(), "PhoneNumber",
								c -> c.startsWith("+"))
						.build())
			.build()
			.applicative();

		assertThat(addressValidator.validate("JP", "tokyo", "0123456789").isValid()).isTrue();
		assertThat(addressValidator.andThen(foreignAddressValidator).validate("JP", "tokyo", "0123456789").isValid())
			.isTrue();
		assertThat(addressValidator.validate("BE", "brussels", "9876543210").isValid()).isTrue();
		assertThat(addressValidator.andThen(foreignAddressValidator).validate("BE", "brussels", "9876543210").isValid())
			.isFalse();
		assertThat(addressValidator.validate("J", "tokyo", "0123456789").isValid()).isFalse();
		assertThat(addressValidator.andThen(foreignAddressValidator).validate("J", "tokyo", "+0123456789").isValid())
			.isFalse();
	}

	@Test
	void invalidPatternStringToLocalDateValidator() {
		ValueValidator<String, LocalDate> localDateValidator = ValidatorBuilder.<String>of()
			._string(f -> f, "myLocalDate", CharSequenceConstraint::isoLocalDate)
			.build()
			.applicative()
			.map(LocalDate::parse);
		final Validated<LocalDate> localDateValidated = localDateValidator.validate("31/01/2022");
		Assertions.assertThat(localDateValidated.isValid()).isFalse();
		Assertions.assertThat(localDateValidated.errors()).hasSize(1);
		Assertions.assertThat(localDateValidated.errors().get(0).messageKey()).isEqualTo("charSequence.localdate");
		Assertions.assertThat(localDateValidated.errors().get(0).message())
			.isEqualTo(
					"\"myLocalDate\" must be a valid representation of a local date using the pattern: uuuu-MM-dd. The give value is: 31/01/2022");
	}

	@Test
	void validStringToLocalDateValidator() {
		ValueValidator<String, LocalDate> localDateValidator = ValidatorBuilder.<String>of()
			._string(f -> f, "myLocalDate", c -> c.localDate("dd/MM/uuuu"))
			.build()
			.applicative()
			.map(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/uuuu")));
		final Validated<LocalDate> localDateValidated = localDateValidator.validate("31/01/2022");
		Assertions.assertThat(localDateValidated.isValid()).isTrue();
		Assertions.assertThat(localDateValidated.value()).isEqualTo(LocalDate.of(2022, 1, 31));
	}

	@Test
	void nullInputIsAcceptedWithoutApplyingConstraints() {
		final ValueValidator<String, String> name = StringValidatorBuilder.of("name", c -> c.notBlank()).build();
		assertThat(name.validate(null).isValid()).isFalse();

		assertThat(name.optional().validated(null)).isEmpty();
		assertThat(name.optional().validated("yavi")).isEqualTo(Optional.of("yavi"));
		assertThat(name.nullable().validated(null)).isNull();
		assertThat(name.nullable().validated("yavi")).isEqualTo("yavi");
		assertThat(name.defaultValue("anonymous").validated(null)).isEqualTo("anonymous");
		assertThat(name.defaultValue("anonymous").validated("yavi")).isEqualTo("yavi");

		// a value that is present is still validated
		assertThat(name.optional().validate(" ").errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("charSequence.notBlank");
		assertThat(name.nullable().validate(" ").isValid()).isFalse();
		assertThat(name.defaultValue("anonymous").validate(" ").isValid()).isFalse();
	}

	@Test
	void anyOfProducesTheValueOfTheFirstValidAlternative() {
		final ValueValidator<String, Number> number = ValueValidator.anyOf(
				StringValidatorBuilder.of("", c -> c.isInteger()).build(Integer::valueOf),
				StringValidatorBuilder.of("", c -> c.isBigDecimal()).build(BigDecimal::new));
		assertThat(number.validated("1")).isEqualTo(1);
		assertThat(number.validated("1.5")).isEqualTo(new BigDecimal("1.5"));

		final ConstraintViolations violations = number.prefixed("amount").validate("x").errors();
		assertThat(violations).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.anyOf", "charSequence.integer", "charSequence.bigdecimal");
		assertThat(violations).extracting(ConstraintViolation::name).containsOnly("amount");
		assertThat(violations.get(0).message()).isEqualTo("\"amount\" must satisfy at least one of the 2 alternatives");
		assertThat(violations.get(0).args()).containsExactly("amount", 2, "x");

		assertThatThrownBy(ValueValidator::anyOf).isInstanceOf(IllegalArgumentException.class);
	}

}
