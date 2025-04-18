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

import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
			.failureWith(ConstraintViolation.builder().name("name").message("\"{0}\" must not be blank."));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
	}

	@Test
	void testFailureWith() {
		final Validated<Object> validated = Validated.failureWith(Collections
			.singletonList(ConstraintViolation.builder().name("name").message("\"{0}\" must not be blank.")));
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors()).hasSize(1);
		assertThat(validated.errors().get(0).message()).isEqualTo("\"name\" must not be blank.");
	}

}