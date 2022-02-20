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

class CharacterConstraintTest {

	@ParameterizedTest
	@ValueSource(chars = { 101, 120 })
	void validGreaterThan(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.greaterThan((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 100, 0 })
	void invalidGreaterThan(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.greaterThan((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 101, 100 })
	void validGreaterThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.greaterThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 0 })
	void invalidGreaterThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.greaterThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 0 })
	void validLessThan(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.lessThan((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 100, 120 })
	void invalidLessThan(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.lessThan((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 100 })
	void validLessThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.lessThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 101, 120 })
	void invalidLessThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(
				c -> c.lessThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 100 })
	void validPositive(char value) {
		Predicate<Character> predicate = retrievePredicate(
				NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 0 })
	void invalidPositive(char value) {
		Predicate<Character> predicate = retrievePredicate(
				NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 100 })
	void validNegative(char value) {
		Predicate<Character> predicate = retrievePredicate(
				NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Character> retrievePredicate(
			Function<CharacterConstraint<Character>, CharacterConstraint<Character>> constraint) {
		return constraint.apply(new CharacterConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
