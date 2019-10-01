/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.builder.ValidatorBuilder;

public class CastTest {
	@Test
	public void anotherClonedValidatorShouldAlsoWork_GH23() {
		final Student student = new Student();
		final ConstraintViolations result = Student.validator.validate(student);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("name");
		assertThat(result.get(1).name()).isEqualTo("id");
	}

	@Test
	public void castShouldWork_GH23() {
		final Employee employee = new Employee();
		final ConstraintViolations result = Employee.validator.validate(employee);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("name");
		assertThat(result.get(1).name()).isEqualTo("service");
	}

	@Test
	public void originalValidatorShouldAlsoWork_GH23() {
		final Person person = new Person();
		final ConstraintViolations result = Person.validator.validate(person);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(1);
		assertThat(result.get(0).name()).isEqualTo("name");
	}

	public static class Employee extends Person {
		public static final Validator<Employee> validator = Person.validatorBuilder
				.clone().cast(Employee.class)
				.constraint(Employee::getServiceId, "service", Constraint::notNull)
				.build();

		private String serviceId;

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
	}

	public static class Person {
		static ValidatorBuilder<Person> validatorBuilder = ValidatorBuilder
				.of(Person.class)
				.constraint(Person::getName, "name", Constraint::notNull);

		public static final Validator<Person> validator = validatorBuilder.build();

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	public static class Student extends Person {
		public static final Validator<Student> validator = Person.validatorBuilder.clone()
				.cast(Student.class).constraint(Student::getId, "id", Constraint::notNull)
				.build();

		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}
}