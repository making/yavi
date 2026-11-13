/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import am.ik.yavi.constraint.base.NumericConstraintBase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalConstraintTest {

	@ParameterizedTest
	@ValueSource(strings = { "101", "150" })
	void validGreaterThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "100", "-50" })
	void invalidGreaterThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "101", "100" })
	void validGreaterThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "-50" })
	void invalidGreaterThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.greaterThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "-50" })
	void validLessThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "100", "150" })
	void invalidLessThan(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThan(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void validLessThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "101", "150" })
	void invalidLessThanOrEqual(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(c -> c.lessThanOrEqual(new BigDecimal("100")));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void validPositive(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150", "0" })
	void invalidPositive(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void invalidNegative(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150" })
	void validNegative(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void validPositiveOrZero(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150" })
	void invalidPositiveOrZero(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void invalidNegativeOrZero(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150", "0" })
	void validNegativeOrZero(BigDecimal value) {
		Predicate<BigDecimal> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@CsvSource({ "0, true", "0.00, true", "1.5, true", "1.50, true", "-1.25, true", "1.250, true", "1.255, false",
			"1E+3, true", "1E-3, false", "0.001, false" })
	void fractionDigits(BigDecimal value, boolean valid) {
		// trailing zeros are not counted
		assertThat(retrievePredicate(c -> c.fractionDigits(2)).test(value)).isEqualTo(valid);
	}

	@ParameterizedTest
	@CsvSource({ "0, true", "0.00, true", "0.5, true", "999.99, true", "-999, true", "9.99E+2, true", "1000, false",
			"-1000.0, false", "1E+3, false", "0100, true" })
	void integerDigits(BigDecimal value, boolean valid) {
		assertThat(retrievePredicate(c -> c.integerDigits(3)).test(value)).isEqualTo(valid);
		// a number below one has no integer digit
		assertThat(retrievePredicate(c -> c.integerDigits(0)).test(value))
			.isEqualTo(value.abs().compareTo(BigDecimal.ONE) < 0);
		assertThatThrownBy(() -> new BigDecimalConstraint<BigDecimal>().integerDigits(-1))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new BigDecimalConstraint<BigDecimal>().fractionDigits(-1))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@CsvSource({ "0, true", "0.25, true", "-1.50, true", "1E+2, true", "0.3, false", "0.250001, false" })
	void multipleOf(BigDecimal value, boolean valid) {
		assertThat(retrievePredicate(c -> c.multipleOf(new BigDecimal("0.25"))).test(value)).isEqualTo(valid);
		// the integral types share the constraint; the remainder of a negative value
		// counts
		final boolean integral = value.scale() <= 0;
		final boolean multipleOfFive = integral && valid;
		if (integral) {
			final long n = value.longValueExact();
			assertThat(
					new ByteConstraint<Byte>().multipleOf((byte) 5).predicates().peekFirst().predicate().test((byte) n))
				.isEqualTo(multipleOfFive);
			assertThat(new ShortConstraint<Short>().multipleOf((short) 5)
				.predicates()
				.peekFirst()
				.predicate()
				.test((short) n)).isEqualTo(multipleOfFive);
			assertThat(
					new IntegerConstraint<Integer>().multipleOf(5).predicates().peekFirst().predicate().test((int) n))
				.isEqualTo(multipleOfFive);
			assertThat(new LongConstraint<Long>().multipleOf(5L).predicates().peekFirst().predicate().test(n))
				.isEqualTo(multipleOfFive);
			assertThat(new BigIntegerConstraint<BigInteger>().multipleOf(BigInteger.valueOf(5))
				.predicates()
				.peekFirst()
				.predicate()
				.test(BigInteger.valueOf(n))).isEqualTo(multipleOfFive);
		}
		assertThat(new IntegerConstraint<Integer>().multipleOf(5).predicates().peekFirst().predicate().test(-12))
			.isFalse();
		assertThatThrownBy(() -> new BigDecimalConstraint<BigDecimal>().multipleOf(BigDecimal.ZERO))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new IntegerConstraint<Integer>().multipleOf(-5))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private static Predicate<BigDecimal> retrievePredicate(
			Function<BigDecimalConstraint<BigDecimal>, BigDecimalConstraint<BigDecimal>> constraint) {
		return constraint.apply(new BigDecimalConstraint<>()).predicates().peekFirst().predicate();
	}

}
