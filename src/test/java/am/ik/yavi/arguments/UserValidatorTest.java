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
			.<Role> _enum("role", c -> c.notNull().oneOf(User.Role.USER, User.Role.ADMIN))
			.apply(User::new);

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
