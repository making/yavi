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

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YearMonthMonthConstraintTest {

	@Test
	void isPastValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(YearMonth.now().minus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(YearMonth.now().plus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(YearMonth.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(YearMonth.now().minus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(YearMonth.now().plus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.pastOrPresent(clock));
		assertThat(predicate.test(YearMonth.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(YearMonth.now().plus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(YearMonth.now().minus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(YearMonth.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(YearMonth.now().plus(60, ChronoUnit.YEARS))).isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(YearMonth.now().minus(60, ChronoUnit.YEARS))).isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.futureOrPresent(clock));
		assertThat(predicate.test(YearMonth.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.before(() -> now.plusMonths(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		YearMonth now = YearMonth.now();
		YearMonth past = now.minusMonths(10);
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.after(() -> now.plusMonths(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.after(() -> now.minusMonths(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusMonths(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		YearMonth now = YearMonth.now();
		YearMonth past = now.minusMonths(10);
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusMonths(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusMonths(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactValid() {
		YearMonth now = YearMonth.now();
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		YearMonth now = YearMonth.now();
		Supplier<YearMonth> nowSupplier = () -> now;

		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		YearMonth now = YearMonth.now();

		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.between(() -> now.plusMonths(1), () -> now.minusMonths(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		OffsetDateTime value = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0,
				ZoneOffset.ofHours(0));
		Predicate<YearMonth> predicate = retrievePredicate(
				c -> c.fieldPredicate(MONTH_OF_YEAR, s -> s >= 0));
		assertThat(predicate.test(YearMonth.from(value))).isTrue();
	}

	private static Predicate<YearMonth> retrievePredicate(
			Function<YearMonthConstraint<YearMonth>, YearMonthConstraint<YearMonth>> constraint) {
		return constraint.apply(new YearMonthConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
