package am.ik.yavi.constraint;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

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
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.before(() -> now.plusSeconds(10)));
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
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.after(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.after(() -> now.minusSeconds(10)));
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
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeOrEqualInValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.beforeOrEqual(() -> past));
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
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterOrEqualValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.afterOrEqual(() -> now.minusSeconds(10)));
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

		Predicate<Instant> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Instant now = Instant.now();

		Predicate<Instant> predicate = retrievePredicate(
				c -> c.between(() -> now.plusSeconds(1), () -> now.minusSeconds(1)));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	private static Predicate<Instant> retrievePredicate(
			Function<InstantConstraint<Instant>, InstantConstraint<Instant>> constraint) {
		return constraint.apply(new InstantConstraint<>()).predicates().peekFirst()
				.predicate();
	}
}
