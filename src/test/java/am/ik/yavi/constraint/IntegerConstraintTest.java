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
package am.ik.yavi.constraint;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class IntegerConstraintTest {

	@ParameterizedTest
	@ValueSource(ints = { 101, 150 })
	void validGreaterThan(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.greaterThan(100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(ints = { 100, -50 })
	void invalidGreaterThan(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.greaterThan(100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = { 101, 100 })
	void validGreaterThanOrEqual(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(ints = { 99, -50 })
	void invalidGreaterThanOrEqual(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = { 99, -50 })
	void validLessThan(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.lessThan(100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(ints = { 100, 150 })
	void invalidLessThan(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.lessThan(100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(ints = { 99, 100 })
	void validLessThanOrEqual(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.lessThanOrEqual(100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(ints = { 101, 150 })
	void invalidLessThanOrEqual(int value) {
		Predicate<Integer> predicate = retrievePredicate(c -> c.lessThanOrEqual(100));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Integer> retrievePredicate(
			Function<IntegerConstraint<Integer>, IntegerConstraint<Integer>> constraint) {
		return constraint.apply(new IntegerConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
