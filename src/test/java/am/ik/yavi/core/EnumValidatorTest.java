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

import am.ik.yavi.Color;
import am.ik.yavi.Message;
import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnumValidatorTest {
	Validator<Message> validator;

	@Nested
	class TestConstraintEnumValidator {
		@BeforeEach
		void beforeEach() {
			validator = ValidatorBuilder.of(Message.class)
					.constraint(Message::getColor, "color", c -> c.equalTo(Color.RED))
					.build();
		}

		@Test
		void testValidEnumValidation() {
			Message message = new Message("text", Color.RED);

			boolean isValid = validator.validate(message).isValid();

			assertThat(isValid).isTrue();
		}

		@Test
		void testInvalidValidEnum() {
			Message message = new Message("text", Color.BLUE);

			boolean isValid = validator.validate(message).isValid();

			assertThat(isValid).isFalse();
		}
	}

	@Nested
	class TestExplicitEnumValidator {
		@BeforeEach
		void beforeEach() {
			validator = ValidatorBuilder.of(Message.class)._enum(Message::getColor,
					"color", c -> c.oneOf(EnumSet.of(Color.RED, Color.BLUE))).build();
		}

		@Test
		void testValidEnumValidation() {
			Message message = new Message("text", Color.RED);

			boolean isValid = validator.validate(message).isValid();

			assertThat(isValid).isTrue();
		}

		@Test
		void testInvalidValidEnum() {
			Message message = new Message("text", Color.GREEN);

			boolean isValid = validator.validate(message).isValid();

			assertThat(isValid).isFalse();
		}
	}
}
