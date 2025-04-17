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