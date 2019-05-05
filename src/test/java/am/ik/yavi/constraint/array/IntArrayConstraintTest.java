/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntArrayConstraintTest {
	private IntArrayConstraint<int[]> constraint = new IntArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<int[]> predicate = constraint.notEmpty().predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<int[]> predicate = constraint.lessThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<int[]> predicate = constraint.lessThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<int[]> predicate = constraint.greaterThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<int[]> predicate = constraint.greaterThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	public void contains() {
		Predicate<int[]> predicate = constraint.contains(100).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 101, 102 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<int[]> predicate = constraint.fixedSize(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}
}
