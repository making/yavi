/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
import java.util.stream.Stream;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArgumentsValidatorsTest {
	static final StringValidator<Country> countryValidator = StringValidatorBuilder
			.of("country", c -> c.notBlank().greaterThanOrEqual(2)).build(Country::new);

	static final StringValidator<String> streetValidator = StringValidatorBuilder
			.of("street", c -> c.notBlank()).build();

	static final StringValidator<PhoneNumber> phoneNumberValidator = StringValidatorBuilder
			.of("phoneNumber",
					c -> c.notBlank().greaterThanOrEqual(8).lessThanOrEqual(16))
			.build(PhoneNumber::new);

	static final Arguments1Validator<Map<String, String>, Country> mapCountryValidator = countryValidator
			.compose(map -> map.get("country"));

	static final Arguments1Validator<Map<String, String>, String> mapStreetValidator = streetValidator
			.compose(map -> map.get("street"));

	static final Arguments1Validator<Map<String, String>, PhoneNumber> mapPhoneNumberValidator = phoneNumberValidator
			.compose(map -> map.get("phoneNumber"));

	static final StringValidator<String> v1 = StringValidatorBuilder
			.of("s1", c -> c.notBlank()).build();

	static final StringValidator<String> v2 = StringValidatorBuilder
			.of("s2", c -> c.notBlank()).build();

	static final StringValidator<String> v3 = StringValidatorBuilder
			.of("s3", c -> c.notBlank()).build();

	static final StringValidator<String> v4 = StringValidatorBuilder
			.of("s4", c -> c.notBlank()).build();

	static final StringValidator<String> v5 = StringValidatorBuilder
			.of("s5", c -> c.notBlank()).build();

	static final StringValidator<String> v6 = StringValidatorBuilder
			.of("s6", c -> c.notBlank()).build();

	static final StringValidator<String> v7 = StringValidatorBuilder
			.of("s7", c -> c.notBlank()).build();

	static final StringValidator<String> v8 = StringValidatorBuilder
			.of("s8", c -> c.notBlank()).build();

	static final StringValidator<String> v9 = StringValidatorBuilder
			.of("s9", c -> c.notBlank()).build();

	static final StringValidator<String> v10 = StringValidatorBuilder
			.of("s10", c -> c.notBlank()).build();

	static final Arguments10Validator<String, String, String, String, String, String, String, String, String, String, List<String>> arguments10Validator = v1
			.split(v2).split(v3).split(v4).split(v5).split(v6).split(v7).split(v8)
			.split(v9).split(v10).apply(Arrays::asList);

	final Arguments1Validator<Map<String, String>, String> mapV1 = v1
			.compose(map -> map.get("s1"));

	final Arguments1Validator<Map<String, String>, String> mapV2 = v2
			.compose(map -> map.get("s2"));

	final Arguments1Validator<Map<String, String>, String> mapV3 = v3
			.compose(map -> map.get("s3"));

	final Arguments1Validator<Map<String, String>, String> mapV4 = v4
			.compose(map -> map.get("s4"));

	final Arguments1Validator<Map<String, String>, String> mapV5 = v5
			.compose(map -> map.get("s5"));

	final Arguments1Validator<Map<String, String>, String> mapV6 = v6
			.compose(map -> map.get("s6"));

	final Arguments1Validator<Map<String, String>, String> mapV7 = v7
			.compose(map -> map.get("s7"));

	final Arguments1Validator<Map<String, String>, String> mapV8 = v8
			.compose(map -> map.get("s8"));

	final Arguments1Validator<Map<String, String>, String> mapV9 = v9
			.compose(map -> map.get("s9"));

	final Arguments1Validator<Map<String, String>, String> mapV10 = v10
			.compose(map -> map.get("s10"));

	final Arguments1Validator<Map<String, String>, List<String>> map10V = mapV1
			.combine(mapV2).combine(mapV3).combine(mapV4).combine(mapV5).combine(mapV6)
			.combine(mapV7).combine(mapV8).combine(mapV9).combine(mapV10)
			.apply(Arrays::asList);

	@ParameterizedTest
	@MethodSource("splitValidators")
	void splitValid(Arguments3Validator<String, String, String, Address> validator) {

		final Validated<Address> validated = validator.validate("JP", "XYZ Avenue",
				"123-456-789");
		assertThat(validated.isValid()).isTrue();
		final Address address = validated.value();
		assertThat(address.country().name()).isEqualTo("JP");
		assertThat(address.street()).isEqualTo("XYZ Avenue");
		assertThat(address.phoneNumber().value()).isEqualTo("123-456-789");
	}

	@ParameterizedTest
	@MethodSource("splitValidators")
	void splitInvalid(Arguments3Validator<String, String, String, Address> validator) {
		final Validated<Address> validated = validator.validate("J", " ", "1234567");
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("street");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("phoneNumber");
		assertThat(violations.get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void split10Valid() {
		final Validated<List<String>> validated = arguments10Validator.validate("1", "2",
				"3", "4", "5", "6", "7", "8", "9", "10");
		assertThat(validated.isValid()).isTrue();
		final List<String> list = validated.value();
		assertThat(list).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10");
	}

	@Test
	void split10Invalid() {
		final Validated<List<String>> validated = arguments10Validator.validate(" ", " ",
				" ", " ", " ", " ", " ", " ", " ", " ");
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(10);
		for (int i = 0; i < 10; i++) {
			assertThat(violations.get(i).name()).isEqualTo("s" + (i + 1));
			assertThat(violations.get(i).messageKey()).isEqualTo("charSequence.notBlank");
		}
	}

	@ParameterizedTest
	@MethodSource("combineValidators")
	void combineValid(Arguments1Validator<Map<String, String>, Address> validator) {
		final Validated<Address> validated = validator
				.validate(new HashMap<String, String>() {
					{
						put("country", "JP");
						put("street", "XYZ Avenue");
						put("phoneNumber", "123-456-789");
					}
				});
		assertThat(validated.isValid()).isTrue();
		final Address address = validated.value();
		assertThat(address.country().name()).isEqualTo("JP");
		assertThat(address.street()).isEqualTo("XYZ Avenue");
		assertThat(address.phoneNumber().value()).isEqualTo("123-456-789");
	}

	@ParameterizedTest
	@MethodSource("combineValidators")
	void combineInvalid(Arguments1Validator<Map<String, String>, Address> validator) {
		final Validated<Address> validated = validator
				.validate(new HashMap<String, String>() {
					{
						put("country", "J");
						put("street", " ");
						put("phoneNumber", "1234567");
					}
				});
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("country");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("street");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("phoneNumber");
		assertThat(violations.get(2).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void combine10Valid() {
		final Validated<List<String>> validated = map10V
				.validate(new HashMap<String, String>() {
					{
						for (int i = 1; i <= 10; i++) {
							put("s" + i, String.valueOf(i));
						}
					}
				});
		assertThat(validated.isValid()).isTrue();
		final List<String> list = validated.value();
		assertThat(list).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10");
	}

	@Test
	void combine10Invalid() {
		final Validated<List<String>> validated = map10V
				.validate(new HashMap<String, String>() {
					{
						for (int i = 1; i <= 10; i++) {
							put("s" + i, " ");
						}
					}
				});
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(10);
		for (int i = 0; i < 10; i++) {
			assertThat(violations.get(i).name()).isEqualTo("s" + (i + 1));
			assertThat(violations.get(i).messageKey()).isEqualTo("charSequence.notBlank");
		}
	}

	static Stream<Arguments3Validator<String, String, String, Address>> splitValidators() {
		return Stream.of(
				ArgumentsValidators
						.split(countryValidator, streetValidator, phoneNumberValidator)
						.apply(Address::new),
				countryValidator.split(streetValidator).split(phoneNumberValidator)
						.apply(Address::new));
	}

	static Stream<Arguments1Validator<Map<String, String>, Address>> combineValidators() {
		return Stream.of(
				ArgumentsValidators.combine(mapCountryValidator, mapStreetValidator,
						mapPhoneNumberValidator).apply(Address::new),
				mapCountryValidator.combine(mapStreetValidator)
						.combine(mapPhoneNumberValidator).apply(Address::new));
	}

	@Test
	void sequence1Valid() {
		final Arguments1Validator<Map<String, String>, List<String>> sequenced = ArgumentsValidators
				.sequence1(Arrays.asList(mapV1, mapV2, mapV3, mapV4, mapV5, mapV6, mapV7,
						mapV8, mapV9, mapV10));

		final Validated<List<String>> validated = sequenced
				.validate(new HashMap<String, String>() {
					{
						for (int i = 1; i <= 10; i++) {
							put("s" + i, String.valueOf(i));
						}
					}
				});
		assertThat(validated.isValid()).isTrue();
		final List<String> list = validated.value();
		assertThat(list).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9",
				"10");
	}

	@Test
	void sequence1Invalid() {
		final Arguments1Validator<Map<String, String>, List<String>> sequenced = ArgumentsValidators
				.sequence1(Arrays.asList(mapV1, mapV2, mapV3, mapV4, mapV5, mapV6, mapV7,
						mapV8, mapV9, mapV10));

		final Validated<List<String>> validated = sequenced
				.validate(new HashMap<String, String>() {
					{
						for (int i = 1; i <= 10; i++) {
							put("s" + i, " ");
						}
					}
				});
		assertThat(validated.isValid()).isFalse();
		final ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(10);
		for (int i = 0; i < 10; i++) {
			assertThat(violations.get(i).name()).isEqualTo("s" + (i + 1));
			assertThat(violations.get(i).messageKey()).isEqualTo("charSequence.notBlank");
		}
	}

	static Stream<Arguments1Validator<Iterable<String>, List<PhoneNumber>>> phoneNumberListValidators() {
		return Stream.of(ArgumentsValidators.liftList(phoneNumberValidator),
				phoneNumberValidator.liftList());
	}

	@ParameterizedTest
	@MethodSource("phoneNumberListValidators")
	void liftListValid(
			Arguments1Validator<Iterable<String>, List<PhoneNumber>> phoneNumberListValidator) {
		List<String> input = Arrays.asList("012012345678", "012012348765",
				"012012345679");
		Validated<List<PhoneNumber>> actual = phoneNumberListValidator.validate(input);

		assertThat(actual.isValid()).isTrue();
		assertThat(actual.value()).isEqualTo(Arrays.asList(
				new PhoneNumber("012012345678"), new PhoneNumber("012012348765"),
				new PhoneNumber("012012345679")));
	}

	@ParameterizedTest
	@MethodSource("phoneNumberListValidators")
	void liftListInvalid(
			Arguments1Validator<Iterable<String>, List<PhoneNumber>> phoneNumberListValidator) {
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

	static Stream<Arguments1Validator<Iterable<String>, Set<PhoneNumber>>> phoneNumberSetValidators() {
		return Stream.of(ArgumentsValidators.liftSet(phoneNumberValidator),
				phoneNumberValidator.liftSet());
	}

	@ParameterizedTest
	@MethodSource("phoneNumberSetValidators")
	void liftSetValid(
			Arguments1Validator<Iterable<String>, Set<PhoneNumber>> phoneNumberSetValidator) {
		List<String> input = Arrays.asList("012012345678", "012012348765", "012012345679",
				"012012345678");
		Validated<Set<PhoneNumber>> actual = phoneNumberSetValidator.validate(input);

		assertThat(actual.isValid()).isTrue();
		assertThat(new ArrayList<>(actual.value())).isEqualTo(Arrays.asList(
				new PhoneNumber("012012345678"), new PhoneNumber("012012348765"),
				new PhoneNumber("012012345679")));
	}

	@ParameterizedTest
	@MethodSource("phoneNumberSetValidators")
	void liftSetInvalid(
			Arguments1Validator<Iterable<String>, Set<PhoneNumber>> phoneNumberSetValidator) {
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

	static Stream<Arguments1Validator<Optional<String>, Optional<PhoneNumber>>> phoneNumberOptionalValidators() {
		return Stream.of(ArgumentsValidators.liftOptional(phoneNumberValidator),
				phoneNumberValidator.liftOptional());
	}

	@ParameterizedTest
	@MethodSource("phoneNumberOptionalValidators")
	void liftOptionalValid(
			Arguments1Validator<Optional<String>, Optional<PhoneNumber>> phoneNumberOptionalValidator) {
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

	@ParameterizedTest
	@MethodSource("phoneNumberOptionalValidators")
	void liftOptionalInvalid(
			Arguments1Validator<Optional<String>, Optional<PhoneNumber>> phoneNumberOptionalValidator) {
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

}