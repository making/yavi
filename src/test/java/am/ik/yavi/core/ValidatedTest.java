/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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