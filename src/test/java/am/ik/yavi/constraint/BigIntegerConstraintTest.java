/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.NumericConstraintBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class BigIntegerConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = { "101", "150" })
	void validGreaterThan(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.greaterThan(new BigInteger("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "100", "-50" })
	void invalidGreaterThan(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.greaterThan(new BigInteger("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "101", "100" })
	void validGreaterThanOrEqual(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigInteger("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "-50" })
	void invalidGreaterThanOrEqual(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigInteger("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "-50" })
	void validLessThan(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.lessThan(new BigInteger("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "100", "150" })
	void invalidLessThan(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.lessThan(new BigInteger("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void validLessThanOrEqual(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigInteger("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "101", "150" })
	void invalidLessThanOrEqual(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigInteger("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void validPositive(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150", "0" })
	void invalidPositive(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void invalidNegative(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150" })
	void validNegative(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void validPositiveOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150" })
	void invalidPositiveOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void invalidNegativeOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150", "0" })
	void validNegativeOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<BigInteger> retrievePredicate(
			Function<BigIntegerConstraint<BigInteger>, BigIntegerConstraint<BigInteger>> constraint) {
		return constraint.apply(new BigIntegerConstraint<>()).predicates().peekFirst().predicate();
	}

}
