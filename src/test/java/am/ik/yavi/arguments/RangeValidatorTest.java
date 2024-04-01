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
package am.ik.yavi.arguments;

import am.ik.yavi.Range;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.CustomValidatorTest.RangeConstraint;
import am.ik.yavi.core.Validated;
import am.ik.yavi.validator.Yavi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RangeValidatorTest {
	Arguments2Validator<Integer, Integer, Range> validator = Yavi.arguments()
			._integer("from", c -> c.greaterThanOrEqual(0))
			._integer("to", c -> c.greaterThanOrEqual(0)).apply(Range::new)
			.andThen(Yavi.arguments()
					.<Range> _object("to", c -> c.predicate(RangeConstraint.SINGLETON))
					.get());

	@Test
	void valid() {
		Range range = validator.validated(0, 1);
		assertThat(range.getFrom()).isEqualTo(0);
		assertThat(range.getTo()).isEqualTo(1);
	}

	@Test
	void invalid_single() {
		Validated<Range> validated = validator.validate(-1, -1);
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(2);
		assertThat(violations.get(0).name()).isEqualTo("from");
		assertThat(violations.get(0).messageKey())
				.isEqualTo("numeric.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("to");
		assertThat(violations.get(1).messageKey())
				.isEqualTo("numeric.greaterThanOrEqual");
	}

	@Test
	void invalid_cross() {
		Validated<Range> validated = validator.validate(2, 1);
		assertThat(validated.isValid()).isFalse();
		ConstraintViolations violations = validated.errors();
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).name()).isEqualTo("to");
		assertThat(violations.get(0).messageKey()).isEqualTo("range.cross");
	}
}
