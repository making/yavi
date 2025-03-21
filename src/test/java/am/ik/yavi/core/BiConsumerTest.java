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
package am.ik.yavi.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import am.ik.yavi.User;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.message.SimpleMessageFormatter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

class BiConsumerTest {

	static final ErrorHandler<List<ConstraintViolation>> errorHandler = (errors, name, messageKey, args,
			defaultMessage) -> errors.add(new ConstraintViolation(name, messageKey, defaultMessage, args,
					new SimpleMessageFormatter(), Locale.ENGLISH));

	static final Validator<User> userValidator = ValidatorBuilder.of(User.class)
		.constraint(User::getName, "name", c -> c.notNull().greaterThanOrEqual(1).lessThanOrEqual(20))
		.constraint(User::getEmail, "email", c -> c.notNull().greaterThanOrEqual(5).lessThanOrEqual(50).email())
		.constraint(User::getAge, "age", c -> c.notNull().greaterThanOrEqual(0).lessThanOrEqual(200))
		.constraint(User::isEnabled, "enabled", c -> c.isTrue())
		.build();

	static Stream<BiConsumer<User, List<ConstraintViolation>>> biConsumers() {
		return Stream.of(userValidator.toBiConsumer(errorHandler),
				userValidator.applicative().toBiConsumer(errorHandler));
	}

	@ParameterizedTest
	@MethodSource("biConsumers")
	void allValid(BiConsumer<User, List<ConstraintViolation>> validator) throws Exception {
		User user = new User("Demo", "demo@example.com", 100);
		user.setEnabled(true);
		final List<ConstraintViolation> violations = new ArrayList<>();

		validator.accept(user, violations);
		assertThat(violations.size()).isEqualTo(0);
	}

	@ParameterizedTest
	@MethodSource("biConsumers")
	void allInvalid(BiConsumer<User, List<ConstraintViolation>> validator) throws Exception {
		User user = new User("", "example.com", 300);
		user.setEnabled(false);
		final List<ConstraintViolation> violations = new ArrayList<>();

		validator.accept(user, violations);
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
