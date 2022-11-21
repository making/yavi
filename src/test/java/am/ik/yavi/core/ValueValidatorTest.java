/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.arguments.Arguments1Validator;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

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
		ValueValidator<String, Country> countryValidator = Country.validator()
				.applicative().compose(Country::new);
		ValueValidator<String, String> streetValidator = ValueValidator.passThrough();
		ValueValidator<String, PhoneNumber> phoneValidator = PhoneNumber.validator()
				.applicative().compose(PhoneNumber::new);

		Arguments3Validator<String, String, String, Address> addressValidator = Arguments1Validator
				.from(countryValidator).split(streetValidator).split(phoneValidator)
				.apply(Address::new);

		ValueValidator<Address, Address> foreignAddressValidator = ValidatorBuilder
				.<Address> of()
				.constraintOnCondition(
						(address,
								group) -> !"JP"
										.equalsIgnoreCase(address.country().name()),
						ValidatorBuilder.<Address> of()
								._string(a -> a.phoneNumber().value(), "PhoneNumber",
										c -> c.startsWith("+"))
								.build())
				.build().applicative();

		assertThat(addressValidator.validate("JP", "tokyo", "0123456789").isValid())
				.isTrue();
		assertThat(addressValidator.andThen(foreignAddressValidator)
				.validate("JP", "tokyo", "0123456789").isValid()).isTrue();
		assertThat(addressValidator.validate("BE", "brussels", "9876543210").isValid())
				.isTrue();
		assertThat(addressValidator.andThen(foreignAddressValidator)
				.validate("BE", "brussels", "9876543210").isValid()).isFalse();
		assertThat(addressValidator.validate("J", "tokyo", "0123456789").isValid())
				.isFalse();
		assertThat(addressValidator.andThen(foreignAddressValidator)
				.validate("J", "tokyo", "+0123456789").isValid()).isFalse();
	}

	@Test
	void invalidPatternStringToLocalDateValidator() {
		ValueValidator<String, LocalDate> localDateValidator
				= ValidatorBuilder.<String>of()._string(f -> f, "myLocalDate", CharSequenceConstraint::isIsoLocalDate)
				.build().applicative().andThen(LocalDate::parse);
		final Validated<LocalDate> localDateValidated = localDateValidator.validate("31/01/2022");
		Assertions.assertThat(localDateValidated.isValid()).isFalse();
		Assertions.assertThat(localDateValidated.errors()).hasSize(1);
		Assertions.assertThat(localDateValidated.errors().get(0).messageKey()).isEqualTo("charSequence.localdate");
		Assertions.assertThat(localDateValidated.errors().get(0).message()).isEqualTo("\"myLocalDate\" must be a valid representation of a local date using the pattern: uuuu-MM-dd. The give value is: 31/01/2022");
	}

	@Test
	void validStringToLocalDateValidator() {
		ValueValidator<String, LocalDate> localDateValidator
				= ValidatorBuilder.<String>of()._string(f -> f, "myLocalDate", c -> c.isLocalDate("dd/MM/yyyy"))
				.build().applicative().andThen(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
		final Validated<LocalDate> localDateValidated = localDateValidator.validate("31/01/2022");
		Assertions.assertThat(localDateValidated.isValid()).isTrue();
		Assertions.assertThat(localDateValidated.value()).isEqualTo(LocalDate.of(2022,1,31));
	}
}
