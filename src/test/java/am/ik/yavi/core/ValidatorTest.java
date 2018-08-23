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

import java.text.Normalizer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.User;

public class ValidatorTest {
	Validator<User> validator() {
		return Validator.<User> builder() //
				.constraint(User::getName, "name", c -> c.notNull() //
						.greaterThanOrEqual(1) //
						.lessThanOrEqual(20)) //
				.constraint(User::getEmail, "email", c -> c.notNull() //
						.greaterThanOrEqual(1) //
						.lessThanOrEqual(50) //
						.email()) //
				.constraint(User::getAge, "age", c -> c.notNull() //
						.greaterThanOrEqual(0) //
						.lessThanOrEqual(200))
				.build();
	}

	@Test
	public void valid() throws Exception {
		User user = new User("foo", "foo@example.com", 30);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	public void allInvalid() throws Exception {
		User user = new User("", "example.com", 300);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message())
				.isEqualTo("The size of \"name\" must be greater than or equal to 1");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" must not be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("number.lessThanOrEqual");
	}

	@Test
	public void multipleViolationOnOneProperty() throws Exception {
		User user = new User("foo", "", 200);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(2);
		assertThat(violations.get(0).message())
				.isEqualTo("The size of \"email\" must be greater than or equal to 1");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message())
				.isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
	}

	@Test
	public void nullValues() throws Exception {
		User user = new User(null, null, null);
		Validator<User> validator = validator();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo("\"name\" must not be null");
		assertThat(violations.get(0).messageKey()).isEqualTo("object.notNull");
		assertThat(violations.get(1).message()).isEqualTo("\"email\" must not be null");
		assertThat(violations.get(1).messageKey()).isEqualTo("object.notNull");
		assertThat(violations.get(2).message()).isEqualTo("\"age\" must not be null");
		assertThat(violations.get(2).messageKey()).isEqualTo("object.notNull");
	}

	@Test
	public void combiningCharacter() throws Exception {
		User user = new User("モシ\u3099", null, null);
		Validator<User> validator = Validator.builder(User.class)
				.constraint(User::getName, Normalizer.Form.NFC, "name",
						c -> c.lessThanOrEqual(2))
				.build();
		ConstraintViolations violations = validator.validate(user);
		assertThat(violations.isValid()).isTrue();
	}
}