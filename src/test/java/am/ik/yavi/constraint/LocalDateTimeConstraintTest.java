package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import am.ik.yavi.arguments.LocalDateTimeValidator;
import am.ik.yavi.builder.LocalDateTimeValidatorBuilder;
import am.ik.yavi.core.Validated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateTimeConstraintTest {
	@Test
	void isPastValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastInValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.past());
		assertThat(predicate.test(LocalDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.past(clock));
		assertThat(predicate.test(LocalDateTime.now(clock))).isFalse();
	}

	@Test
	void isPastOrPresentValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isPastOrPresentInValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.pastOrPresent());
		assertThat(predicate.test(LocalDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isPastOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.pastOrPresent(clock));
		assertThat(predicate.test(LocalDateTime.now(clock))).isTrue();
	}

	@Test
	void isFutureValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureInValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.future());
		assertThat(predicate.test(LocalDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.future(clock));
		assertThat(predicate.test(LocalDateTime.now(clock))).isFalse();
	}

	@Test
	void isFutureOrPresentValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalDateTime.now().plus(60, ChronoUnit.SECONDS)))
				.isTrue();
	}

	@Test
	void isFutureOrPresentInValid() {
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.futureOrPresent());
		assertThat(predicate.test(LocalDateTime.now().minus(60, ChronoUnit.SECONDS)))
				.isFalse();
	}

	@Test
	void isFutureOrPresentExactInValid() {
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.futureOrPresent(clock));
		assertThat(predicate.test(LocalDateTime.now(clock))).isTrue();
	}

	@Test
	void isBeforeValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime past = now.minusDays(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.before(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.after(() -> now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime past = now.minusDays(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeOrEqualExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterOrEqualExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalDateTime now, LocalDateTime rangeFrom,
			LocalDateTime rangeTo) {
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Supplier<LocalDateTime> nowSupplier = () -> now;

		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		LocalDateTime now = LocalDateTime.now();

		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void temporalFieldValid() {
		LocalDateTime value = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isTrue();
	}

	@Test
	void temporalFieldInValid() {
		LocalDateTime value = LocalDateTime.of(2022, 1, 2, 0, 0, 0);
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.fieldPredicate(DAY_OF_WEEK, week -> week == SATURDAY.getValue()));
		assertThat(predicate.test(value)).isFalse();
	}

	@Test
	void Message() {
		final AtomicReference<LocalDateTime> saved = new AtomicReference<>();
		final LocalDateTimeValidator<LocalDateTime> validator = LocalDateTimeValidatorBuilder
				.of("now", c -> c.after(() -> {
					final LocalDateTime now = LocalDateTime.now();
					saved.set(now);
					return now;
				})).build();
		final Validated<LocalDateTime> validated = validator
				.validate(LocalDateTime.now());
		assertThat(validated.isValid()).isFalse();
		assertThat(validated.errors().get(0).message())
				.isEqualTo("\"now\" has to be after " + saved.get());
	}

	private static Stream<Arguments> validBetweenDates() {
		return IntStream.rangeClosed(1, 10).boxed().map(i -> {
			LocalDateTime now = LocalDateTime.now();
			return Arguments.of(now, now.minusHours(i), now.plusHours(i));
		});
	}

	private static Predicate<LocalDateTime> retrievePredicate(
			Function<LocalDateTimeConstraint<LocalDateTime>, LocalDateTimeConstraint<LocalDateTime>> constraint) {
		return constraint.apply(new LocalDateTimeConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
