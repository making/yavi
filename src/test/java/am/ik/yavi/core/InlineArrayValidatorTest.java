/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithArray;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

class InlineArrayValidatorTest extends AbstractArrayValidatorTest {

	@Test
	void nullCollectionValid() throws Exception {
		Validator<FormWithArray> validator = ValidatorBuilder.of(FormWithArray.class) //
			.forEachIfPresent(FormWithArray::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
		FormWithArray form = new FormWithArray(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Override
	public Validator<FormWithArray> validator() {
		return ValidatorBuilder.of(FormWithArray.class) //
			.forEach(FormWithArray::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
	}

}
