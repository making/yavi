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

class FloatConstraintTest {

	@ParameterizedTest
	@ValueSource(floats = { 101.0f, 150.0f })
	void validGreaterThan(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.greaterThan(100.0f));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 100.0f, -50.0f })
	void invalidGreaterThan(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.greaterThan(100.0f));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 101.0f, 100.0f })
	void validGreaterThanOrEqual(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100.0f));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99L, -50.0f })
	void invalidGreaterThanOrEqual(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100.0f));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99L, -50.0f })
	void validLessThan(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.lessThan(100.0f));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 100.0f, 150.0f })
	void invalidLessThan(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.lessThan(100.0f));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99L, 100.0f })
	void validLessThanOrEqual(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.lessThanOrEqual(100.0f));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 101.0f, 150.0f })
	void invalidLessThanOrEqual(float value) {
		Predicate<Float> predicate = retrievePredicate(c -> c.lessThanOrEqual(100.0f));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 101.0f, 150.0f })
	void validPositive(float value) {
		Predicate<Float> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { -101.0f, -150.0f, 0f })
	void invalidPositive(float value) {
		Predicate<Float> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99f, 100f, 0f })
	void invalidNegative(float value) {
		Predicate<Float> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { -100f, -10f })
	void validNegative(float value) {
		Predicate<Float> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99.5f, 100.5f, 0f })
	void validPositiveOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { -101f, -12f })
	void invalidPositiveOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::positiveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99.0f, 100f })
	void invalidNegaitveOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { -101f, -120f, 0f })
	void validNegaitveOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::negaitveOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(floats = { 99.0f, 100f })
	void invalidNegativeOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(floats = { -101f, -120f, 0f })
	void validNegativeOrZero(float value) {
		Predicate<Float> predicate = retrievePredicate(
				NumericConstraintBase::negativeOrZero);
		assertThat(predicate.test(value)).isTrue();
	}

	private static Predicate<Float> retrievePredicate(
			Function<FloatConstraint<Float>, FloatConstraint<Float>> constraint) {
		return constraint.apply(new FloatConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
