/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
	void invalidNegaitveOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-150", "0" })
	void validNegaitveOrZero(BigInteger value) {
		Predicate<BigInteger> predicate = retrievePredicate(NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isTrue();
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
