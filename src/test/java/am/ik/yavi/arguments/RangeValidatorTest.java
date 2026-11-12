/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.arguments;

import java.util.Objects;
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
		._integer("to", c -> c.greaterThanOrEqual(0))
		.apply((from, to) -> new Range(Objects.requireNonNull(from), Objects.requireNonNull(to)))
		.andThen(Yavi.arguments().<Range>_object("to", c -> c.predicate(RangeConstraint.SINGLETON)).get());

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
		assertThat(violations.get(0).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
		assertThat(violations.get(1).name()).isEqualTo("to");
		assertThat(violations.get(1).messageKey()).isEqualTo("numeric.greaterThanOrEqual");
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
