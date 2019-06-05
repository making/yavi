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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ByteConstraintTest {

	@ParameterizedTest
	@ValueSource(bytes = {101, 120})
	void validGreaterThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThan((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = {100, -50})
	void invalidGreaterThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThan((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(bytes = {101, 100})
	void validGreaterThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = {99, -50})
	void invalidGreaterThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.greaterThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}


	@ParameterizedTest
	@ValueSource(bytes = {99, -50})
	void validLessThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThan((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = {100, 120})
	void invalidLessThan(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThan((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	@ParameterizedTest
	@ValueSource(bytes = {99, 100})
	void validLessThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(bytes = {101, 120})
	void invalidLessThanOrEqual(byte value) {
		Predicate<Byte> predicate = retrievePredicate(c -> c.lessThanOrEqual((byte) 100));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Predicate<Byte> retrievePredicate(Function<ByteConstraint<Byte>, ByteConstraint<Byte>> constraint) {
		return constraint.apply(new ByteConstraint<>()).predicates().peekFirst().predicate();
	}
}
