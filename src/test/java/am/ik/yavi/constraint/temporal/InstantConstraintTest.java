package am.ik.yavi.constraint.temporal;

import java.time.Instant;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InstantConstraintTest {

	@Test
	void isBeforeValid() {
		Instant now = Instant.now();
		Instant future = now.plusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(future));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeInValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBeforeSupplierValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.before(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBeforeSupplierInValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.before(() -> past));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterInValid() {
		Instant now = Instant.now();
		Instant future = now.plusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.after(future));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterValid() {
		Instant now = Instant.now();
		Instant past = now.minusSeconds(10);
		Predicate<Instant> predicate = retrievePredicate(c -> c.after(past));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isAfterSuplierInValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.after(() -> now.plusSeconds(10)));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isAfterSuplierValid() {
		Instant now = Instant.now();
		Predicate<Instant> predicate = retrievePredicate(
				c -> c.after(() -> now.minusSeconds(10)));
		assertThat(predicate.test(now)).isTrue();
	}

	@Test
	void isBetweenExactInValid() {
		Instant now = Instant.now();

		Predicate<Instant> predicate = retrievePredicate(c -> c.between(now, now));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenInValidException() {
		Instant now = Instant.now();
		Instant rangeTo = now.minusSeconds(1);
		Instant rangeFrom = now.plusSeconds(1);

		Predicate<Instant> predicate = retrievePredicate(
				c -> c.between(rangeFrom, rangeTo));
		assertThatThrownBy(() -> predicate.test(now))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Parameter 'rangeFrom' has to be before 'rangeTo'");
	}

	@Test
	void isBetweenSuplierExactInValid() {
		Instant now = Instant.now();
		Supplier<Instant> nowSupplier = () -> now;

		Predicate<Instant> predicate = retrievePredicate(
				c -> c.between(nowSupplier, nowSupplier));
		assertThat(predicate.test(now)).isFalse();
	}

	@Test
	void isBetweenSuplierInValidException() {
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
