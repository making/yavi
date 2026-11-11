/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class IntArrayConstraintTest {

	private IntArrayConstraint<int[]> constraint = new IntArrayConstraint<>();

	@Test
	void contains() {
		Predicate<int[]> predicate = constraint.contains(100).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 101, 102 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<int[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<int[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<int[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<int[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<int[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<int[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] {})).isFalse();
	}

}
