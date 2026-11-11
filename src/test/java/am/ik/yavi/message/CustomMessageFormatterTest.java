/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.User;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;

class CustomMessageFormatterTest {

	@Test
	void customMessage() {
		final Validator<User> validator = ValidatorBuilder.<User>of()
			.messageFormatter(CustomMessageFormatter.INSTANCE)
			.constraint(User::getName, "name", c -> c.greaterThanOrEqual(2))
			.constraint(User::getEmail, "email", c -> c.notNull())
			.constraint(User::getAge, "age", c -> c.lessThanOrEqual(20))
			.build();

		final ConstraintViolations violations = validator.validate(new User("a", null, 30));
		assertThat(violations.size()).isEqualTo(3);
		assertThat(violations.get(0).message())
			.isEqualTo("Die Länge von \"name\" muss größer oder gleich 2 sein. Aktuelle Länge ist 1.");
		assertThat(violations.get(1).message()).isEqualTo("Für \"email\" muss ein Wert vorhanden sein.");
		assertThat(violations.get(2).message()).isEqualTo("\"age\" muss kleiner gleich 20 sein.");
	}

}