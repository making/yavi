/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
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

class ObjectArrayConstraintTest {

	private ObjectArrayConstraint<String[], String> constraint = new ObjectArrayConstraint<>();

	@Test
	void contains() {
		Predicate<String[]> predicate = constraint.contains("foo").predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "bar", "baz" })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<String[]> predicate = constraint.fixedSize(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<String[]> predicate = constraint.greaterThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<String[]> predicate = constraint.greaterThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isTrue();
	}

	@Test
	void lessThan() {
		Predicate<String[]> predicate = constraint.lessThan(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<String[]> predicate = constraint.lessThanOrEqual(2).predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<String[]> predicate = constraint.notEmpty().predicates().peekFirst().predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] {})).isFalse();
	}

}
