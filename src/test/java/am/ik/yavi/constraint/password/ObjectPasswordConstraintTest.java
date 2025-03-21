/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.password;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectPasswordConstraintTest {

	PasswordPolicy<Account> passwordPolicy = PasswordPolicy.of("UsernameNotIncluded",
			account -> !account.password().toUpperCase().contains(account.username().toUpperCase()));

	Validator<Account> validator = ValidatorBuilder.<Account>of()
		.constraint(Account::username, "username", c -> c.notBlank())
		.constraint(Account::password, "password", c -> c.greaterThanOrEqual(8).password(policy -> policy.strong()))
		.constraintOnTarget("password", c -> c.password(policy -> policy.required(passwordPolicy).build()))
		.build();

	@Test
	void valid() {
		final Account account = new Account("test", "Abcd123!");
		final ConstraintViolations violations = validator.validate(account);
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void invalid() {
		final Account account = new Account("abcd", "Abcd123!");
		final ConstraintViolations violations = validator.validate(account);
		assertThat(violations.isValid()).isFalse();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("password.required");
		assertThat(violations.get(0).args()).hasSize(3);
		assertThat(violations.get(0).args()[1]).isEqualTo("UsernameNotIncluded");
	}

	static class Account {

		private final String username;

		private final String password;

		public Account(String username, String password) {
			this.username = username;
			this.password = password;
		}

		public String username() {
			return username;
		}

		public String password() {
			return password;
		}

		@Override
		public String toString() {
			return "Account{" + "username='" + username + '\'' + ", password='" + password + '\'' + '}';
		}

	}

}
