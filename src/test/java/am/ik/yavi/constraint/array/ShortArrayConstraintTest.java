/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShortArrayConstraintTest {

	private ShortArrayConstraint<short[]> constraint = new ShortArrayConstraint<>();

	@Test
	void contains() {
		Predicate<short[]> predicate = constraint.contains((short) 100).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 101, (short) 102 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<short[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<short[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<short[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<short[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<short[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<short[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] {})).isFalse();
	}

}
