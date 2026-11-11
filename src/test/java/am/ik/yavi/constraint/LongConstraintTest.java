/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = { -101, -12 })
	void invalidPositiveOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { 99, 100 })
	void invalidNegativeOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = { -101, -120, 0 })
	void validNegativeOrZero(long value) {
		Predicate<Long> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<Long> retrievePredicate(Function<LongConstraint<Long>, LongConstraint<Long>> constraint) {
		return constraint.apply(new LongConstraint<>()).predicates().peekFirst().predicate();
	}

}
