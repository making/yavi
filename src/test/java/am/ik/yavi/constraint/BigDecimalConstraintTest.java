/*
 * Copyright (C) 2018-2020 Toshiaki Maki <makingx@gmail.com>
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = {"101", "150"})
	void validGreaterThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"100", "-50"})
	void invalidGreaterThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"101", "100"})
	void validGreaterThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"99", "-50"})
	void invalidGreaterThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}


	@ParameterizedTest
	@ValueSource(strings = {"99", "-50"})
	void validLessThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"100", "150"})
	void invalidLessThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = {"99", "100"})
	void validLessThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {"101", "150"})
	void invalidLessThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<BigDecimal> retrievePredicate(Function<BigDecimalConstraint<BigDecimal>, BigDecimalConstraint<BigDecimal>> constraint) {
		return constraint.apply(new BigDecimalConstraint<>()).predicates().peekFirst().predicate();
	}
}
