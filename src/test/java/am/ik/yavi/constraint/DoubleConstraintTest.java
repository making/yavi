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

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleConstraintTest {

	@ParameterizedTest
	@ValueSource(doubles = { 101.0, 150.0 })
	void validGreaterThan(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.greaterThan(100.0));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 100.0, -50.0 })
	void invalidGreaterThan(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.greaterThan(100.0));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 101.0, 100.0 })
	void validGreaterThanOrEqual(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100.0));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99L, -50.0 })
	void invalidGreaterThanOrEqual(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100.0));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99L, -50.0 })
	void validLessThan(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.lessThan(100.0));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 100.0, 150.0 })
	void invalidLessThan(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.lessThan(100.0));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99L, 100.0 })
	void validLessThanOrEqual(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.lessThanOrEqual(100.0));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 101.0, 150.0 })
	void invalidLessThanOrEqual(double value) {
		Predicate<Double> predicate = retrievePredicate(c -> c.lessThanOrEqual(100.0));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 101.0, 150.0 })
	void validPositive(double value) {
		Predicate<Double> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { -101.0, -150.0, 0 })
	void invalidPositive(double value) {
		Predicate<Double> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99.0, 100.0, 0.0 })
	void invalidNegative(double value) {
		Predicate<Double> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { -100.0, -10.0 })
	void validNegative(double value) {
		Predicate<Double> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99.5, 100.5, 0 })
	void validPositiveOrZero(double value) {
		Predicate<Double> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(doubles = { -101, -12 })
	void invalidPositiveOrZero(double value) {
		Predicate<Double> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { 99.0, 100 })
	void invalidNegativeOrZero(double value) {
		Predicate<Double> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(doubles = { -101, -120, 0 })
	void validNegativeOrZero(double value) {
		Predicate<Double> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<Double> retrievePredicate(
			Function<DoubleConstraint<Double>, DoubleConstraint<Double>> constraint) {
		return constraint.apply(new DoubleConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
