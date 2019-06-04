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

class LongConstraintTest {

	@ParameterizedTest
	@ValueSource(longs = {101L, 150L})
	void validGreaterThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThan(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = {100L, -50L})
	void invalidGreaterThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThan(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = {101L, 100L})
	void validGreaterThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = {99L, -50L})
	void invalidGreaterThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.greaterThanOrEqual(100L));
		assertThat(predicate.test(value)).isFalse();
	}


	@ParameterizedTest
	@ValueSource(longs = {99L, -50L})
	void validLessThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThan(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = {100L, 150L})
	void invalidLessThan(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThan(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(longs = {99L, 100L})
	void validLessThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThanOrEqual(100L));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(longs = {101L, 150L})
	void invalidLessThanOrEqual(long value) {
		Predicate<Long> predicate = retrievePredicate(c -> c.lessThanOrEqual(100L));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Long> retrievePredicate(Function<LongConstraint<Long>, LongConstraint<Long>> constraint) {
		return constraint.apply(new LongConstraint<>()).predicates().peekFirst().predicate();
	}
}
