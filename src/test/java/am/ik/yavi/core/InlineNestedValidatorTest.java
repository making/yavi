/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

public class InlineNestedValidatorTest extends AbstractNestedValidatorTest {

	@Override
	protected Validator<Address> validator() {
		return ValidatorBuilder.<Address>of()
			.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
			.nest(Address::country, "country", b -> b.constraint(Country::name, "name", c -> c.notBlank() //
				.greaterThanOrEqual(2)))
			.nestIfPresent(Address::phoneNumber, "phoneNumber",
					b -> b.constraint(PhoneNumber::value, "value", c -> c.notBlank() //
						.greaterThanOrEqual(8)))
			.build();
	}

}
