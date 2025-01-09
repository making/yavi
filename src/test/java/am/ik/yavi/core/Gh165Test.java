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
