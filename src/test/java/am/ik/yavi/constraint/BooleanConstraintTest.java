/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.constraint;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class BooleanConstraintTest {

	private BooleanConstraint<Boolean> constraint = new BooleanConstraint<>();

	@Test
	void isFalse() {
		Predicate<Boolean> predicate = constraint.isFalse().predicates().peekFirst().predicate();
		assertThat(predicate.test(true)).isFalse();
		assertThat(predicate.test(false)).isTrue();
	}

	@Test
	void isTrue() {
		Predicate<Boolean> predicate = constraint.isTrue().predicates().peekFirst().predicate();
		assertThat(predicate.test(true)).isTrue();
		assertThat(predicate.test(false)).isFalse();
	}

}
