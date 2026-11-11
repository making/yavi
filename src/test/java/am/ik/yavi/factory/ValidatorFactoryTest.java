/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.factory;

import am.ik.yavi.User;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorFactoryTest {

	private final ValidatorFactory validatorFactory = new ValidatorFactory();

	@Test
	void validator() {
		final Validator<User> validator = this.validatorFactory.validator(builder -> builder
			.constraint(User::getName, "name", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
			.constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
			.constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
			.constraint(User::isEnabled, "enabled", c -> c.isTrue()));

		final User user = new User("", "example.com", 300);
		user.setEnabled(false);

		final ConstraintViolations violations = validator.validate(user);
		assertThat(violations.size()).isEqualTo(4);
		assertThat(violations.get(0).message())
			.isEqualTo("The size of \"name\" must be greater than or equal to 1. The given size is 0");
		assertThat(violations.get(0).messageKey()).isEqualTo("container.greaterThanOrEqual");
		assertThat(violations.get(1).message()).isEqualTo("\"email\" must be a valid email address");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(2).message()).isEqualTo("\"age\" must be less than or equal to 200");
		assertThat(violations.get(2).messageKey()).isEqualTo("numeric.lessThanOrEqual");
		assertThat(violations.get(3).message()).isEqualTo("\"enabled\" must be true");
		assertThat(violations.get(3).messageKey()).isEqualTo("boolean.isTrue");
	}

}