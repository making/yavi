/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CastTest {

	@Test
	void anotherClonedValidatorShouldAlsoWork_GH23() {
		final Student student = new Student();
		final ConstraintViolations result = Student.validator.validate(student);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("name");
		assertThat(result.get(1).name()).isEqualTo("id");
	}

	@Test
	void castShouldWork_GH23() {
		final Employee employee = new Employee();
		final ConstraintViolations result = Employee.validator.validate(employee);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(2);
		assertThat(result.get(0).name()).isEqualTo("name");
		assertThat(result.get(1).name()).isEqualTo("service");
	}

	@Test
	void originalValidatorShouldAlsoWork_GH23() {
		final Person person = new Person();
		final ConstraintViolations result = Person.validator.validate(person);
		assertThat(result.isValid()).isFalse();
		assertThat(result).hasSize(1);
		assertThat(result.get(0).name()).isEqualTo("name");
	}

	static class Employee extends Person {

		static final Validator<Employee> validator = new ValidatorBuilder<>(Person.validatorBuilder)
			.cast(Employee.class)
			.constraint(Employee::getServiceId, "service", Constraint::notNull)
			.build();

		private String serviceId;

		String getServiceId() {
			return serviceId;
		}

		void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

	}

	static class Person {

		static ValidatorBuilder<Person> validatorBuilder = ValidatorBuilder.of(Person.class)
			.constraint(Person::getName, "name", Constraint::notNull);

		static final Validator<Person> validator = validatorBuilder.build();

		private String name;

		String getName() {
			return name;
		}

		void setName(String name) {
			this.name = name;
		}

	}

	static class Student extends Person {

		static final Validator<Student> validator = new ValidatorBuilder<>(Person.validatorBuilder).cast(Student.class)
			.constraint(Student::getId, "id", Constraint::notNull)
			.build();

		private String id;

		String getId() {
			return id;
		}

		void setId(String id) {
			this.id = id;
		}

	}

}