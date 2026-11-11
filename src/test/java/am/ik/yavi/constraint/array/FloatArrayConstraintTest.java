/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class FloatArrayConstraintTest {

	private FloatArrayConstraint<float[]> constraint = new FloatArrayConstraint<>();

	@Test
	void contains() {
		Predicate<float[]> predicate = constraint.contains(100.0f).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isTrue();
		assertThat(predicate.test(new float[] { 101.0f, 102.0f })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<float[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f })).isFalse();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isTrue();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f, 102.0f })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<float[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isFalse();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f, 102.0f })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<float[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f })).isFalse();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isTrue();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f, 102.0f })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<float[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f })).isTrue();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<float[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f })).isTrue();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f })).isTrue();
		assertThat(predicate.test(new float[] { 100.0f, 101.0f, 102.0f })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<float[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new float[] { 100.0f })).isTrue();
		assertThat(predicate.test(new float[] {})).isFalse();
	}

}
