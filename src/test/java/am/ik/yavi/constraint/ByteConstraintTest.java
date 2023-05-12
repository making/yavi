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

import java.math.BigInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ByteConstraintTest {

	@ParameterizedTest
	@ValueSource(bytes = { 101, 120 })
	void validGreaterThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThan((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 100, -50 })
	void invalidGreaterThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThan((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 101, 100 })
	void validGreaterThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 99, -50 })
	void invalidGreaterThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 99, -50 })
	void validLessThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThan((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 100, 120 })
	void invalidLessThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThan((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 99, 100 })
	void validLessThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = { 101, 120 })
	void invalidLessThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void validPositive(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-1", "0" })
	void invalidPositive(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void invalidNegative(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-10" })
	void validNegative(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100", "0" })
	void validPositiveOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-12" })
	void invalidPositiveOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void invalidNegaitveOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-120", "0" })
	void validNegaitveOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = { "99", "100" })
	void invalidNegativeOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(strings = { "-101", "-120", "0" })
	void validNegativeOrZero(byte value) {
		Predicate<Byte> predicate = retrievePredicate(NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<Byte> retrievePredicate(Function<ByteConstraint<Byte>, ByteConstraint<Byte>> constraint) {
		return constraint.apply(new ByteConstraint<>()).predicates().peekFirst().predicate();
	}

}
