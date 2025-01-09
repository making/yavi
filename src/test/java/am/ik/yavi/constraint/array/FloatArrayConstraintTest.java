/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
