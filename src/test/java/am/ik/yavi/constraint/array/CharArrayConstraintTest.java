/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class CharArrayConstraintTest {

	private CharArrayConstraint<char[]> constraint = new CharArrayConstraint<>();

	@Test
	void contains() {
		Predicate<char[]> predicate = constraint.contains((char) 100).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 101, (char) 102 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<char[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<char[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<char[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<char[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<char[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<char[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] {})).isFalse();
	}

}
