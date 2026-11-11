/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConstraintGroupTest {

	@Test
	void name() {
		ConstraintGroup cg = ConstraintGroup.of("foo");
		assertThat(cg.name()).isEqualTo("foo");
		assertThat(cg.name()).isNotEqualTo("Foo");
	}

	@Test
	void testEnum() {
		ConstraintCondition<?> condition = CustomGroup.FOO.toCondition();
		assertThat(condition.test(null, CustomGroup.FOO)).isTrue();
		assertThat(condition.test(null, CustomGroup.BAR)).isFalse();
		assertThat(condition.test(null, ConstraintGroup.of("FOO"))).isTrue();
	}

	@Test
	void toCondition() {
		ConstraintGroup cg = ConstraintGroup.of("foo");
		ConstraintCondition<?> condition = cg.toCondition();
		assertThat(condition.test(null, ConstraintGroup.of("foo"))).isTrue();
		assertThat(condition.test(null, ConstraintGroup.of("Foo"))).isFalse();
		assertThat(condition.test(null, ConstraintGroup.of("bar"))).isFalse();
	}

	enum CustomGroup implements ConstraintGroup {

		FOO, BAR

	}

}
