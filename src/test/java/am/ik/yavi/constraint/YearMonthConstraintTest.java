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

import java.time.YearMonth;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class YearMonthConstraintTest {
	@Nested
	class After {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInTheFutureAreAlwaysValid(int monthLater) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.after(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().plusMonths(monthLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInThePastAreAlwaysInvalid(int monthsEarlier) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.after(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().minusMonths(monthsEarlier))).isFalse();
		}

		@Test
		void equalMonthIsInvalid() {
			YearMonth thisMonth = YearMonth.now();
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.after(thisMonth));
			assertThat(predicate.test(thisMonth)).isFalse();
		}
	}

	@Nested
	class OnOrAfter {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInTheFutureAreAlwaysValid(int monthLater) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrAfter(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().plusMonths(monthLater))).isTrue();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInThePastAreAlwaysInvalid(int monthsEarlier) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrAfter(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().minusMonths(monthsEarlier))).isFalse();
		}

		@Test
		void equalMonthIsInvalid() {
			YearMonth thisMonth = YearMonth.now();
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrAfter(thisMonth));
			assertThat(predicate.test(thisMonth)).isTrue();
		}
	}

	@Nested
	class Before {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInTheFutureAreAlwaysInvalid(int monthLater) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.before(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().plusMonths(monthLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInThePastAreAlwaysValid(int monthsEarlier) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.before(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().minusMonths(monthsEarlier))).isTrue();
		}

		@Test
		void equalMonthIsInvalid() {
			YearMonth thisMonth = YearMonth.now();
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.before(thisMonth));
			assertThat(predicate.test(thisMonth)).isFalse();
		}
	}

	@Nested
	class OnOrBefore {
		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInTheFutureAreAlwaysInvalid(int monthLater) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrBefore(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().plusMonths(monthLater))).isFalse();
		}

		@ParameterizedTest
		@ValueSource(ints = {100, 1000})
		void monthsInThePastAreAlwaysValid(int monthsEarlier) {
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrBefore(YearMonth.now()));
			assertThat(predicate.test(YearMonth.now().minusMonths(monthsEarlier))).isTrue();
		}

		@Test
		void equalMonthIsValid() {
			YearMonth thisMonth = YearMonth.now();
			Predicate<YearMonth> predicate = retrievePredicate(c -> c.onOrBefore(thisMonth));
			assertThat(predicate.test(thisMonth)).isTrue();
		}
	}

	private Predicate<YearMonth> retrievePredicate(Function<YearMonthConstraint<YearMonth>, YearMonthConstraint<YearMonth>> constraint) {
		return constraint.apply(new YearMonthConstraint<>()).predicates().getFirst().predicate();
	}
}
