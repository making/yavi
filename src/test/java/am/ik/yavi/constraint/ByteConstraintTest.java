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

public class ByteConstraintTest {
	private ByteConstraint<Byte> constraint = new ByteConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Byte> predicate = constraint.greaterThan((byte) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((byte) 101)).isTrue();
		assertThat(predicate.test((byte) 100)).isFalse();
	}

	@Test
	public void greaterThanOrEqual() {
		Predicate<Byte> predicate = constraint.greaterThanOrEqual((byte) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((byte) 101)).isTrue();
		assertThat(predicate.test((byte) 100)).isTrue();
		assertThat(predicate.test((byte) 99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Byte> predicate = constraint.lessThan((byte) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((byte) 99)).isTrue();
		assertThat(predicate.test((byte) 100)).isFalse();
	}

	@Test
	public void lessThanOrEqual() {
		Predicate<Byte> predicate = constraint.lessThanOrEqual((byte) 100).predicates()
				.peekFirst().predicate();
		assertThat(predicate.test((byte) 99)).isTrue();
		assertThat(predicate.test((byte) 100)).isTrue();
		assertThat(predicate.test((byte) 101)).isFalse();
	}
}