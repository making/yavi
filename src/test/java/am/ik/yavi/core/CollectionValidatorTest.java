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
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.FormWithCollection;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionValidatorTest extends AbstractCollectionValidatorTest {
	Validator<Address> addressValidator = ValidatorBuilder.<Address> of()
			.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
			.nest(Address::country, "country", Country.validator())
			.nestIfPresent(Address::phoneNumber, "phoneNumber", PhoneNumber.validator())
			.build();

	@Test
	void nullCollectionValid() throws Exception {
		Validator<FormWithCollection> validator = ValidatorBuilder
				.of(FormWithCollection.class) //
				.forEachIfPresent(FormWithCollection::getAddresses, "addresses",
						addressValidator)
				.build();
		FormWithCollection form = new FormWithCollection(null);
		ConstraintViolations violations = validator.validate(form);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void stringListAllInValid() throws Exception {
		Foo foo = new Foo(Arrays.asList("abc", "def", "ghi"));
		Validator<String> stringValidator = ValidatorBuilder.of(String.class).constraint(
				String::toString, "value", c -> c.notNull().lessThanOrEqual(2)).build();
		Validator<Foo> validator = ValidatorBuilder.of(Foo.class)
				.forEach(Foo::getTexts, "texts", stringValidator).build();
		ConstraintViolations violations = validator.validate(foo);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"texts[0].value\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(0).messageKey()).isEqualTo("container.lessThanOrEqual");
		assertThat(violations.get(1).message()).isEqualTo(
				"The size of \"texts[1].value\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(1).messageKey()).isEqualTo("container.lessThanOrEqual");
		assertThat(violations.get(2).message()).isEqualTo(
				"The size of \"texts[2].value\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(2).messageKey()).isEqualTo("container.lessThanOrEqual");
	}

	@Test
	void stringListAllInValidEmptyNestedName() throws Exception {
		Foo foo = new Foo(Arrays.asList("abc", "def", "ghi"));
		Validator<String> stringValidator = ValidatorBuilder.of(String.class)
				.constraint(String::toString, "", c -> c.notNull().lessThanOrEqual(2))
				.build();
		Validator<Foo> validator = ValidatorBuilder.of(Foo.class)
				.forEach(Foo::getTexts, "texts", stringValidator).build();
		ConstraintViolations violations = validator.validate(foo);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"The size of \"texts[0]\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(0).messageKey()).isEqualTo("container.lessThanOrEqual");
		assertThat(violations.get(1).message()).isEqualTo(
				"The size of \"texts[1]\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(1).messageKey()).isEqualTo("container.lessThanOrEqual");
		assertThat(violations.get(2).message()).isEqualTo(
				"The size of \"texts[2]\" must be less than or equal to 2. The given size is 3");
		assertThat(violations.get(2).messageKey()).isEqualTo("container.lessThanOrEqual");
	}

	@Test
	void stringListAllValid() throws Exception {
		Foo foo = new Foo(Arrays.asList("ab", "cd", "ef"));
		Validator<String> stringValidator = ValidatorBuilder.of(String.class).constraint(
				String::toString, "value", c -> c.notNull().lessThanOrEqual(2)).build();
		Validator<Foo> validator = ValidatorBuilder.of(Foo.class)
				.forEach(Foo::getTexts, "texts", stringValidator).build();
		ConstraintViolations violations = validator.validate(foo);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void unique() throws Exception {
		Foo foo = new Foo(Arrays.asList("a", "b", "c", "d", "e"));
		Validator<Foo> validator = ValidatorBuilder.of(Foo.class)
				.constraint(Foo::getTexts, "texts", c -> c.notEmpty().unique()).build();
		ConstraintViolations violations = validator.validate(foo);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void notUnique() throws Exception {
		Foo foo = new Foo(Arrays.asList("a", "b", "c", "b", "c"));
		Validator<Foo> validator = ValidatorBuilder.of(Foo.class)
				.constraint(Foo::getTexts, "texts", c -> c.notEmpty().unique()).build();
		ConstraintViolations violations = validator.validate(foo);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"texts\" must be unique. [b, c] is/are duplicated.");
	}

	@Override
	public Validator<FormWithCollection> validator() {
		return ValidatorBuilder.of(FormWithCollection.class) //
				.forEach(FormWithCollection::getAddresses, "addresses", addressValidator)
				.build();
	}

	static class Foo {
		List<String> texts;

		Foo(List<String> texts) {
			this.texts = texts;
		}

		List<String> getTexts() {
			return texts;
		}
	}
}
