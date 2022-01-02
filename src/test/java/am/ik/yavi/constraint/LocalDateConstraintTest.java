package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
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

import static java.time.DayOfWeek.SATURDAY;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateConstraintTest {
	@Test
	void isPastValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalDate.now().minus(60, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalDate.now().plus(60, ChronoUnit.DAYS))).isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(LocalDate.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalDate.now().minus(60, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalDate.now().plus(60, ChronoUnit.DAYS))).isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.pastOrPresent(clock));
		assertThat(predicate.test(LocalDate.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalDate.now().plus(60, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalDate.now().minus(60, ChronoUnit.DAYS))).isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(LocalDate.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalDate.now().plus(60, ChronoUnit.DAYS))).isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalDate.now().minus(60, ChronoUnit.DAYS))).isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.futureOrPresent(clock));
		assertThat(predicate.test(LocalDate.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalDate now = LocalDate.now();
		LocalDate past = now.minusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		LocalDate now = LocalDate.now();
		LocalDate past = now.minusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalDate now, LocalDate rangeFrom, LocalDate rangeTo) {
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		LocalDate now = LocalDate.now();
		Supplier<LocalDate> nowSupplier = () -> now;

		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		LocalDate now = LocalDate.now();

		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		LocalDate value = LocalDate.of(2022, 1, 1);
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		LocalDate value = LocalDate.of(2022, 1, 2);
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isFalse();
	}

	private static Stream<Arguments> validBetweenDates() {
		return IntStream.rangeClosed(1, 10).boxed().map(i -> {
			LocalDate now = LocalDate.now();
			return Arguments.of(now, now.minusDays(i), now.plusDays(i));
		});
	}

	private static Predicate<LocalDate> retrievePredicate(
			Function<LocalDateConstraint<LocalDate>, LocalDateConstraint<LocalDate>> constraint) {
		return constraint.apply(new LocalDateConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
