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
package am.ik.yavi.meta;

import java.math.BigDecimal;
import java.math.BigInteger;

import am.ik.yavi.PhoneNumber;
import am.ik.yavi.arguments.Arguments3Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintContext;
import am.ik.yavi.core.ConstraintGroup;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validator;
import am.ik.yavi.core.ValueValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ValidatorBuilderTest {

	@Test
	void allTypesBeanMeta() {
		final Validator<AllTypesBean> validator = ValidatorBuilder.<AllTypesBean> of()
				.constraint(_AllTypesBeanMeta.BIGDECIMALVALUE,
						c -> c.greaterThan(BigDecimal.ZERO))
				.constraint(_AllTypesBeanMeta.BIGINTEGERVALUE,
						c -> c.greaterThan(BigInteger.ZERO))
				.constraint(_AllTypesBeanMeta.BOOLEANPRIMITIVEVALUE, c -> c.isTrue())
				.constraint(_AllTypesBeanMeta.BOOLEANVALUE, c -> c.isTrue())
				.constraint(_AllTypesBeanMeta.BYTEPRIMITIVEVALUE,
						c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesBeanMeta.BYTEVALUE, c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesBeanMeta.CHARACTERPRIMITIVEVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesBeanMeta.CHARACTERVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesBeanMeta.DOUBLEPRIMITIVEVALUE,
						c -> c.greaterThan(0.0))
				.constraint(_AllTypesBeanMeta.DOUBLEVALUE, c -> c.greaterThan(0.0))
				.constraint(_AllTypesBeanMeta.FLOATPRIMITIVEVALUE,
						c -> c.greaterThan(0.0f))
				.constraint(_AllTypesBeanMeta.FLOATVALUE, c -> c.greaterThan(0.0f))
				.constraint(_AllTypesBeanMeta.INTEGERPRIMITIVEVALUE,
						c -> c.greaterThan(0))
				.constraint(_AllTypesBeanMeta.INTEGERVALUE, c -> c.greaterThan(0))
				.constraint(_AllTypesBeanMeta.LOCALDATEVALUE, c -> c.notNull())
				.constraint(_AllTypesBeanMeta.LONGPRIMITIVEVALUE,
						c -> c.greaterThan((long) 0))
				.constraint(_AllTypesBeanMeta.LONGVALUE, c -> c.greaterThan((long) 0))
				.constraint(_AllTypesBeanMeta.SHORTPRIMITIVEVALUE,
						c -> c.greaterThan((short) 0))
				.constraint(_AllTypesBeanMeta.SHORTVALUE, c -> c.greaterThan((short) 0))
				.constraint(_AllTypesBeanMeta.STRINGVALUE, c -> c.notEmpty())
				//
				.build();

		final AllTypesBean inValidTarget = new AllTypesBean();
		inValidTarget.setBigDecimalValue(BigDecimal.ZERO);
		inValidTarget.setBigIntegerValue(BigInteger.ZERO);
		inValidTarget.setBooleanPrimitiveValue(false);
		inValidTarget.setBooleanValue(false);
		inValidTarget.setBytePrimitiveValue((byte) 0);
		inValidTarget.setByteValue((byte) 0);
		inValidTarget.setCharacterPrimitiveValue((char) 0);
		inValidTarget.setCharacterValue((char) 0);
		inValidTarget.setDoublePrimitiveValue(0.0);
		inValidTarget.setDoubleValue(0.0);
		inValidTarget.setFloatPrimitiveValue(0.0f);
		inValidTarget.setFloatValue(0.0f);
		inValidTarget.setIntegerPrimitiveValue(0);
		inValidTarget.setIntegerValue(0);
		inValidTarget.setLocalDateValue(null);
		inValidTarget.setLongPrimitiveValue(0);
		inValidTarget.setLongValue((long) 0);
		inValidTarget.setShortPrimitiveValue((short) 0);
		inValidTarget.setShortValue((short) 0);
		inValidTarget.setStringValue("");

		final ConstraintViolations constraintViolations = validator
				.validate(inValidTarget);
		assertThat(constraintViolations.isValid()).isFalse();
		assertThat(constraintViolations.size()).isEqualTo(20);
		assertThat(constraintViolations.get(0).message())
				.isEqualTo("\"bigDecimalValue\" must be greater than 0");
		assertThat(constraintViolations.get(1).message())
				.isEqualTo("\"bigIntegerValue\" must be greater than 0");
		assertThat(constraintViolations.get(2).message())
				.isEqualTo("\"booleanPrimitiveValue\" must be true");
		assertThat(constraintViolations.get(3).message())
				.isEqualTo("\"booleanValue\" must be true");
		assertThat(constraintViolations.get(4).message())
				.isEqualTo("\"bytePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(5).message())
				.isEqualTo("\"byteValue\" must be greater than 0");
		assertThat(constraintViolations.get(6).message())
				.isEqualTo("\"characterPrimitiveValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(7).message())
				.isEqualTo("\"characterValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(8).message())
				.isEqualTo("\"doublePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(9).message())
				.isEqualTo("\"doubleValue\" must be greater than 0");
		assertThat(constraintViolations.get(10).message())
				.isEqualTo("\"floatPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(11).message())
				.isEqualTo("\"floatValue\" must be greater than 0");
		assertThat(constraintViolations.get(12).message())
				.isEqualTo("\"integerPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(13).message())
				.isEqualTo("\"integerValue\" must be greater than 0");
		assertThat(constraintViolations.get(14).message())
				.isEqualTo("\"localDateValue\" must not be null");
		assertThat(constraintViolations.get(15).message())
				.isEqualTo("\"longPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(16).message())
				.isEqualTo("\"longValue\" must be greater than 0");
		assertThat(constraintViolations.get(17).message())
				.isEqualTo("\"shortPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(18).message())
				.isEqualTo("\"shortValue\" must be greater than 0");
		assertThat(constraintViolations.get(19).message())
				.isEqualTo("\"stringValue\" must not be empty");
	}

	@Test
	void allTypesImmutableMeta() {
		final Validator<AllTypesImmutable> validator = ValidatorBuilder
				.<AllTypesImmutable> of()
				.constraint(_AllTypesImmutableMeta.BIGDECIMALVALUE,
						c -> c.greaterThan(BigDecimal.ZERO))
				.constraint(_AllTypesImmutableMeta.BIGINTEGERVALUE,
						c -> c.greaterThan(BigInteger.ZERO))
				.constraint(_AllTypesImmutableMeta.BOOLEANPRIMITIVEVALUE, c -> c.isTrue())
				.constraint(_AllTypesImmutableMeta.BOOLEANVALUE, c -> c.isTrue())
				.constraint(_AllTypesImmutableMeta.BYTEPRIMITIVEVALUE,
						c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesImmutableMeta.BYTEVALUE,
						c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesImmutableMeta.CHARACTERPRIMITIVEVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesImmutableMeta.CHARACTERVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesImmutableMeta.DOUBLEPRIMITIVEVALUE,
						c -> c.greaterThan(0.0))
				.constraint(_AllTypesImmutableMeta.DOUBLEVALUE, c -> c.greaterThan(0.0))
				.constraint(_AllTypesImmutableMeta.FLOATPRIMITIVEVALUE,
						c -> c.greaterThan(0.0f))
				.constraint(_AllTypesImmutableMeta.FLOATVALUE, c -> c.greaterThan(0.0f))
				.constraint(_AllTypesImmutableMeta.INTEGERPRIMITIVEVALUE,
						c -> c.greaterThan(0))
				.constraint(_AllTypesImmutableMeta.INTEGERVALUE, c -> c.greaterThan(0))
				.constraint(_AllTypesImmutableMeta.LOCALDATEVALUE, c -> c.notNull())
				.constraint(_AllTypesImmutableMeta.LONGPRIMITIVEVALUE,
						c -> c.greaterThan((long) 0))
				.constraint(_AllTypesImmutableMeta.LONGVALUE,
						c -> c.greaterThan((long) 0))
				.constraint(_AllTypesImmutableMeta.SHORTPRIMITIVEVALUE,
						c -> c.greaterThan((short) 0))
				.constraint(_AllTypesImmutableMeta.SHORTVALUE,
						c -> c.greaterThan((short) 0))
				.constraint(_AllTypesImmutableMeta.STRINGVALUE, c -> c.notEmpty())
				//
				.build();

		final AllTypesImmutable inValidTarget = new AllTypesImmutable("", false, false,
				(char) 0, (char) 0, (byte) 0, (byte) 0, (short) 0, (short) 0, 0, 0,
				(long) 0, 0, 0.0f, 0.0f, 0.0, 0.0, BigInteger.ZERO, BigDecimal.ZERO,
				null);

		final ConstraintViolations constraintViolations = validator
				.validate(inValidTarget);
		assertThat(constraintViolations.isValid()).isFalse();
		assertThat(constraintViolations.size()).isEqualTo(20);
		assertThat(constraintViolations.get(0).message())
				.isEqualTo("\"bigDecimalValue\" must be greater than 0");
		assertThat(constraintViolations.get(1).message())
				.isEqualTo("\"bigIntegerValue\" must be greater than 0");
		assertThat(constraintViolations.get(2).message())
				.isEqualTo("\"booleanPrimitiveValue\" must be true");
		assertThat(constraintViolations.get(3).message())
				.isEqualTo("\"booleanValue\" must be true");
		assertThat(constraintViolations.get(4).message())
				.isEqualTo("\"bytePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(5).message())
				.isEqualTo("\"byteValue\" must be greater than 0");
		assertThat(constraintViolations.get(6).message())
				.isEqualTo("\"characterPrimitiveValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(7).message())
				.isEqualTo("\"characterValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(8).message())
				.isEqualTo("\"doublePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(9).message())
				.isEqualTo("\"doubleValue\" must be greater than 0");
		assertThat(constraintViolations.get(10).message())
				.isEqualTo("\"floatPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(11).message())
				.isEqualTo("\"floatValue\" must be greater than 0");
		assertThat(constraintViolations.get(12).message())
				.isEqualTo("\"integerPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(13).message())
				.isEqualTo("\"integerValue\" must be greater than 0");
		assertThat(constraintViolations.get(14).message())
				.isEqualTo("\"localDateValue\" must not be null");
		assertThat(constraintViolations.get(15).message())
				.isEqualTo("\"longPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(16).message())
				.isEqualTo("\"longValue\" must be greater than 0");
		assertThat(constraintViolations.get(17).message())
				.isEqualTo("\"shortPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(18).message())
				.isEqualTo("\"shortValue\" must be greater than 0");
		assertThat(constraintViolations.get(19).message())
				.isEqualTo("\"stringValue\" must not be empty");
	}

	@Test
	void allTypesFieldMeta() {
		final Validator<AllTypesField> validator = ValidatorBuilder.<AllTypesField> of()
				.constraint(_AllTypesFieldMeta.BIGDECIMALVALUE,
						c -> c.greaterThan(BigDecimal.ZERO))
				.constraint(_AllTypesFieldMeta.BIGINTEGERVALUE,
						c -> c.greaterThan(BigInteger.ZERO))
				.constraint(_AllTypesFieldMeta.BOOLEANPRIMITIVEVALUE, c -> c.isTrue())
				.constraint(_AllTypesFieldMeta.BOOLEANVALUE, c -> c.isTrue())
				.constraint(_AllTypesFieldMeta.BYTEPRIMITIVEVALUE,
						c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesFieldMeta.BYTEVALUE, c -> c.greaterThan((byte) 0))
				.constraint(_AllTypesFieldMeta.CHARACTERPRIMITIVEVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesFieldMeta.CHARACTERVALUE,
						c -> c.greaterThan((char) 0))
				.constraint(_AllTypesFieldMeta.DOUBLEPRIMITIVEVALUE,
						c -> c.greaterThan(0.0))
				.constraint(_AllTypesFieldMeta.DOUBLEVALUE, c -> c.greaterThan(0.0))
				.constraint(_AllTypesFieldMeta.FLOATPRIMITIVEVALUE,
						c -> c.greaterThan(0.0f))
				.constraint(_AllTypesFieldMeta.FLOATVALUE, c -> c.greaterThan(0.0f))
				.constraint(_AllTypesFieldMeta.INTEGERPRIMITIVEVALUE,
						c -> c.greaterThan(0))
				.constraint(_AllTypesFieldMeta.INTEGERVALUE, c -> c.greaterThan(0))
				.constraint(_AllTypesFieldMeta.LOCALDATEVALUE, c -> c.notNull())
				.constraint(_AllTypesFieldMeta.LONGPRIMITIVEVALUE,
						c -> c.greaterThan((long) 0))
				.constraint(_AllTypesFieldMeta.LONGVALUE, c -> c.greaterThan((long) 0))
				.constraint(_AllTypesFieldMeta.SHORTPRIMITIVEVALUE,
						c -> c.greaterThan((short) 0))
				.constraint(_AllTypesFieldMeta.SHORTVALUE, c -> c.greaterThan((short) 0))
				.constraint(_AllTypesFieldMeta.STRINGVALUE, c -> c.notEmpty())
				//
				.build();

		final AllTypesField inValidTarget = new AllTypesField("", false, false, (char) 0,
				(char) 0, (byte) 0, (byte) 0, (short) 0, (short) 0, 0, 0, (long) 0, 0,
				0.0f, 0.0f, 0.0, 0.0, BigInteger.ZERO, BigDecimal.ZERO, null);

		final ConstraintViolations constraintViolations = validator
				.validate(inValidTarget);
		assertThat(constraintViolations.isValid()).isFalse();
		assertThat(constraintViolations.size()).isEqualTo(20);
		assertThat(constraintViolations.get(0).message())
				.isEqualTo("\"bigDecimalValue\" must be greater than 0");
		assertThat(constraintViolations.get(1).message())
				.isEqualTo("\"bigIntegerValue\" must be greater than 0");
		assertThat(constraintViolations.get(2).message())
				.isEqualTo("\"booleanPrimitiveValue\" must be true");
		assertThat(constraintViolations.get(3).message())
				.isEqualTo("\"booleanValue\" must be true");
		assertThat(constraintViolations.get(4).message())
				.isEqualTo("\"bytePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(5).message())
				.isEqualTo("\"byteValue\" must be greater than 0");
		assertThat(constraintViolations.get(6).message())
				.isEqualTo("\"characterPrimitiveValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(7).message())
				.isEqualTo("\"characterValue\" must be greater than \u0000");
		assertThat(constraintViolations.get(8).message())
				.isEqualTo("\"doublePrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(9).message())
				.isEqualTo("\"doubleValue\" must be greater than 0");
		assertThat(constraintViolations.get(10).message())
				.isEqualTo("\"floatPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(11).message())
				.isEqualTo("\"floatValue\" must be greater than 0");
		assertThat(constraintViolations.get(12).message())
				.isEqualTo("\"integerPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(13).message())
				.isEqualTo("\"integerValue\" must be greater than 0");
		assertThat(constraintViolations.get(14).message())
				.isEqualTo("\"localDateValue\" must not be null");
		assertThat(constraintViolations.get(15).message())
				.isEqualTo("\"longPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(16).message())
				.isEqualTo("\"longValue\" must be greater than 0");
		assertThat(constraintViolations.get(17).message())
				.isEqualTo("\"shortPrimitiveValue\" must be greater than 0");
		assertThat(constraintViolations.get(18).message())
				.isEqualTo("\"shortValue\" must be greater than 0");
		assertThat(constraintViolations.get(19).message())
				.isEqualTo("\"stringValue\" must not be empty");
	}

	@Test
	void constructorArguments() {
		final Arguments3Validator<String, String, Integer, Person> validator = ArgumentsValidatorBuilder
				.of(Person::new)
				.builder(b -> b
						.constraint(_PersonArgumentsMeta.FIRSTNAME,
								c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
						.constraint(_PersonArgumentsMeta.LASTNAME,
								c -> c.greaterThanOrEqual(1).lessThanOrEqual(50))
						.constraint(_PersonArgumentsMeta.AGE,
								c -> c.greaterThanOrEqual(20).lessThanOrEqual(99)))
				.build();

		assertThatThrownBy(() -> validator.validated("", "", 0))
				.isInstanceOfSatisfying(ConstraintViolationsException.class, e -> {
					final ConstraintViolations violations = e.violations();
					assertThat(violations.isValid()).isFalse();
					assertThat(violations.size()).isEqualTo(3);
					assertThat(violations.get(0).message()).isEqualTo(
							"The size of \"firstName\" must be greater than or equal to 1. The given size is 0");
					assertThat(violations.get(1).message()).isEqualTo(
							"The size of \"lastName\" must be greater than or equal to 1. The given size is 0");
					assertThat(violations.get(2).message())
							.isEqualTo("\"age\" must be greater than or equal to 20");
				});
	}

	@Test
	void methodArguments() {
		final UserService userService = new UserService();
		final Arguments3Validator<UserService, String, String, User> validator = ArgumentsValidatorBuilder
				.of(UserService::createUser) //
				.builder(b -> b //
						.constraint(_UserServiceCreateUserArgumentsMeta.USERSERVICE,
								c -> c.notNull())
						.constraint(_UserServiceCreateUserArgumentsMeta.EMAIL,
								c -> c.email())
						.constraint(_UserServiceCreateUserArgumentsMeta.NAME,
								c -> c.notNull()))
				.build();

		assertThatThrownBy(() -> validator.validated(userService, "jd", null)) //
				.isInstanceOfSatisfying(ConstraintViolationsException.class,
						e -> assertThat(e.getMessage()).isEqualTo(
								"Constraint violations found!" + System.lineSeparator()
										+ "* \"email\" must be a valid email address"
										+ System.lineSeparator()
										+ "* \"name\" must not be null"));
	}

	@Test
	void nested() {
		final Validator<Address> validator = ValidatorBuilder.<Address> of() //
				.nest(_AddressMeta.COUNTRY, b -> b //
						.constraint(_Address_CountryMeta.NAME, c -> c //
								.greaterThanOrEqual(2) //
								.lessThanOrEqual(16)))
				.constraint(_AddressMeta.STREET, c -> c //
						.greaterThanOrEqual(2) //
						.lessThanOrEqual(100))
				.nest(_AddressMeta.PHONENUMBER, b -> b //
						.constraint(_Address_PhoneNumberMeta.VALUE, c -> c //
								.greaterThanOrEqual(8) //
								.lessThanOrEqual(16)))
				.build();

		final ConstraintViolations violations = validator.validate(
				new Address(new Address.Country(""), "", new Address.PhoneNumber("")));

		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"country.name\" must be greater than or equal to 2. The given size is 0");
		assertThat(violations.get(1).message()).isEqualTo(
				"The size of \"street\" must be greater than or equal to 2. The given size is 0");
		assertThat(violations.get(2).message()).isEqualTo(
				"The size of \"phoneNumber.value\" must be greater than or equal to 8. The given size is 0");
	}

	@Test
	void conditionalValueValidators() {
		ConstraintGroup group = ConstraintGroup.of("JP");
		ValueValidator<String, String> passThrough = ValueValidator.passThrough();
		ValueValidator<String, String> validator = ValidatorBuilder.<String> of()
				.constraintOnCondition(
						(String address,
								ConstraintContext c) -> !"JP".equalsIgnoreCase(c.name()),
						passThrough)
				.constraintOnGroup(group,
						PhoneNumber.validator().applicative().compose(PhoneNumber::new))
				.build().applicative();
		assertThat(validator.validate("1234", group).isValid()).isFalse();
		assertThat(validator.validate("1234").isValid()).isTrue();
	}
}
