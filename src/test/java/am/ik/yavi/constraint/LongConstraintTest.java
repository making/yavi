/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import am.ik.yavi.constraint.base.NumericConstraintBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class LongConstraintTest {

	@ParameterizedTest
	@ValueSource(longs = { 101L, 150L })
	void validGreaterThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThan(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { 100L, -50L })
	void invalidGreaterThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThan(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 101L, 100L })
	void validGreaterThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99L, -50L })
	void invalidGreaterThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99L, -50L })
	void validLessThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThan(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { 100L, 150L })
	void invalidLessThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThan(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99L, 100L })
	void validLessThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThanOrEqual(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { 101L, 150L })
	void invalidLessThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThanOrEqual(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 101L, 150L })
	void validPositive(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { -101L, -150L, 0L })
	void invalidPositive(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 9L, 100L, 0L })
	void invalidNegative(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { -100L, -10L })
	void validNegative(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99, 100, 0 })
	void validPositiveOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { -101, -12 })
	void invalidPositiveOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99, 100 })
	void invalidNegativeOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { -101, -120, 0 })
	void validNegativeOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<Long> retrievePredicate(
			Function<LongConstraint<Long>, LongConstraint<Long>> constraint) {
		return constraint.apply(new LongConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
