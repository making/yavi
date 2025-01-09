/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalTimeConstraintTest {

	private static final LocalTime BASE_TIME = LocalTime.of(12, 30);

	@Test
	void isPastValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalTime.now().minus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalTime.now().plus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(LocalTime.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalTime.now().minus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalTime.now().plus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.pastOrPresent(clock));
		assertThat(predicate.test(LocalTime.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalTime.now().plus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalTime.now().minus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(LocalTime.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalTime.now().plus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalTime.now().minus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.futureOrPresent(clock));
		assertThat(predicate.test(LocalTime.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.before(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalTime past = BASE_TIME.minusHours(10);
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.before(() -> BASE_TIME));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isAfterInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.after(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isAfterValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.after(() -> BASE_TIME.minusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.after(() -> BASE_TIME));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		LocalTime past = BASE_TIME.minusHours(10);
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> BASE_TIME));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.afterOrEqual(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.afterOrEqual(() -> BASE_TIME.minusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isAfterOrEqualExactInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.afterOrEqual(() -> BASE_TIME));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalTime now, LocalTime rangeFrom, LocalTime rangeTo) {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		Supplier<LocalTime> nowSupplier = () -> BASE_TIME;

		Predicate<LocalTime> predicate = retrievePredicate(c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.between(() -> BASE_TIME.plusHours(1), () -> BASE_TIME.minusHours(1)));
		assertThatThrownBy(() -> predicate.test(BASE_TIME)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		LocalTime value = LocalTime.of(10, 30, 0);
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.fieldPredicate(MINUTE_OF_HOUR, s -> s % 15 == 0));
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		LocalTime value = LocalTime.of(10, 40, 0);
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.fieldPredicate(MINUTE_OF_HOUR, s -> s % 15 == 0));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Stream<Arguments> validBetweenDates() {
		return IntStream.rangeClosed(1, 10)
			.boxed()
			.map(i -> Arguments.of(BASE_TIME, BASE_TIME.minusHours(i), BASE_TIME.plusHours(i)));
	}

	private static Predicate<LocalTime> retrievePredicate(
			Function<LocalTimeConstraint<LocalTime>, LocalTimeConstraint<LocalTime>> constraint) {
		return constraint.apply(new LocalTimeConstraint<>()).predicates().peekFirst().predicate();
	}

}
