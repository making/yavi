package am.ik.yavi.constraint;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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

class ZonedDateTimeConstraintTest {

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
