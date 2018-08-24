/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
import am.ik.yavi.constraint.Constraint;

public class InlineNestedValidatorTest extends AbstractNestedValidatorTest {
	@Override
	protected Validator<Address> validator() {
		return Validator.<Address> builder()
				.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
				.constraintForObject(Address::country, "country",
						Constraint::notNull)
				.constraintForNested(Address::country, "country",
						b -> b.constraint(Country::name, "name", c -> c.notBlank() //
								.greaterThanOrEqual(2)))
				.constraintForNested(Address::phoneNumber, "phoneNumber",
						b -> b.constraint(PhoneNumber::value, "value", c -> c.notBlank() //
								.greaterThanOrEqual(8)))
				.build();
	}
}
