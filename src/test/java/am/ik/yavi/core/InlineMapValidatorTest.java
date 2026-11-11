/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithMap;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

class InlineMapValidatorTest extends AbstractMapValidatorTest {

	@Test
	void nullCollectionValid() throws Exception {
		Validator<FormWithMap> validator = ValidatorBuilder.of(FormWithMap.class) //
			.forEachIfPresent(FormWithMap::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
		FormWithMap form = new FormWithMap(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Override
	public Validator<FormWithMap> validator() {
		return ValidatorBuilder.of(FormWithMap.class) //
			.forEach(FormWithMap::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
	}

}
