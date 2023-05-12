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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static java.time.temporal.ChronoField.INSTANT_SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstantConstraintTest {

	@Test
	void isPastValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(Instant.now().minus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(Instant.now().plus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Instant> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(Instant.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(Instant.now().minus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(Instant.now().plus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Instant> predicate = retrievePredicate(c -> c.pastOrPresent(clock));
		assertThat(predicate.test(Instant.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(Instant.now().plus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(Instant.now().minus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Instant> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(Instant.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(Instant.now().plus(60, ChronoUnit.SECONDS))).isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<Instant> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(Instant.now().minus(60, ChronoUnit.SECONDS))).isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<Instant> predicate = retrievePredicate(c -> c.futureOrPresent(clock));
		assertThat(predicate.test(Instant.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.after(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.after(() -> now.minusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now.minusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		Instant now = Instant.now();
		Supplier<Instant> nowSupplier = () -> now;

		Predicate<Instant> predicate = retrievePredicate(c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Instant now = Instant.now();

		Predicate<Instant> predicate = retrievePredicate(
				c -> c.between(() -> now.plusSeconds(1), () -> now.minusSeconds(1)));
		assertThatThrownBy(() -> predicate.test(now)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		OffsetDateTime value = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.ofHours(0));
		Predicate<Instant> predicate = retrievePredicate(c -> c.fieldPredicate(INSTANT_SECONDS, s -> s >= 0));
		assertThat(predicate.test(value.toInstant())).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		OffsetDateTime value = OffsetDateTime.of(1969, 12, 31, 23, 59, 59, 0, ZoneOffset.ofHours(0));
		Predicate<Instant> predicate = retrievePredicate(c -> c.fieldPredicate(INSTANT_SECONDS, s -> s >= 0));
		assertThat(predicate.test(value.toInstant())).isFalse();
	}

	private static Predicate<Instant> retrievePredicate(
			Function<InstantConstraint<Instant>, InstantConstraint<Instant>> constraint) {
		return constraint.apply(new InstantConstraint<>()).predicates().peekFirst().predicate();
	}

}
