package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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
