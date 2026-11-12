/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.jspecify.annotations.Nullable;
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

		private final @Nullable String field;

		private final @Nullable Boolean bool;

		public NestedObject(@Nullable String field, @Nullable Boolean bool) {
			this.field = field;
			this.bool = bool;
		}

		public @Nullable String field() {
			return field;
		}

		public @Nullable Boolean bool() {
			return bool;
		}

	}

	public static class RootObject {

		private final @Nullable String field;

		private final NestedObject nested2;

		public RootObject(String field, NestedObject nested2) {
			this.field = field;
			this.nested2 = nested2;
		}

		public @Nullable String field() {
			return field;
		}

		public NestedObject nested2() {
			return nested2;
		}

	}

}
