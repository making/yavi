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
package am.ik.yavi.constraint.charsequence;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.constraint.CharSequenceConstraint;

class ByteSizeConstraintTest {
	private CharSequenceConstraint<String, String> constraint = new CharSequenceConstraint<>();

	@Test
	void fixedSize() {
		Predicate<String> predicate = constraint.asByteArray().fixedSize(4).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
		assertThat(predicate.test("あ")).isFalse(); // 3
		assertThat(predicate.test("あa")).isTrue(); // 4
		assertThat(predicate.test("\uD842\uDFB7")).isTrue(); // 4
	}

	@Test
	void greaterThan() {
		Predicate<String> predicate = constraint.asByteArray().greaterThan(3).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
		assertThat(predicate.test("あ")).isFalse(); // 3
	}

	@Test
	void greaterThanOrEqual() {
		Predicate<String> predicate = constraint.asByteArray().greaterThanOrEqual(3)
				.predicates().peekFirst().predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("ab")).isFalse();
		assertThat(predicate.test("あ")).isTrue(); // 3
	}

	@Test
	void lessThan() {
		Predicate<String> predicate = constraint.asByteArray().lessThan(3).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
		assertThat(predicate.test("あ")).isFalse(); // 3
	}

	@Test
	void lessThanOrEqual() {
		Predicate<String> predicate = constraint.asByteArray().lessThanOrEqual(3)
				.predicates().peekFirst().predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("abcd")).isFalse();
		assertThat(predicate.test("あ")).isTrue(); // 3
		assertThat(predicate.test("あa")).isFalse(); // 4
	}
}
