package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LongConstraintTest {
	private LongConstraint<Long> constraint = new LongConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Long> predicate = constraint.greaterThan(100L).predicates().get(0)
				.predicate();
		assertThat(predicate.test(101L)).isTrue();
		assertThat(predicate.test(100L)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Long> predicate = constraint.greaterThanOrEquals(100L).predicates()
				.get(0).predicate();
		assertThat(predicate.test(101L)).isTrue();
		assertThat(predicate.test(100L)).isTrue();
		assertThat(predicate.test(99L)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Long> predicate = constraint.lessThan(100L).predicates().get(0)
				.predicate();
		assertThat(predicate.test(99L)).isTrue();
		assertThat(predicate.test(100L)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Long> predicate = constraint.lessThanOrEquals(100L).predicates().get(0)
				.predicate();
		assertThat(predicate.test(99L)).isTrue();
		assertThat(predicate.test(100L)).isTrue();
		assertThat(predicate.test(101L)).isFalse();
	}
}