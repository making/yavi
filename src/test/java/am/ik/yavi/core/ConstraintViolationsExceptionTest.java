/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintViolationsExceptionTest {

	@Test
	void customMessage() {
		final ConstraintViolations violations = new ConstraintViolations();
		violations.add(ConstraintViolation.builder().name("name1").message("a is invalid."));
		final ConstraintViolationsException exception = new ConstraintViolationsException("error!", violations);
		assertThat(exception.getMessage()).isEqualTo("error!");
	}

	@Test
	void defaultMessage() {
		final ConstraintViolations violations = new ConstraintViolations();
		violations.add(ConstraintViolation.builder().name("name1").message("a is invalid."));
		final ConstraintViolationsException exception = new ConstraintViolationsException(violations);
		assertThat(exception.getMessage())
			.isEqualTo("Constraint violations found!" + System.lineSeparator() + "* a is invalid.");
	}

}