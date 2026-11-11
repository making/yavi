/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstraintViolationsTest {

	@Test
	public void apply() {
		SimpleMessageFormatter messageFormatter = SimpleMessageFormatter.getInstance();
		ConstraintViolations violations = new ConstraintViolations();
		violations.add(ConstraintViolation.builder()
			.name("foo0")
			.messageKey("abc0")
			.defaultMessageFormat("hello0")
			.args(1)
			.messageFormatter(messageFormatter)
			.build());
		violations.add(ConstraintViolation.builder()
			.name("foo1")
			.messageKey("abc1")
			.defaultMessageFormat("hello1")
			.args(1)
			.messageFormatter(messageFormatter)
			.build());
		BindingResult bindingResult = new BindingResult();
		violations.apply(bindingResult::rejectValue);
		assertThat(bindingResult.toString()).isEqualTo("[foo0_abc0_[1]_hello0][foo1_abc1_[1]_hello1]");
	}

	static class BindingResult {

		final StringBuilder builder = new StringBuilder();

		public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
			this.builder.append("[")
				.append(field)
				.append("_")
				.append(errorCode)
				.append("_")
				.append(Arrays.toString(errorArgs))
				.append("_")
				.append(defaultMessage)
				.append("]");
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}

	}

}
