package am.ik.yavi.constraint.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateTimeConstraintTest {

	@Test
	void isBeforeValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime future = now.plusMinutes(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.before(future));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime past = now.minusMinutes(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.before(past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime future = now.plusMinutes(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.after(future));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime past = now.minusMinutes(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.after(past));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalDateTime now, LocalDateTime rangeFrom,
			LocalDateTime rangeTo) {
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		LocalDateTime now = LocalDateTime.now();

		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.between(now, now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime rangeTo = now.minusDays(1);
		LocalDateTime rangeFrom = now.plusDays(1);

		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
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
