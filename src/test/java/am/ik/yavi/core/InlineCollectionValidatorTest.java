/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithCollection;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

class InlineCollectionValidatorTest extends AbstractCollectionValidatorTest {

	@Test
	void nullCollectionValid() throws Exception {
		Validator<FormWithCollection> validator = ValidatorBuilder.of(FormWithCollection.class) //
			.forEachIfPresent(FormWithCollection::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
		FormWithCollection form = new FormWithCollection(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Override
	public Validator<FormWithCollection> validator() {
		return ValidatorBuilder.of(FormWithCollection.class) //
			.forEach(FormWithCollection::getAddresses, "addresses",
					b -> b.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
						.nest(Address::country, "country", Country.validator())
						.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator()))
			.build();
	}

}
