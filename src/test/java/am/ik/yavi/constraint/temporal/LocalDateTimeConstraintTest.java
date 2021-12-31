package am.ik.yavi.constraint.temporal;

import java.time.LocalDateTime;
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
	void isBeforeSupplierValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.before(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeSupplierInValid() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime past = now.minusDays(10);
		Predicate<LocalDateTime> predicate = retrievePredicate(c -> c.before(() -> past));
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

	@Test
	void isAfterSuplierInValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.plusDays(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterSuplierValid() {
		LocalDateTime now = LocalDateTime.now();
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.after(() -> now.minusDays(10)));
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

	@ParameterizedTest
	@MethodSource("validBetweenDates")
	void isBetweenSuplierValid(LocalDateTime now, LocalDateTime rangeFrom,
			LocalDateTime rangeTo) {
		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(() -> rangeFrom, () -> rangeTo));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenSuplierExactInValid() {
		LocalDateTime now = LocalDateTime.now();
		Supplier<LocalDateTime> nowSupplier = () -> now;

		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenSuplierInValidException() {
		LocalDateTime now = LocalDateTime.now();

		Predicate<LocalDateTime> predicate = retrievePredicate(
				c -> c.between(() -> now.plusDays(1), () -> now.minusDays(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void supplierMessage() {
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
