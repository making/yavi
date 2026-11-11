/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithCollection;
import am.ik.yavi.PhoneNumber;

abstract class AbstractCollectionValidatorTest {

	@Test
	void allInvalid() throws Exception {
		Validator<FormWithCollection> validator = validator();
		FormWithCollection form = new FormWithCollection(
				Arrays.asList(new Address(new Country(null), null, new PhoneNumber("")),
						new Address(new Country(null), null, new PhoneNumber(""))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(8);
		assertThat(violations.get(0).message()).isEqualTo("\"addresses[0].street\" must not be blank");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).message()).isEqualTo("\"addresses[0].country.name\" must not be blank");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).message()).isEqualTo("\"addresses[0].phoneNumber.value\" must not be blank");
		assertThat(violations.get(2).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(3).message()).isEqualTo(
				"The size of \"addresses[0].phoneNumber.value\" must be greater than or equal to 8. The given size is 0");
		assertThat(violations.get(3).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(4).message()).isEqualTo("\"addresses[1].street\" must not be blank");
		assertThat(violations.get(4).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(5).message()).isEqualTo("\"addresses[1].country.name\" must not be blank");
		assertThat(violations.get(5).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(6).message()).isEqualTo("\"addresses[1].phoneNumber.value\" must not be blank");
		assertThat(violations.get(6).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(7).message()).isEqualTo(
				"The size of \"addresses[1].phoneNumber.value\" must be greater than or equal to 8. The given size is 0");
		assertThat(violations.get(7).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void inValidOne() throws Exception {
		Validator<FormWithCollection> validator = validator();

		FormWithCollection form = new FormWithCollection(
				Arrays.asList(new Address(new Country("JP"), "tokyo", new PhoneNumber("0123456789")),
						new Address(new Country(null), null, new PhoneNumber(""))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(4);
		assertThat(violations.get(0).message()).isEqualTo("\"addresses[1].street\" must not be blank");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(1).message()).isEqualTo("\"addresses[1].country.name\" must not be blank");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).message()).isEqualTo("\"addresses[1].phoneNumber.value\" must not be blank");
		assertThat(violations.get(2).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(3).message()).isEqualTo(
				"The size of \"addresses[1].phoneNumber.value\" must be greater than or equal to 8. The given size is 0");
		assertThat(violations.get(3).messageKey()).isEqualTo("container.greaterThanOrEqual");
	}

	@Test
	void nullCollectionInValid() throws Exception {
		Validator<FormWithCollection> validator = validator();

		FormWithCollection form = new FormWithCollection(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("\"addresses\" must not be null");
		assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	void nullElement() throws Exception {
		Validator<FormWithCollection> validator = validator();

		FormWithCollection form = new FormWithCollection(
				Arrays.asList(new Address(new Country("JP"), "tokyo", new PhoneNumber("0123456789")), null));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("\"addresses[1]\" must not be null");
		assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	void valid() throws Exception {
		Validator<FormWithCollection> validator = validator();
		FormWithCollection form = new FormWithCollection(
				Arrays.asList(new Address(new Country("JP"), "tokyo", new PhoneNumber("0123456789")),
						new Address(new Country("JP"), "osaka", new PhoneNumber("0123456788"))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	protected abstract Validator<FormWithCollection> validator();

}
