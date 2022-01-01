package am.ik.yavi.constraint;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
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
	void isBeforeValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime future = now.plusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.before(future));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime past = now.minusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.before(past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeSupplierValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeSupplierInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime past = now.minusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime future = now.plusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.after(future));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime past = now.minusDays(10);
		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.after(past));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterSuplierInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterSuplierValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(OffsetDateTime now, OffsetDateTime rangeFrom,
			OffsetDateTime rangeTo) {
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();

		Predicate<OffsetDateTime> predicate = retrievePredicate(c -> c.between(now, now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime rangeTo = now.minusDays(1);
		OffsetDateTime rangeFrom = now.plusDays(1);

		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenSuplierValid(OffsetDateTime now, OffsetDateTime rangeFrom,
			OffsetDateTime rangeTo) {
		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenSuplierExactInValid() {
		OffsetDateTime now = OffsetDateTime.now();
		Supplier<OffsetDateTime> nowSupplier = () -> now;

		Predicate<OffsetDateTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenSuplierInValidException() {
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

	private static Stream<Arguments> allZonesBesideSystemDefault() {
		return ZoneId.SHORT_IDS.values().stream().map(ZoneId::of)
				.filter(zone -> !Objects.equals(zone, ZoneId.systemDefault()))
				.map(Arguments::of);
	}

	private static Predicate<OffsetDateTime> retrievePredicate(
			Function<OffsetDateTimeConstraint<OffsetDateTime>, OffsetDateTimeConstraint<OffsetDateTime>> constraint) {
		return constraint.apply(new OffsetDateTimeConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
