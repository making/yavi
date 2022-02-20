/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
import java.time.ZoneId;
import java.time.ZoneOffset;
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

class OffsetDateTimeConstraintTest {
	@Test
	void isPastValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(OffsetDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(OffsetDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(OffsetDateTime.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(OffsetDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(OffsetDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.pastOrPresent(clock));
		assertThat(predicate.test(OffsetDateTime.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(OffsetDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(OffsetDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(OffsetDateTime.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(OffsetDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(OffsetDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.futureOrPresent(clock));
		assertThat(predicate.test(OffsetDateTime.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime past = now.minusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime past = now.minusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(OffsetDateTime now, OffsetDateTime rangeFrom,
			OffsetDateTime rangeTo) {
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Supplier<OffsetDateTime> nowSupplier = () -> now;

		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		OffsetDateTime now = OffsetDateTime.now();

		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		OffsetDateTime value = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0,
				ZoneOffset.ofHours(9));
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		OffsetDateTime value = OffsetDateTime.of(2022, 1, 2, 0, 0, 0, 0,
				ZoneOffset.ofHours(9));
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Stream<Arguments> validBetweenDates() {
		List<Arguments> validBetweenZones = IntStream.rangeClosed(1, 10).boxed()
				.map(i -> ZoneId.SHORT_IDS.values().stream().map(ZoneId::of)
						.map(OffsetDateTime::now)
						.map(offsetDateTime -> Arguments.of(offsetDateTime,
								offsetDateTime.minusHours(i),
								offsetDateTime.plusHours(i)))
						.collect(Collectors.toList()))
				.flatMap(List::stream).collect(Collectors.toList());
		return validBetweenZones.stream();
	}

	private static Predicate<OffsetDateTime> retrievePredicate(
			Function<OffsetDateTimeConstraint<OffsetDateTime>, OffsetDateTimeConstraint<OffsetDateTime>> constraint) {
		return constraint.apply(new OffsetDateTimeConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
