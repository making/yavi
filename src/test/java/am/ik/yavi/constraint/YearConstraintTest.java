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

import java.time.Year;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class YearConstraintTest {
	@Nested
	class After {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInTheFutureAreAlwaysValid(int yearsLater) {
			Predicate<Year> predicate = retrievePredicate(c -> c.after(Year.now()));
			assertThat(predicate.test(Year.now().plusYears(yearsLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInThePastAreAlwaysInvalid(int yearsEarlier) {
			Predicate<Year> predicate = retrievePredicate(c -> c.after(Year.now()));
			assertThat(predicate.test(Year.now().minusYears(yearsEarlier))).isFalse();
		}

		@Test
		void equalYearIsInvalid() {
			Year thisYear = Year.now();
			Predicate<Year> predicate = retrievePredicate(c -> c.after(thisYear));
			assertThat(predicate.test(thisYear)).isFalse();
		}
	}

	@Nested
	class OnOrAfter {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInTheFutureAreAlwaysValid(int yearsLater) {
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrAfter(Year.now()));
			assertThat(predicate.test(Year.now().plusYears(yearsLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInThePastAreAlwaysInvalid(int yearsEarlier) {
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrAfter(Year.now()));
			assertThat(predicate.test(Year.now().minusYears(yearsEarlier))).isFalse();
		}

		@Test
		void equalYearIsInvalid() {
			Year thisYear = Year.now();
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrAfter(thisYear));
			assertThat(predicate.test(thisYear)).isTrue();
		}
	}

	@Nested
	class Before {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInTheFutureAreAlwaysInvalid(int yearsLater) {
			Predicate<Year> predicate = retrievePredicate(c -> c.before(Year.now()));
			assertThat(predicate.test(Year.now().plusYears(yearsLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInThePastAreAlwaysValid(int yearsEarlier) {
			Predicate<Year> predicate = retrievePredicate(c -> c.before(Year.now()));
			assertThat(predicate.test(Year.now().minusYears(yearsEarlier))).isTrue();
		}

		@Test
		void equalYearIsInvalid() {
			Year thisYear = Year.now();
			Predicate<Year> predicate = retrievePredicate(c -> c.before(thisYear));
			assertThat(predicate.test(thisYear)).isFalse();
		}
	}

	@Nested
	class OnOrBefore {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInTheFutureAreAlwaysInvalid(int yearsLater) {
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrBefore(Year.now()));
			assertThat(predicate.test(Year.now().plusYears(yearsLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void yearsInThePastAreAlwaysValid(int yearsEarlier) {
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrBefore(Year.now()));
			assertThat(predicate.test(Year.now().minusYears(yearsEarlier))).isTrue();
		}

		@Test
		void equalYearIsValid() {
			Year thisYear = Year.now();
			Predicate<Year> predicate = retrievePredicate(c -> c.onOrBefore(thisYear));
			assertThat(predicate.test(thisYear)).isTrue();
		}
	}

	private Predicate<Year> retrievePredicate(Function<YearConstraint<Year>, YearConstraint<Year>> constraint) {
		return constraint.apply(new YearConstraint<>()).predicates().getFirst().predicate();
	}
}
