/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ByteArrayConstraintTest {

	private ByteArrayConstraint<byte[]> constraint = new ByteArrayConstraint<>();

	@Test
	void contains() {
		Predicate<byte[]> predicate = constraint.contains((byte) 100).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 101, (byte) 102 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<byte[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<byte[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<byte[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<byte[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<byte[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<byte[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] {})).isFalse();
	}

}
