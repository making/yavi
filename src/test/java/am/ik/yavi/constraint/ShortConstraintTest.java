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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ShortConstraintTest {

	@ParameterizedTest
	@ValueSource(shorts = { 101, 150 })
	void validGreaterThan(short value) {
		Predicate<Short> predicate = retrievePredicate(c -> c.greaterThan((short) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 100, -50 })
	void invalidGreaterThan(short value) {
		Predicate<Short> predicate = retrievePredicate(c -> c.greaterThan((short) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 101, 100 })
	void validGreaterThanOrEqual(short value) {
		Predicate<Short> predicate = retrievePredicate(
				c -> c.greaterThanOrEqual((short) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 99, -50 })
	void invalidGreaterThanOrEqual(short value) {
		Predicate<Short> predicate = retrievePredicate(
				c -> c.greaterThanOrEqual((short) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 99, -50 })
	void validLessThan(short value) {
		Predicate<Short> predicate = retrievePredicate(c -> c.lessThan((short) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 100, 150 })
	void invalidLessThan(short value) {
		Predicate<Short> predicate = retrievePredicate(c -> c.lessThan((short) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 99, 100 })
	void validLessThanOrEqual(short value) {
		Predicate<Short> predicate = retrievePredicate(
				c -> c.lessThanOrEqual((short) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(shorts = { 101, 150 })
	void invalidLessThanOrEqual(short value) {
		Predicate<Short> predicate = retrievePredicate(
				c -> c.lessThanOrEqual((short) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Short> retrievePredicate(
			Function<ShortConstraint<Short>, ShortConstraint<Short>> constraint) {
		return constraint.apply(new ShortConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
