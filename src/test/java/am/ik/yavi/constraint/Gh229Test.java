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
package am.ik.yavi.constraint;

import java.time.Instant;
import java.util.function.Supplier;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh229Test {
	@Test
	void validationResultShouldDifferIfMemoizeIsTrue() {
		final Validator<Supplier<Instant>> validator = ValidatorBuilder
				.<Supplier<Instant>> of()
				._instant(Supplier::get, "date", c -> c.before(() -> {
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return Instant.now();
				}, true)).build();
		final ConstraintViolations violations1 = validator.validate(Instant::now);
		assertThat(violations1.isValid()).isTrue();
		try {
			Thread.sleep(200);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		final ConstraintViolations violations2 = validator.validate(Instant::now);
		assertThat(violations2.isValid()).isFalse();
		assertThat(violations2.size()).isEqualTo(1);
		assertThat(violations2.get(0).messageKey()).isEqualTo("temporal.before");
	}

	@Test
	void validationResultShouldNotDifferIfMemoizeIsFalse() {
		final Validator<Supplier<Instant>> validator = ValidatorBuilder
				.<Supplier<Instant>> of()
				._instant(Supplier::get, "date", c -> c.before(() -> {
					try {
						Thread.sleep(100);
					}
					catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return Instant.now();
				}, false)).build();
		final ConstraintViolations violations1 = validator.validate(Instant::now);
		assertThat(violations1.isValid()).isTrue();
		try {
			Thread.sleep(200);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		final ConstraintViolations violations2 = validator.validate(Instant::now);
		assertThat(violations2.isValid()).isTrue();
	}
}
