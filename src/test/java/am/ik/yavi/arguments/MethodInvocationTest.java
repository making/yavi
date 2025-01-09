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
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.core.ConstraintViolationsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MethodInvocationTest {

	static final Arguments4Validator<UserService, String, String, Role, User> validator = ArgumentsValidatorBuilder
		.of(UserService::createUser) //
		.builder(b -> b //
			._object(Arguments1::arg1, "userService", c -> c.notNull())
			._string(Arguments2::arg2, "email", c -> c.email())
			._string(Arguments3::arg3, "name", c -> c.notNull())
			._enum(Arguments4::arg4, "role", c -> c.notNull().oneOf(Role.USER, Role.ADMIN))) //
		.build();

	static final UserService userService = new UserService();

	@Test
	void valid() {
		final User user = validator.validated(userService, "jd@example.com", "John Doe", Role.USER);
		assertThat(user).isNotNull();
	}

	@Test
	void invalid() {
		assertThatThrownBy(() -> validator.validated(userService, "jd", null, Role.GUEST)) //
			.isInstanceOfSatisfying(ConstraintViolationsException.class,
					e -> assertThat(e.getMessage()).isEqualTo("Constraint violations found!" + System.lineSeparator()
							+ "* \"email\" must be a valid email address" + System.lineSeparator()
							+ "* \"name\" must not be null" + System.lineSeparator()
							+ "* \"role\" must be one of the following values: [USER, ADMIN]"));
	}

}
