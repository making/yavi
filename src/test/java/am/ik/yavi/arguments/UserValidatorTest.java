/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.arguments.User.Role;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserValidatorTest {

	Arguments3Validator<String, String, Role, User> validator = Yavi.arguments()
		._string("email", c -> c.notBlank().email())
		._string("name", c -> c.notBlank())
		.<Role>_enum("role", c -> c.notNull().oneOf(User.Role.USER, User.Role.ADMIN))
		.apply((email, name, role) -> new User(email, name, role));

	@Test
	void valid() {
		User user = validator.validated("demo@example.com", "demo", Role.USER);
		assertThat(user.email()).isEqualTo("demo@example.com");
		assertThat(user.name()).isEqualTo("demo");
		assertThat(user.role()).isEqualTo(Role.USER);
	}

	@Test
	void invalid() {
		Validated<User> user = validator.validate("demo", "   ", Role.GUEST);
		assertThat(user.isValid()).isFalse();
		ConstraintViolations violations = user.errors();
		assertThat(violations).hasSize(3);
		assertThat(violations.get(0).name()).isEqualTo("email");
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.email");
		assertThat(violations.get(1).name()).isEqualTo("name");
		assertThat(violations.get(1).messageKey()).isEqualTo("charSequence.notBlank");
		assertThat(violations.get(2).name()).isEqualTo("role");
		assertThat(violations.get(2).messageKey()).isEqualTo("object.oneOf");
	}

}
