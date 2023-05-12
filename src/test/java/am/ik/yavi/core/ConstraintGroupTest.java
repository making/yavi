/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
