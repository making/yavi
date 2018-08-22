package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShortConstraintTest {
	private ShortConstraint<Short> constraint = new ShortConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Short> predicate = constraint.greaterThan((short) 100).predicates()
				.get(0).predicate();
		assertThat(predicate.test((short) 101)).isTrue();
		assertThat(predicate.test((short) 100)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Short> predicate = constraint.greaterThanOrEquals((short) 100)
				.predicates().get(0).predicate();
		assertThat(predicate.test((short) 101)).isTrue();
		assertThat(predicate.test((short) 100)).isTrue();
		assertThat(predicate.test((short) 99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Short> predicate = constraint.lessThan((short) 100).predicates().get(0)
				.predicate();
		assertThat(predicate.test((short) 99)).isTrue();
		assertThat(predicate.test((short) 100)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Short> predicate = constraint.lessThanOrEquals((short) 100).predicates()
				.get(0).predicate();
		assertThat(predicate.test((short) 99)).isTrue();
		assertThat(predicate.test((short) 100)).isTrue();
		assertThat(predicate.test((short) 101)).isFalse();
	}
}