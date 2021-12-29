package am.ik.yavi.constraint.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDateConstraintTest {

	@Test
	void isBeforeValid() {
		LocalDate now = LocalDate.now();
		LocalDate future = now.plusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(future));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		LocalDate now = LocalDate.now();
		LocalDate past = now.minusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeSupplierValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeSupplierInValid() {
		LocalDate now = LocalDate.now();
		LocalDate past = now.minusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		LocalDate now = LocalDate.now();
		LocalDate future = now.plusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(future));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		LocalDate now = LocalDate.now();
		LocalDate past = now.minusDays(10);
		Predicate<LocalDate> predicate = retrievePredicate(c -> c.after(past));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterSuplierInValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterSuplierValid() {
		LocalDate now = LocalDate.now();
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenValid(LocalDate now, LocalDate rangeFrom, LocalDate rangeTo) {
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		LocalDate now = LocalDate.now();

		Predicate<LocalDate> predicate = retrievePredicate(c -> c.between(now, now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		LocalDate now = LocalDate.now();
		LocalDate rangeTo = now.minusDays(1);
		LocalDate rangeFrom = now.plusDays(1);

		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenSuplierValid(LocalDate now, LocalDate rangeFrom, LocalDate rangeTo) {
		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenSuplierExactInValid() {
		LocalDate now = LocalDate.now();
		Supplier<LocalDate> nowSupplier = () -> now;

		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenSuplierInValidException() {
		LocalDate now = LocalDate.now();

		Predicate<LocalDate> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
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
