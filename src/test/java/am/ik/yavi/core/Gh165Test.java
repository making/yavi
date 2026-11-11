/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.nio.charset.StandardCharsets;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Gh165Test {

	private final CustomConstraint<String> constraint = new CustomConstraint<String>() {
		private final int maxCount = 4;

		@Override
		public String defaultMessageFormat() {
			return "The tag \"{3}\" is longer than {1} bytes. length is {2}";
		}

		@Override
		public Object[] arguments(String violatedValue) {
			return new Object[] { this.maxCount, violatedValue.length() };
		}

		@Override
		public String messageKey() {
			return "";
		}

		@Override
		public boolean test(String tag) {
			return tag.getBytes(StandardCharsets.UTF_8).length <= this.maxCount;
		}
	};

	static class Tag {

		private final String name;

		Tag(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	@Test
	void valid() {
		final Validator<Tag> validator = ValidatorBuilder.of(Tag.class)
			.constraint(Tag::getName, "name", c -> c.notBlank().predicate(constraint))
			.build();
		final ConstraintViolations violations = validator.validate(new Tag("aaaaaa"));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.get(0).message()).isEqualTo("The tag \"aaaaaa\" is longer than 4 bytes. length is 6");
	}

}
