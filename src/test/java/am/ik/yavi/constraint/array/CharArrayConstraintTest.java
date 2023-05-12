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

class CharArrayConstraintTest {
	private CharArrayConstraint<char[]> constraint = new CharArrayConstraint<>();

	@Test
	void contains() {
		Predicate<char[]> predicate = constraint.contains((char) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 101, (char) 102 })).isFalse();
	}

	@Test
	void fixedSize() {
		Predicate<char[]> predicate = constraint.fixedSize(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isFalse();
	}

	@Test
	void greaterThan() {
		Predicate<char[]> predicate = constraint.greaterThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isTrue();
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<char[]> predicate = constraint.greaterThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isTrue();
	}

	@Test
	void lessThan() {
		Predicate<char[]> predicate = constraint.lessThan(2).predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
	}

	@Test
	void lessThanOrEqual() {
		Predicate<char[]> predicate = constraint.lessThanOrEqual(2).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isFalse();
	}

	@Test
	void notEmpty() {
		Predicate<char[]> predicate = constraint.notEmpty().predicates().peekFirst()
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] {})).isFalse();
	}
}
