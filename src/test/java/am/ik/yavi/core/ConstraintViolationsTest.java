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
import java.util.Locale;

import am.ik.yavi.message.SimpleMessageFormatter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstraintViolationsTest {

	@Test
	public void apply() {
		SimpleMessageFormatter messageFormatter = new SimpleMessageFormatter();
		ConstraintViolations violations = new ConstraintViolations();
		violations.add(new ConstraintViolation("foo0", "abc0", "hello0", new Object[] { 1 }, messageFormatter,
				Locale.getDefault()));
		violations.add(new ConstraintViolation("foo1", "abc1", "hello1", new Object[] { 1 }, messageFormatter,
				Locale.getDefault()));

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
