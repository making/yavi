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
package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharacterConstraintTest {
	private CharacterConstraint<Character> constraint = new CharacterConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Character> predicate = constraint.greaterThan((char) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((char) 101)).isTrue();
		assertThat(predicate.test((char) 100)).isFalse();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<Character> predicate = constraint.greaterThanOrEqual((char) 100)
				.predicates().peekFirst().predicate();
		assertThat(predicate.test((char) 101)).isTrue();
		assertThat(predicate.test((char) 100)).isTrue();
		assertThat(predicate.test((char) 99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Character> predicate = constraint.lessThan((char) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((char) 99)).isTrue();
		assertThat(predicate.test((char) 100)).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<Character> predicate = constraint.lessThanOrEqual((char) 100)
				.predicates().peekFirst().predicate();
		assertThat(predicate.test((char) 99)).isTrue();
		assertThat(predicate.test((char) 100)).isTrue();
		assertThat(predicate.test((char) 101)).isFalse();
	}
}
