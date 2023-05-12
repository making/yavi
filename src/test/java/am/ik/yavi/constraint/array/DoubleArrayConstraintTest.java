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
package am.ik.yavi.constraint.array;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleArrayConstraintTest {
	private DoubleArrayConstraint<double[]> constraint = new DoubleArrayConstraint<>();

	@Test
	void contains() {
		Predicate<double[]> predicate = constraint.contains(100.0).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 101.0, 102.0 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<double[]> predicate = constraint.fixedSize(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<double[]> predicate = constraint.greaterThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<double[]> predicate = constraint.greaterThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<double[]> predicate = constraint.lessThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<double[]> predicate = constraint.lessThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<double[]> predicate = constraint.notEmpty().predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] {})).isFalse();
	}
}
