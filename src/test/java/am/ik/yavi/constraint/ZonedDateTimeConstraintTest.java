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
package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ZonedDateTimeConstraintTest {
	@Test
	void isPastValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(ZonedDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(ZonedDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(ZonedDateTime.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(ZonedDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(ZonedDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.pastOrPresent(clock));
		assertThat(predicate.test(ZonedDateTime.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(ZonedDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(ZonedDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(ZonedDateTime.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(ZonedDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(ZonedDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.futureOrPresent(clock));
		assertThat(predicate.test(ZonedDateTime.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime past = now.minusDays(10);
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		ZonedDateTime past = now.minusDays(10);
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(ZonedDateTime now, ZonedDateTime rangeFrom,
			ZonedDateTime rangeTo) {
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		ZonedDateTime now = ZonedDateTime.now();
		Supplier<ZonedDateTime> nowSupplier = () -> now;

		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		ZonedDateTime now = ZonedDateTime.now();

		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		ZonedDateTime value = ZonedDateTime.of(2022, 1, 1, 0, 0, 0, 0,
				ZoneOffset.ofHours(9));
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		ZonedDateTime value = ZonedDateTime.of(2022, 1, 2, 0, 0, 0, 0,
				ZoneOffset.ofHours(9));
		Predicate<ZonedDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Stream<Arguments> validBetweenDates() {
		List<Arguments> validBetweenZones = IntStream.rangeClosed(1, 10).boxed()
				.map(i -> ZoneId.SHORT_IDS.values().stream().map(ZoneId::of)
						.map(ZonedDateTime::now)
						.map(zonedDateTime -> Arguments.of(zonedDateTime,
								zonedDateTime.minusHours(i), zonedDateTime.plusHours(i)))
						.collect(Collectors.toList()))
				.flatMap(List::stream).collect(Collectors.toList());
		return validBetweenZones.stream();
	}

	private static Predicate<ZonedDateTime> retrievePredicate(
			Function<ZonedDateTimeConstraint<ZonedDateTime>, ZonedDateTimeConstraint<ZonedDateTime>> constraint) {
		return constraint.apply(new ZonedDateTimeConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
