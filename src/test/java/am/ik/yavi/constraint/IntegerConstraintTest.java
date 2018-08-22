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

public class IntegerConstraintTest {
	private IntegerConstraint<Integer> constraint = new IntegerConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Integer> predicate = constraint.greaterThan(100).predicates().get(0)
				.predicate();
		assertThat(predicate.test(101)).isTrue();
		assertThat(predicate.test(100)).isFalse();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<Integer> predicate = constraint.greaterThanOrEqual(100).predicates()
				.get(0).predicate();
		assertThat(predicate.test(101)).isTrue();
		assertThat(predicate.test(100)).isTrue();
		assertThat(predicate.test(99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Integer> predicate = constraint.lessThan(100).predicates().get(0)
				.predicate();
		assertThat(predicate.test(99)).isTrue();
		assertThat(predicate.test(100)).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<Integer> predicate = constraint.lessThanOrEqual(100).predicates().get(0)
				.predicate();
		assertThat(predicate.test(99)).isTrue();
		assertThat(predicate.test(100)).isTrue();
		assertThat(predicate.test(101)).isFalse();
	}
}