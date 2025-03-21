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

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Gh335Test {

	public static Validator<NestedObject> nestedValidator = ValidatorBuilder.<NestedObject>of() //
		.constraintOnCondition((nested, constraintGroup) -> nested.bool(),
				c -> c.constraint(NestedObject::field, "field", Constraint::notNull)) //
		.failFast(true) //
		.build();

	public static Validator<RootObject> rootValidator = ValidatorBuilder.<RootObject>of() //
		.nest(RootObject::nested2, "nested2", nestedValidator) //
		.failFast(true) //
		.build();

	@Test
	void rootValidator_Should_FailFast() {
		// Arrange
		RootObject object = new RootObject("aaa", new NestedObject(null, true));

		// Act
		ConstraintViolations actual = rootValidator.validate(object);

		// Assert
		assertFalse(actual.isValid());
		assertEquals(actual.size(), 1);
	}

	@Test
	void nestedValidator_Should_FailFast() {
		// Arrange
		NestedObject object = new NestedObject(null, true);

		// Act
		ConstraintViolations actual = nestedValidator.validate(object);

		// Assert
		assertFalse(actual.isValid());
		assertEquals(actual.size(), 1);
	}

	public static class NestedObject {

		private final String field;

		private final Boolean bool;

		public NestedObject(String field, Boolean bool) {
			this.field = field;
			this.bool = bool;
		}

		public String field() {
			return field;
		}

		public Boolean bool() {
			return bool;
		}

	}

	public static class RootObject {

		private final String field;

		private final NestedObject nested2;

		public RootObject(String field, NestedObject nested2) {
			this.field = field;
			this.nested2 = nested2;
		}

		public String field() {
			return field;
		}

		public NestedObject nested2() {
			return nested2;
		}

	}

}
