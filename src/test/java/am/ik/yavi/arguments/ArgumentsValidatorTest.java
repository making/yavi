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
package am.ik.yavi.arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.Range;
import am.ik.yavi.User;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ViolationMessage;
import org.junit.jupiter.api.Test;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgumentsValidatorTest {

	final Arguments1Validator<String, Country> arguments1Validator = ArgumentsValidatorBuilder
			.of(Country::new) //
			.builder(b -> b //
					._string(Arguments1::arg1, "name", c -> c.greaterThanOrEqual(2)))
			.build();

	final Arguments2Validator<Integer, Integer, Range> arguments2Validator = ArgumentsValidatorBuilder
			.of(Range::new)
			.builder(b -> b
					._integer(Arguments1::arg1, "from",
							c -> c.greaterThanOrEqual(0).lessThanOrEqual(9))
					._integer(Arguments2::arg2, "to",
							c -> c.greaterThanOrEqual(0).lessThanOrEqual(9))
					.constraintOnTarget(a -> a.arg1() < a.arg2(), "range",
							ViolationMessage.of("to.isGreaterThanFrom",
									"\"to\" must be greater than \"from\".")))
			.build();

	final Arguments3Validator<String, String, Integer, User> arguments3Validator = ArgumentsValidatorBuilder
			.of(User::new)
			.builder(b -> b
					._string(Arguments1::arg1, "name",
							c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
					._string(Arguments2::arg2, "email",
							c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50)
									.email())
					._integer(Arguments3::arg3, "age",
							c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200)))
			.build();

	final Arguments1Validator<Map<String, Object>, User> mapValidator = arguments3Validator
			.compose(m -> Arguments.of((String) m.get("name"), (String) m.get("email"),
					(Integer) m.get("age")));

	static final Arguments1Validator<String, PhoneNumber> phoneNumberValidator = StringValidatorBuilder
			.of("phoneNumber",
					c -> c.notBlank().greaterThanOrEqual(8).lessThanOrEqual(16))
			.build(PhoneNumber::new).compose(identity());

	@Test
	void testArg2_allInvalid() {
		assertThatThrownBy(() -> arguments2Validator.validated(-1, -3))
				.isInstanceOfSatisfying(ConstraintViolationsException.class,
						e -> assertThat(e.getMessage()).isEqualTo(
								"Constraint violations found!" + System.lineSeparator()
										+ "* \"from\" must be greater than or equal to 0"
										+ System.lineSeparator()
										+ "* \"to\" must be greater than or equal to 0"
										+ System.lineSeparator()
										+ "* \"to\" must be greater than \"from\"."));
	}

	@Test
	void testArg2_valid() {
		final Range range = arguments2Validator.validated(3, 5);
		assertThat(range.getFrom()).isEqualTo(3);
		assertThat(range.getTo()).isEqualTo(5);
	}

	@Test
	void testArg3_allInvalid() {
		assertThatThrownBy(() -> arguments3Validator.validated("", "example.com", 300))
				.isInstanceOfSatisfying(ConstraintViolationsException.class, e -> {
					final ConstraintViolations violations = e.violations();
					assertThat(violations.isValid()).isFalse();
					assertThat(violations.size()).isEqualTo(3);
					assertThat(violations.get(0).message()).isEqualTo(
							"The size of \"name\" must be greater than or equal to 1. The given size is 0");
					assertThat(violations.get(0).messageKey())
							.isEqualTo("container.greaterThanOrEqual");
					assertThat(violations.get(1).message())
							.isEqualTo("\"email\" must be a valid email address");
					assertThat(violations.get(1).messageKey())
							.isEqualTo("charSequence.email");
					assertThat(violations.get(2).message())
							.isEqualTo("\"age\" must be less than or equal to 200");
					assertThat(violations.get(2).messageKey())
							.isEqualTo("numeric.lessThanOrEqual");
				});
	}

	@Test
	void testArg3_either_allInvalid() {
		final Validated<User> either = arguments3Validator.validate("", "example.com",
				300);
		assertThat(either.isValid()).isFalse();
		final ConstraintViolations violations = either.errors();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void testArg3_either_valid() {
		final Validated<User> either = arguments3Validator.validate("foo",
				"foo@example.com", 30);
		assertThat(either.isValid()).isTrue();
		final User user = either.value();
		assertThat(user.getName()).isEqualTo("foo");
		assertThat(user.getEmail()).isEqualTo("foo@example.com");
		assertThat(user.getAge()).isEqualTo(30);
	}

	@Test
	void testArg3_valid() {
		final User user = arguments3Validator.validated("foo", "foo@example.com", 30);
		assertThat(user.getName()).isEqualTo("foo");
		assertThat(user.getEmail()).isEqualTo("foo@example.com");
		assertThat(user.getAge()).isEqualTo(30);
	}

	@Test
	void testArg_invalid() {
		assertThatThrownBy(() -> arguments1Validator.validated("J"))
				.isInstanceOfSatisfying(ConstraintViolationsException.class,
						e -> assertThat(e.getMessage()).isEqualTo(
								"Constraint violations found!" + System.lineSeparator()
										+ "* The size of \"name\" must be greater than or equal to 2. The given size is 1"));
	}

	@Test
	void testArg_valid() {
		final Country country = arguments1Validator.validated("JP");
		assertThat(country.name()).isEqualTo("JP");
	}

	@Test
	void testValidateOnly_valid() {
		final Product product = new Product("foo", 100);
		assertThat(product).isNotNull();
	}

	@Test
	void testValidateOnly_invalid() {
		assertThatThrownBy(() -> new Product("", 0)) //
				.isInstanceOfSatisfying(ConstraintViolationsException.class,
						e -> assertThat(e.getMessage()).isEqualTo(
								"Constraint violations found!" + System.lineSeparator()
										+ "* \"name\" must not be empty"
										+ System.lineSeparator()
										+ "* \"price\" must be greater than 0"));
	}

	@Test
	void contramap_allInvalid() {
		final Map<String, Object> map = new HashMap<>();
		map.put("name", "");
		map.put("email", "example.com");
		map.put("age", 300);
		final Validated<User> validated = mapValidator.validate(map);
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
	}

	@Test
	void contramap_valid() {
		final Map<String, Object> map = new HashMap<>();
		map.put("name", "foo");
		map.put("email", "foo@example.com");
		map.put("age", 30);
		final Validated<User> validated = mapValidator.validate(map);
		assertThat(validated.isValid()).isTrue();
		final User user = validated.value();
		assertThat(user.getName()).isEqualTo("foo");
		assertThat(user.getEmail()).isEqualTo("foo@example.com");
		assertThat(user.getAge()).isEqualTo(30);
	}

	@Test
	void fromValidatorSubset_valid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator
				.from(Country.validator().prefixed("country"));
		final Validated<Country> countryValidated = countryValidator
				.validate(new Country("JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@Test
	void fromValidatorSubset_invalid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator
				.from(Country.validator().prefixed("country"));
		final Validated<Country> countryValidated = countryValidator
				.validate(new Country("J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}

	@Test
	void fromValueValidator_valid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator
				.from(Country.validator().prefixed("country").applicative());
		final Validated<Country> countryValidated = countryValidator
				.validate(new Country("JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@Test
	void fromValueValidator_invalid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator
				.from(Country.validator().prefixed("country").applicative());
		final Validated<Country> countryValidated = countryValidator
				.validate(new Country("J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}

	@Test
	void liftListValid() {
		Arguments1Validator<Iterable<String>, List<PhoneNumber>> phoneNumberListValidator = phoneNumberValidator
				.liftList();
		List<String> input = Arrays.asList("012012345678", "012012348765",
				"012012345679");
		Validated<List<PhoneNumber>> actual = phoneNumberListValidator.validate(input);

		assertThat(actual.isValid()).isTrue();
		assertThat(actual.value()).isEqualTo(Arrays.asList(
				new PhoneNumber("012012345678"), new PhoneNumber("012012348765"),
				new PhoneNumber("012012345679")));
	}

	@Test
	void liftListInvalid() {
		Arguments1Validator<Iterable<String>, List<PhoneNumber>> phoneNumberListValidator = phoneNumberValidator
				.liftList();
		List<String> input = Arrays.asList("012012345678", "", "012");
		Validated<List<PhoneNumber>> actual = phoneNumberListValidator.validate(input);

		assertThat(actual.isValid()).isFalse();
		assertThat(actual.errors()).hasSize(3);
		assertThat(actual.errors().get(0).name()).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(0).messageKey())
				.isEqualTo("charSequence.notBlank");
		assertThat(actual.errors().get(0).args()[0]).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(1).name()).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(1).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(actual.errors().get(1).args()[0]).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(2).name()).isEqualTo("phoneNumber[2]");
		assertThat(actual.errors().get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(actual.errors().get(2).args()[0]).isEqualTo("phoneNumber[2]");
	}

	@Test
	void liftSetValid() {
		Arguments1Validator<Iterable<String>, Set<PhoneNumber>> phoneNumberSetValidator = phoneNumberValidator
				.liftSet();
		List<String> input = Arrays.asList("012012345678", "012012348765", "012012345679",
				"012012345678");
		Validated<Set<PhoneNumber>> actual = phoneNumberSetValidator.validate(input);

		assertThat(actual.isValid()).isTrue();
		assertThat(new ArrayList<>(actual.value())).isEqualTo(Arrays.asList(
				new PhoneNumber("012012345678"), new PhoneNumber("012012348765"),
				new PhoneNumber("012012345679")));
	}

	@Test
	void liftSetInvalid() {
		Arguments1Validator<Iterable<String>, Set<PhoneNumber>> phoneNumberSetValidator = phoneNumberValidator
				.liftSet();
		List<String> input = Arrays.asList("012012345678", "", "012", "012012345678");
		Validated<Set<PhoneNumber>> actual = phoneNumberSetValidator.validate(input);

		assertThat(actual.isValid()).isFalse();
		assertThat(actual.errors()).hasSize(3);
		assertThat(actual.errors().get(0).name()).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(0).messageKey())
				.isEqualTo("charSequence.notBlank");
		assertThat(actual.errors().get(0).args()[0]).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(1).name()).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(1).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(actual.errors().get(1).args()[0]).isEqualTo("phoneNumber[1]");
		assertThat(actual.errors().get(2).name()).isEqualTo("phoneNumber[2]");
		assertThat(actual.errors().get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(actual.errors().get(2).args()[0]).isEqualTo("phoneNumber[2]");
	}

	@Test
	void liftOptionalValid() {
		Arguments1Validator<Optional<String>, Optional<PhoneNumber>> phoneNumberOptionalValidator = phoneNumberValidator
				.liftOptional();

		Validated<Optional<PhoneNumber>> actual = phoneNumberOptionalValidator
				.validate(Optional.of("012012345678"));
		assertThat(actual.isValid()).isTrue();
		assertThat(actual.value())
				.isEqualTo(Optional.of(new PhoneNumber("012012345678")));

		Validated<Optional<PhoneNumber>> actual2 = phoneNumberOptionalValidator
				.validate(Optional.empty());
		assertThat(actual2.isValid()).isTrue();
		assertThat(actual2.value()).isEqualTo(Optional.empty());
	}

	@Test
	void liftOptionalInvalid() {
		Arguments1Validator<Optional<String>, Optional<PhoneNumber>> phoneNumberOptionalValidator = phoneNumberValidator
				.liftOptional();

		Validated<Optional<PhoneNumber>> actual = phoneNumberOptionalValidator
				.validate(Optional.of(""));
		assertThat(actual.isValid()).isFalse();
		assertThat(actual.errors()).hasSize(2);
		assertThat(actual.errors().get(0).name()).isEqualTo("phoneNumber");
		assertThat(actual.errors().get(0).messageKey())
				.isEqualTo("charSequence.notBlank");
		assertThat(actual.errors().get(0).args()[0]).isEqualTo("phoneNumber");
		assertThat(actual.errors().get(1).name()).isEqualTo("phoneNumber");
		assertThat(actual.errors().get(1).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(actual.errors().get(1).args()[0]).isEqualTo("phoneNumber");
	}

	@Test
	void nullArgValid() {
		final Arguments1Validator<String, Country> validator = ArgumentsValidatorBuilder
				.of(Country::new).builder(b -> b._string(Arguments1::arg1, "country",
						c -> c.greaterThan(1)))
				.build();
		final Validated<Country> validated = validator.validate(null);
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isNotNull();
		assertThat(validated.value().name()).isNull();
	}

	@Test
	void nullArgInValid() {
		final Arguments1Validator<String, Country> validator = ArgumentsValidatorBuilder
				.of(Country::new).builder(b -> b._string(Arguments1::arg1, "country",
						c -> c.notNull().greaterThan(1)))
				.build();
		final Validated<Country> validated = validator.validate(null);
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).name()).isEqualTo("country");
		assertThat(validated.errors().get(0).messageKey()).isEqualTo("object.notNull");
		assertThat(validated.errors().get(0).args()[0]).isEqualTo("country");
	}
}
