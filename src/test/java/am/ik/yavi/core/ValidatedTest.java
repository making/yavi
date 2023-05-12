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

import java.util.Collections;
import java.util.Locale;

import am.ik.yavi.message.SimpleMessageFormatter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ValidatedTest {

	@Test
	void successWith() {
		final Validated<String> validated = Validated.successWith("OK");
		assertThat(validated.isValid()).isTrue();
		assertThat(validated.value()).isEqualTo("OK");
	}

	@Test
	void failureWith() {
		final Validated<Object> validated = Validated
			.failureWith(new ConstraintViolation("name", "notNull", "\"{0}\" must not be blank.",
					new Object[] { "name", "" }, new SimpleMessageFormatter(), Locale.ENGLISH));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
	}

	@Test
	void testFailureWith() {
		final Validated<Object> validated = Validated.failureWith(
				Collections.singletonList(new ConstraintViolation("name", "notNull", "\"{0}\" must not be blank.",
						new Object[] { "name", "" }, new SimpleMessageFormatter(), Locale.ENGLISH)));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
	}

}