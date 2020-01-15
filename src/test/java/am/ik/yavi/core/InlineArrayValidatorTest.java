/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithArray;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

public class InlineArrayValidatorTest extends AbstractArrayValidatorTest {
	@Test
	public void nullCollectionValid() throws Exception {
		Validator<FormWithArray> validator = ValidatorBuilder.of(FormWithArray.class) //
				.forEachIfPresent(FormWithArray::getAddresses, "addresses",
						b -> b.constraint(Address::street, "street",
								c -> c.notBlank().lessThan(32))
								.nest(Address::country, "country", Country.validator())
								.nestIfPresent(Address::phoneNumber, "phoneNumber",
										PhoneNumber.validator()))
				.build();
		FormWithArray form = new FormWithArray(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Override
	public Validator<FormWithArray> validator() {
		return ValidatorBuilder.of(FormWithArray.class) //
				.forEach(FormWithArray::getAddresses, "addresses",
						b -> b.constraint(Address::street, "street",
								c -> c.notBlank().lessThan(32))
								.nest(Address::country, "country", Country.validator())
								.nestIfPresent(Address::phoneNumber, "phoneNumber",
										PhoneNumber.validator()))
				.build();
	}
}
