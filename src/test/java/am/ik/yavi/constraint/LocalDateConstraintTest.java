/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class LocalDateConstraintTest {
	@Nested
	class After {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInTheFutureAreAlwaysValid(int daysLater) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().plusDays(daysLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInThePastAreAlwaysInvalid(int daysEarlier) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().minusDays(daysEarlier))).isFalse();
		}

		@Test
		void equalDateIsInvalid() {
			LocalDate today = LocalDate.now();
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(today));
			assertThat(predicate.test(today)).isFalse();
		}
	}

	@Nested
	class OnOrAfter {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInTheFutureAreAlwaysValid(int daysLater) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrAfter(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().plusDays(daysLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInThePastAreAlwaysInvalid(int daysEarlier) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrAfter(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().minusDays(daysEarlier))).isFalse();
		}

		@Test
		void equalDateIsInvalid() {
			LocalDate today = LocalDate.now();
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrAfter(today));
			assertThat(predicate.test(today)).isTrue();
		}
	}

	@Nested
	class Before {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInTheFutureAreAlwaysInvalid(int daysLater) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().plusDays(daysLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInThePastAreAlwaysValid(int daysEarlier) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().minusDays(daysEarlier))).isTrue();
		}

		@Test
		void equalDateIsInvalid() {
			LocalDate today = LocalDate.now();
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(today));
			assertThat(predicate.test(today)).isFalse();
		}
	}

	@Nested
	class OnOrBefore {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInTheFutureAreAlwaysInvalid(int daysLater) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrBefore(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().plusDays(daysLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void datesInThePastAreAlwaysValid(int daysEarlier) {
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrBefore(LocalDate.now()));
			assertThat(predicate.test(LocalDate.now().minusDays(daysEarlier))).isTrue();
		}

		@Test
		void equalDateIsValid() {
			LocalDate today = LocalDate.now();
			Predicate<LocalDate> predicate = retrievePredicate(c -> c.onOrBefore(today));
			assertThat(predicate.test(today)).isTrue();
		}
	}

	private Predicate<LocalDate> retrievePredicate(Function<LocalDateConstraint<LocalDate>, LocalDateConstraint<LocalDate>> constraint) {
		return constraint.apply(new LocalDateConstraint<>()).predicates().getFirst().predicate();
	}
}
