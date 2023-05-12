/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh240Test {
	@Test
	void defaultMessage() {
		final Validator<List<String>> validator = ValidatorBuilder.<List<String>> of()
				._collection(l -> l, "list", l -> l.lessThanOrEqual(2)).build();
		final ConstraintViolation violation = validator
				.validate(Arrays.asList("x", "y", "z")).get(0);
		assertThat(violation.message()).isEqualTo(
				"The size of \"list\" must be less than or equal to 2. The given size is 3");
	}

	@Test
	void customMessage() {
		final Validator<List<String>> validator = ValidatorBuilder.<List<String>> of()
				._collection(l -> l, "list",
						l -> l.lessThanOrEqual(2)
								.message("{0} had {2} elements, max allowed is {1}"))
				.build();
		final ConstraintViolation violation = validator
				.validate(Arrays.asList("x", "y", "z")).get(0);
		assertThat(violation.message())
				.isEqualTo("list had 3 elements, max allowed is 2");
	}
}
