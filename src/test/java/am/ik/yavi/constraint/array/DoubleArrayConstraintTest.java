/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleArrayConstraintTest {

	private DoubleArrayConstraint<double[]> constraint = new DoubleArrayConstraint<>();

	@Test
	void contains() {
		Predicate<double[]> predicate = constraint.contains(100.0).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 101.0, 102.0 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<double[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<double[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<double[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<double[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<double[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<double[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] {})).isFalse();
	}

}
