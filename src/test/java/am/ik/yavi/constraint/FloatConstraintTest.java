/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FloatConstraintTest {
	private FloatConstraint<Float> constraint = new FloatConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Float> predicate = constraint.greaterThan(100.0f).predicates().get(0)
				.predicate();
		assertThat(predicate.test(101.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Float> predicate = constraint.greaterThanOrEquals(100.0f).predicates()
				.get(0).predicate();
		assertThat(predicate.test(101.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isTrue();
		assertThat(predicate.test(99.0f)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Float> predicate = constraint.lessThan(100.0f).predicates().get(0)
				.predicate();
		assertThat(predicate.test(99.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Float> predicate = constraint.lessThanOrEquals(100.0f).predicates()
				.get(0).predicate();
		assertThat(predicate.test(99.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isTrue();
		assertThat(predicate.test(101.0f)).isFalse();
	}
}