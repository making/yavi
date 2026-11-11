/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
		Predicate<Character> predicate = retrievePredicate(c -> c.greaterThan((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 100, 0 })
	void invalidGreaterThan(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.greaterThan((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 101, 100 })
	void validGreaterThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.greaterThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 0 })
	void invalidGreaterThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.greaterThanOrEqual((char) 100));
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
		Predicate<Character> predicate = retrievePredicate(c -> c.lessThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 101, 120 })
	void invalidLessThanOrEqual(char value) {
		Predicate<Character> predicate = retrievePredicate(c -> c.lessThanOrEqual((char) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 100 })
	void validPositive(char value) {
		Predicate<Character> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(chars = { 0 })
	void invalidPositive(char value) {
		Predicate<Character> predicate = retrievePredicate(NumericConstraintBase::positive);
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(chars = { 99, 100 })
	void validNegative(char value) {
		Predicate<Character> predicate = retrievePredicate(NumericConstraintBase::negative);
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Character> retrievePredicate(
			Function<CharacterConstraint<Character>, CharacterConstraint<Character>> constraint) {
		return constraint.apply(new CharacterConstraint<>()).predicates().peekFirst().predicate();
	}

}
