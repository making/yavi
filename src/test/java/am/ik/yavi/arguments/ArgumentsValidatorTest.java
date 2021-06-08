/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.util.HashMap;
import java.util.Map;

import am.ik.yavi.Country;
import am.ik.yavi.Range;
import am.ik.yavi.User;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import am.ik.yavi.core.ViolationMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArgumentsValidatorTest {

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
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator.from(Country.validator().prefixed("country"));
		final Validated<Country> countryValidated = countryValidator.validate(new Country("JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@Test
	void fromValidatorSubset_invalid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator.from(Country.validator().prefixed("country"));
		final Validated<Country> countryValidated = countryValidator.validate(new Country("J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}

	@Test
	void fromValueValidator_valid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator.from(Country.validator().prefixed("country").applicative());
		final Validated<Country> countryValidated = countryValidator.validate(new Country("JP"));
		assertThat(countryValidated.isValid()).isTrue();
		assertThat(countryValidated.value().name()).isEqualTo("JP");
	}

	@Test
	void fromValueValidator_invalid() {
		final Arguments1Validator<Country, Country> countryValidator = Arguments1Validator.from(Country.validator().prefixed("country").applicative());
		final Validated<Country> countryValidated = countryValidator.validate(new Country("J"));
		assertThat(countryValidated.isValid()).isFalse();
		final ConstraintViolations violations = countryValidated.errors();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(0).name()).isEqualTo("country.name");
	}
}
