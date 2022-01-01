package am.ik.yavi.constraint;

import java.time.LocalTime;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalTimeConstraintTest {

	private static final LocalTime BASE_TIME = LocalTime.of(12, 30);

	@Test
	void isBeforeValid() {
		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.before(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalTime past = BASE_TIME.minusHours(10);
		Predicate<LocalTime> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isAfterInValid() {
		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.after(() -> BASE_TIME.plusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isAfterValid() {
		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.after(() -> BASE_TIME.minusHours(10)));
		assertThat(predicate.test(BASE_TIME)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalTime now, LocalTime rangeFrom, LocalTime rangeTo) {
		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		Supplier<LocalTime> nowSupplier = () -> BASE_TIME;

		Predicate<LocalTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(BASE_TIME)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Predicate<LocalTime> predicate = retrievePredicate(c -> c
				.between(() -> BASE_TIME.plusHours(1), () -> BASE_TIME.minusHours(1)));
		assertThatThrownBy(() -> predicate.test(BASE_TIME))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	private static Stream<Arguments> validBetweenDates() {
		return IntStream.rangeClosed(1, 10).boxed().map(i -> Arguments.of(BASE_TIME,
				BASE_TIME.minusHours(i), BASE_TIME.plusHours(i)));
	}

	private static Predicate<LocalTime> retrievePredicate(
			Function<LocalTimeConstraint<LocalTime>, LocalTimeConstraint<LocalTime>> constraint) {
		return constraint.apply(new LocalTimeConstraint<>()).predicates().peekFirst()
				.predicate();
	}

}
