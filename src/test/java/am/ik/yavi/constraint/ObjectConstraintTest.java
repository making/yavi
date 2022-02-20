/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import am.ik.yavi.Color;
import am.ik.yavi.Message;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolation;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import am.ik.yavi.jsr305.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectConstraintTest {
	private final ObjectConstraint<String, String> constraint = new ObjectConstraint<>();

	@Test
	void isNull() {
		Predicate<String> predicate = constraint.isNull().predicates().getFirst()
				.predicate();
		assertThat(predicate.test("foo")).isFalse();
		assertThat(predicate.test(null)).isTrue();
	}

	@Test
	void notNull() {
		Predicate<String> predicate = constraint.notNull().predicates().getFirst()
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test(null)).isFalse();
	}

	@Nested
	class EqualTo {
		@Test
		void equalObjectsSucceed() {
			assertThat(equalToPredicate("other").test("other")).isTrue();
		}

		@Test
		void differentObjectsFail() {
			assertThat(equalToPredicate("other").test("this")).isFalse();
		}

		@Test
		void nullInputFails() {
			assertThat(equalToPredicate("other").test(null)).isFalse();
		}

		@Test
		void equalToNullFailsWithNonNullInput() {
			assertThat(equalToPredicate(null).test("this")).isFalse();
		}

		@Test
		void equalToNullSucceedsWithNullInput() {
			assertThat(equalToPredicate(null).test(null)).isTrue();
		}

		private Predicate<String> equalToPredicate(@Nullable String other) {
			return constraint.equalTo(other).predicates().getFirst().predicate();
		}
	}

	@Nested
	class OneOf {
		@Test
		void succeedsWhenInputIsOneOfValues() {
			assertThat(oneOfPredicate(Arrays.asList("this", "other")).test("this"))
					.isTrue();
		}

		@Test
		void failsWhenInputIsNoneOfValues() {
			assertThat(oneOfPredicate(Arrays.asList("this", "other")).test("unknown"))
					.isFalse();
		}

		@Test
		void failsWhenInputIsNull() {
			assertThat(oneOfPredicate(Arrays.asList("this", "other")).test(null))
					.isFalse();
		}

		@Test
		void succeedsWhenInputIsNullAndNullIsOneOfTheValues() {
			assertThat(oneOfPredicate(Arrays.asList("this", null)).test(null)).isTrue();
		}

		@Test
		void nullValuesIsNotAllowed() {
			assertThatThrownBy(() -> oneOfPredicate(null))
					.isExactlyInstanceOf(NullPointerException.class);
		}

		@Test
		void violatedOneOfArgumentsAreRenderedCorrectly() {
			final Validator<Message> validator = ValidatorBuilder.<Message> of()
					.constraint(Message::getText, "text",
							c -> c.oneOf(Arrays.asList("a", "b")))
					._object(Message::getColor, "color",
							c -> c.oneOf(EnumSet.of(Color.RED, Color.BLUE)))
					.build();
			final ConstraintViolations violations = validator
					.validate(new Message("c", Color.GREEN));
			assertThat(violations).extracting(ConstraintViolation::message)
					.containsExactlyInAnyOrder(
							"\"text\" must be one of the following values: [a, b]",
							"\"color\" must be one of the following values: [RED, BLUE]");
		}

		private Predicate<String> oneOfPredicate(List<String> values) {
			return constraint.oneOf(values).predicates().getFirst().predicate();
		}
	}
}
