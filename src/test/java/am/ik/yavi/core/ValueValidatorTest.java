/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Objects;
import java.util.Optional;
import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.arguments.IntegerValidator;
import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.validator.Yavi;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.jspecify.annotations.Nullable;

class ValueValidatorTest {

	@Test
	void passThrough() {
		final ValueValidator<String, @Nullable String> validator = ValueValidator.passThrough();
		final Validated<@Nullable String> validated = validator.validate("hello");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isEqualTo("hello");
	}

	@Test
	void andThenChainsValidators() {
		ValueValidator<String, Country> countryValidator = Country.validator().applicative().compose(Country::new);
		ValueValidator<String, @Nullable String> streetValidator = ValueValidator.passThrough();
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
		// unless the validator recovers, which replaces any failure; the function is
		// given the violations, and none for null
		assertThat(name.recover("anonymous").validated(null)).isEqualTo("anonymous");
		assertThat(name.recover("anonymous").validated(" ")).isEqualTo("anonymous");
		assertThat(name.recover("anonymous").validated("yavi")).isEqualTo("yavi");
		final ValueValidator<String, String> reported = name
			.recoverWith(violations -> violations.stream().map(ConstraintViolation::messageKey).toList().toString());
		assertThat(reported.validated(null)).isEqualTo("[]");
		assertThat(reported.validated(" ")).isEqualTo("[charSequence.notBlank]");
		assertThat(reported.validated("yavi")).isEqualTo("yavi");
		// the mismatch of an untyped input is a failure like any other, and the input is
		// transformed before it is validated
		assertThat(name.recover("anonymous").untyped().validated(1)).isEqualTo("anonymous");
		assertThat(name.recover("anonymous").preprocess(String::strip).untyped().validated(" yavi ")).isEqualTo("yavi");
	}

	@Test
	void inputIsTransformedBeforeItIsConstrained() {
		// a string is transformed where its constraints are written, in the order of
		// writing
		final StringValidator<String> code = Yavi._string("code",
				c -> c.trim().toUpperCase().transform(s -> s.replace("-", "")).notBlank().lessThanOrEqual(4));
		assertThat(code.validated(" ab-cd\u3000")).isEqualTo("ABCD");
		assertThat(code.validated("\u0041\u030a")).isEqualTo("\u0041\u030a");
		assertThat(Yavi._string(c -> c.toNormalForm(Normalizer.Form.NFC).fixedSize(1)).validated("\u0041\u030a"))
			.isEqualTo("\u00c5");
		assertThat(Yavi._string(c -> c.toLowerCase()).map(String::length).validated("I")).isEqualTo(1);
		// null is not transformed, the violated value is the transformed one
		assertThat(code.validate(null).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("charSequence.notBlank");
		assertThat(Yavi._string(c -> c.trim().email()).validate(" a ").errors())
			.extracting(ConstraintViolation::violatedValue)
			.containsExactly("a");
		// the transform is kept by the combinators, with the description and the input
		// type: an untyped input is transformed after its type is checked
		final ValueValidator<String, Optional<Integer>> length = code.map(String::length)
			.constraintOnTarget(n -> n > 1, "code", ViolationMessage.of("code.length", "too short"))
			.prefixed("item")
			.optional();
		assertThat(length.validated(" a-b ")).contains(2);
		assertThat(length.description()).isEqualTo(new ValueDescription.Optional(ValueDescription.Value.of(String.class,
				ValidatorBuilder.<Arguments1<String>>of()
					._string(Arguments1::arg1, "code", c -> c.notBlank().lessThanOrEqual(4))
					.build()),
				null));
		assertThat(length.untyped().validated(" a-b ")).contains(2);
		assertThat(length.untyped().validate(1).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.type");

		// any other type is transformed with preprocess; the function added last is
		// applied first
		final IntegerValidator<Integer> percent = Yavi._integer("percent", c -> c.lessThanOrEqual(100))
			.preprocess(i -> i * 10)
			.preprocess(Math::abs);
		assertThat(percent.validated(-5)).isEqualTo(50);
		// null is not transformed: it is rejected, or accepted as it is
		assertThat(percent.validate(null).errors()).extracting(ConstraintViolation::messageKey)
			.containsExactly("object.notNull");
		assertThat(percent.nullable().validated(null)).isNull();
		assertThat(percent.validate(-50).errors()).extracting(ConstraintViolation::violatedValue).containsExactly(500);
		assertThat(percent.map(String::valueOf).nullable().preprocess(i -> i + 1).untyped().validated(4.0))
			.isEqualTo("50");
		assertThat(code.preprocess(s -> s + "-x").validated("ab ")).isEqualTo("AB X");

		// the input type of a lambda stays unknown
		final ValueValidator<String, String> lambda = (s, locale, context) -> Validated.successWith(s == null ? "" : s);
		final ValueValidator<String, String> stripped = lambda.describedAs(code.description())
			.preprocess(String::strip);
		assertThat(stripped.validated(" a ")).isEqualTo("a");
		assertThat(stripped.description()).isEqualTo(code.description());
		assertThatThrownBy(stripped::untyped).isInstanceOf(UnsupportedOperationException.class);
		assertThat(stripped.untyped(String.class).validated(" a ")).isEqualTo("a");
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
