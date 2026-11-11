/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Arrays;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh338Test {

	static Validator<EntityUpdate> validator = ValidatorBuilder.<EntityUpdate>of()
		.constraint(EntityUpdate::getMessageType, "messageType", c -> c.notOneOf(Arrays.asList(-9999, -137)))
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
		assertThat(violations.get(0).message())
			.isEqualTo("\"messageType\" must not be one of the following values: [-9999, -137]");
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
