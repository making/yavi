/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh156Test {

	static class Person {

		private final @Nullable String firstName;

		private final @Nullable String middleName;

		private final @Nullable String lastName;

		public Person(@Nullable String firstName, @Nullable String middleName, @Nullable String lastName) {
			this.firstName = firstName;
			this.middleName = middleName;
			this.lastName = lastName;
		}

		@Override
		public String toString() {
			if (this.middleName == null) {
				return String.format("%s %s", this.firstName, this.lastName);
			}
			return String.format("%s %s %s", this.firstName, this.middleName, this.lastName);
		}

	}

	final Arguments3Validator<String, String, String, Person> personValidator = Yavi.arguments()
		._string("firstName", c -> c.notBlank().lessThanOrEqual(128))
		._string("middleName", c -> c.lessThanOrEqual(128))
		._string("lastName", c -> c.notBlank().lessThanOrEqual(128))
		.apply(Person::new);

	@Test
	void allNonNull() {
		final Validated<Person> validated = personValidator.validate("John", "Michael", "Doe");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value().toString()).isEqualTo("John Michael Doe");
	}

	@Test
	void nullable() {
		final Validated<Person> validated = personValidator.validate("John", null, "Doe");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value().toString()).isEqualTo("John Doe");
	}

	@Test
	void allNull() {
		final Validated<Person> validated = personValidator.validate(null, null, null);
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(2);
	}

}
