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

import java.util.Arrays;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh338Test {

	static Validator<EntityUpdate> validator = ValidatorBuilder.<EntityUpdate> of()
			.constraint(EntityUpdate::getMessageType, "messageType",
					c -> c.notOneOf(Arrays.asList(-9999, -137)))
			.build();

	@Test
	void valid() {
		ConstraintViolations violations = validator.validate(new EntityUpdate(100));
		assertThat(violations.isValid()).isTrue();
	}

	@Test
	void invalid() {
		ConstraintViolations violations = validator.validate(new EntityUpdate(-137));
		assertThat(violations.isValid()).isFalse();
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo(
				"\"messageType\" must not be one of the following values: [-9999, -137]");
	}

	static class EntityUpdate {
		private final Integer messageType;

		EntityUpdate(Integer messageType) {
			this.messageType = messageType;
		}

		public Integer getMessageType() {
			return messageType;
		}
	}
}
