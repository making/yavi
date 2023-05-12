/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.builder.StringValidatorBuilder;
import am.ik.yavi.core.Validated;
import am.ik.yavi.jsr305.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh156Test {
	static class Person {
		private final String firstName;

		private final String middleName;

		private final String lastName;

		public Person(String firstName, @Nullable String middleName, String lastName) {
			this.firstName = firstName;
			this.middleName = middleName;
			this.lastName = lastName;
		}

		@Override
		public String toString() {
			if (this.middleName == null) {
				return String.format("%s %s", this.firstName, this.lastName);
			}
			return String.format("%s %s %s", this.firstName, this.middleName,
					this.lastName);
		}
	}

	final StringValidator<String> firstNameValidator = StringValidatorBuilder
			.of("firstName", c -> c.notBlank().lessThanOrEqual(128)).build();

	final StringValidator<String> middleNameValidator = StringValidatorBuilder
			.of("middleName", c -> c.lessThanOrEqual(128)).build();

	final StringValidator<String> lastNameValidator = StringValidatorBuilder
			.of("lastName", c -> c.notBlank().lessThanOrEqual(128)).build();

	final Arguments3Validator<String, String, String, Person> personValidator = ArgumentsValidators
			.split(firstNameValidator, middleNameValidator, lastNameValidator)
			.apply(Person::new);

	@Test
	void allNonNull() {
		final Validated<Person> validated = personValidator.validate("John", "Michael",
				"Doe");
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
