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

import java.util.Arrays;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.*;

public class MultiNestedCollectionValidatorTest {
	Validator<Address> addressValidator = Validator.<Address> builder()
			.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
			.nest(Address::country, "country", Country.validator())
			.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator())
			.build();
	Validator<FormWithCollection> formValidator = Validator
			.builder(FormWithCollection.class) //
			.forEach(FormWithCollection::getAddresses, "addresses", addressValidator)
			.build();

	Validator<NestedFormWithCollection> validator = Validator
			.builder(NestedFormWithCollection.class)
			.forEach(NestedFormWithCollection::getForms, "forms", formValidator).build();

	@Test
	public void valid() {
		NestedFormWithCollection form = new NestedFormWithCollection(
				Arrays.asList(new FormWithCollection(Arrays.asList(
						new Address(new Country("JP"), "tokyo",
								new PhoneNumber("0123456789")),
						new Address(new Country("JP"), "osaka",
								new PhoneNumber("0123456788"))))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void invalid() {
		NestedFormWithCollection form = new NestedFormWithCollection(
				Arrays.asList(new FormWithCollection(Arrays.asList(
						new Address(new Country("JP"), "tokyo",
								new PhoneNumber("0123456")),
						new Address(new Country("JP"), "",
								new PhoneNumber("0123456788"))))));
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).name())
				.isEqualTo("forms[0].addresses[0].phoneNumber.value");
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"forms[0].addresses[0].phoneNumber.value\" must be greater than or equal to 8. The given size is 7");
		assertThat(violations.get(1).name()).isEqualTo("forms[0].addresses[1].street");
		assertThat(violations.get(1).message())
				.isEqualTo("\"forms[0].addresses[1].street\" must not be blank");
	}
}
