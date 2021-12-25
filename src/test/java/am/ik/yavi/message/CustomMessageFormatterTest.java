/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.message;

import am.ik.yavi.core.Constraint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.User;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

class CustomMessageFormatterTest {

	@Test
	void customMessage() {
		final Validator<User> validator = ValidatorBuilder.<User> of()
				.messageFormatter(CustomMessageFormatter.INSTANCE)
				.constraint(User::getName, "name", c -> c.greaterThanOrEqual(2))
				.constraint(User::getEmail, "email", Constraint::notNull)
				.constraint(User::getAge, "age", c -> c.lessThanOrEqual(20)).build();

		final ConstraintViolations violations = validator
				.validate(new User("a", null, 30));
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message()).isEqualTo(
				"Die Länge von \"name\" muss größer oder gleich 2 sein. Aktuelle Länge ist 1.");
		assertThat(violations.get(1).message())
				.isEqualTo("Für \"email\" muss ein Wert vorhanden sein.");
		assertThat(violations.get(2).message())
				.isEqualTo("\"age\" muss kleiner gleich 20 sein.");
	}
}