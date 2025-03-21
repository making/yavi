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
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static java.time.temporal.ChronoField.YEAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearConstraintTest {

	@Test
	void isPastValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(Year.now().minus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(Year.now().plus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Year> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(Year.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(Year.now().minus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(Year.now().plus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Year> predicate = retrievePredicate(c -> c.pastOrPresent(clock));
		assertThat(predicate.test(Year.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(Year.now().plus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(Year.now().minus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Year> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(Year.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(Year.now().plus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(Year.now().minus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Year> predicate = retrievePredicate(c -> c.futureOrPresent(clock));
		assertThat(predicate.test(Year.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.before(() -> now.plusYears(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		Year now = Year.now();
		Year past = now.minusYears(10);
		Predicate<Year> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.after(() -> now.plusYears(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.after(() -> now.minusYears(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> now.plusYears(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		Year now = Year.now();
		Year past = now.minusYears(10);
		Predicate<Year> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now.plusYears(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now.minusYears(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactValid() {
		Year now = Year.now();
		Predicate<Year> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		Year now = Year.now();
		Supplier<Year> nowSupplier = () -> now;

		Predicate<Year> predicate = retrievePredicate(c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Year now = Year.now();

		Predicate<Year> predicate = retrievePredicate(c -> c.between(() -> now.plusYears(1), () -> now.minusYears(1)));
		assertThatThrownBy(() -> predicate.test(now)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		Predicate<Year> predicate = retrievePredicate(c -> c.fieldPredicate(YEAR, s -> s >= 0));
		assertThat(predicate.test(Year.of(999_999_999))).isTrue();
	}

	private static Predicate<Year> retrievePredicate(Function<YearConstraint<Year>, YearConstraint<Year>> constraint) {
		return constraint.apply(new YearConstraint<>()).predicates().peekFirst().predicate();
	}

}
