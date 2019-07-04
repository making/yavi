/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.Address;
import am.ik.yavi.Country;
import am.ik.yavi.PhoneNumber;
import am.ik.yavi.builder.ValidatorBuilder;

public class NestedValidatorTest extends AbstractNestedValidatorTest {
	@Override
	protected Validator<Address> validator() {
		return ValidatorBuilder.<Address> of()
				.constraint(Address::street, "street", c -> c.notBlank().lessThan(32))
				.nest(Address::country, "country", Country.validator())
				.nestIfPresent(Address::phoneNumber, "phoneNumber",
						PhoneNumber.validator())
				.build();
	}

	private ConstraintCondition<IntRange> whenAllOfNumbersNotNull = (r,
			g) -> r.small != null && r.big != null;

	private Validator<IntRange> thenCompareNumbers = ValidatorBuilder.of(IntRange.class)
			.constraintOnTarget(r -> r.big > r.small, "big", null,
					"[big] must be greater than [small]")
			.build();

	private Validator<IntRange> intRangeValidator = ValidatorBuilder.of(IntRange.class)
			.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.small, "small",
					Constraint::notNull)
			.constraint((ValidatorBuilder.ToInteger<IntRange>) r -> r.big, "big",
					Constraint::notNull)
			.constraintOnCondition(whenAllOfNumbersNotNull, thenCompareNumbers).build();

	@Test
	void testStandAlone_GH20() {
		IntRange intRange = new IntRange(1, 0);

		assertThat(intRangeValidator.validate(intRange).isValid()).isFalse();
	}

	@Test
	void testNested_GH20() {
		Validator<Nested> validator = ValidatorBuilder.of(Nested.class)
				.nest(n -> n.intRange, "intRange", intRangeValidator).build();

		Nested nested = new Nested(new IntRange(1, 0));

		assertThat(validator.validate(nested).isValid()).isFalse();
		validator.validate(nested).forEach(x -> {
			System.out.println(x);
		});
	}

	public static class Nested {
		IntRange intRange;

		Nested(IntRange intRange) {
			this.intRange = intRange;
		}
	}

	public static class IntRange {
		public Integer small;
		public Integer big;

		IntRange(Integer small, Integer big) {
			this.small = small;
			this.big = big;
		}
	}
}
