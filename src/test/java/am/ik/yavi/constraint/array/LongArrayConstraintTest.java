/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LongArrayConstraintTest {

	private LongArrayConstraint<long[]> constraint = new LongArrayConstraint<>();

	@Test
	void contains() {
		Predicate<long[]> predicate = constraint.contains(100L).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 101L, 102L })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<long[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<long[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L, 101L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<long[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<long[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<long[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<long[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] {})).isFalse();
	}

}
