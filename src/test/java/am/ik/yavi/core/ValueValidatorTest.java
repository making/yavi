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
import org.junit.jupiter.api.Test;

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
	void validatable() {
		ConstraintGroup group = ConstraintGroup.of("JP");
		ValueValidator<String, String> passThrough = ValueValidator.passThrough();
		ValueValidator<String, String> validator = ValidatorBuilder.<String> of()
				.constraintOnCondition(
						(String address,
								ConstraintGroup g) -> !"JP".equalsIgnoreCase(g.name()),
						passThrough)
				.constraintOnGroup(group,
						PhoneNumber.validator().applicative().compose(PhoneNumber::new))
				.build().applicative();
		assertThat(validator.validate("1234", group).isValid()).isFalse();
		assertThat(validator.validate("1234").isValid()).isTrue();
	}
}
