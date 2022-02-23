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
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Gh168Test {

	public static enum AnimalType {
		DOG, CAT
	}

	public static class Action {
		private final String name;

		public Action(String name) {
			this.name = name;
		}

		public String name() {
			return name;
		}
	}

	public static class Animal {
		private final AnimalType type;

		private final List<Action> actions;

		public Animal(AnimalType type, List<Action> actions) {
			this.type = type;
			this.actions = actions;
		}

		public AnimalType type() {
			return type;
		}

		public List<Action> actions() {
			return actions;
		}
	}

	final Validator<Animal> validator = ValidatorBuilder.<Animal> of()
			.constraintOnCondition(
					(animal, constraintContext) -> animal.type() == AnimalType.DOG,
					b -> b.forEach(Animal::actions, "actions", action -> action
							.constraint(Action::name, "name", c -> c.predicate(
									name -> name.equals("run") || name.equals("bark"),
									"dog.message",
									"For type dog an action \"{1}\" is not allowed. Allowed values are: [\"run\", \"bark\"]"))))
			.constraintOnCondition(
					(animal, constraintContext) -> animal.type() == AnimalType.CAT,
					b -> b.forEach(Animal::actions, "actions", action -> action
							.constraint(Action::name, "name", c -> c.predicate(
									name -> name.equals("run") || name.equals("meow"),
									"cat.message",
									"For type cat an action \"{1}\" is not allowed. Allowed values are: [\"run\", \"meow\"]"))))
			.build();

	@Test
	void validDog() {
		final ConstraintViolations violations = validator.validate(new Animal(
				AnimalType.DOG, Arrays.asList(new Action("run"), new Action("bark"))));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void invalidDog() {
		final ConstraintViolations violations = validator.validate(new Animal(
				AnimalType.DOG, Arrays.asList(new Action("run"), new Action("meow"))));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).name()).isEqualTo("actions[1].name");
		assertThat(violations.get(0).message()).isEqualTo(
				"For type dog an action \"meow\" is not allowed. Allowed values are: [\"run\", \"bark\"]");
	}

	@Test
	void validCat() {
		final ConstraintViolations violations = validator.validate(new Animal(
				AnimalType.CAT, Arrays.asList(new Action("run"), new Action("meow"))));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void invalidCat() {
		final ConstraintViolations violations = validator.validate(new Animal(
				AnimalType.CAT, Arrays.asList(new Action("run"), new Action("bark"))));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).name()).isEqualTo("actions[1].name");
		assertThat(violations.get(0).message()).isEqualTo(
				"For type cat an action \"bark\" is not allowed. Allowed values are: [\"run\", \"meow\"]");
	}
}
