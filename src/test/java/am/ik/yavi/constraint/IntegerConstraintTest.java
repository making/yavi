package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegerConstraintTest {
	private IntegerConstraint<Integer> constraint = new IntegerConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Integer> predicate = constraint.greaterThan(100).holders().get(0)
				.predicate();
		assertThat(predicate.test(101)).isTrue();
		assertThat(predicate.test(100)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Integer> predicate = constraint.greaterThanOrEquals(100).holders()
				.get(0).predicate();
		assertThat(predicate.test(101)).isTrue();
		assertThat(predicate.test(100)).isTrue();
		assertThat(predicate.test(99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Integer> predicate = constraint.lessThan(100).holders().get(0)
				.predicate();
		assertThat(predicate.test(99)).isTrue();
		assertThat(predicate.test(100)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Integer> predicate = constraint.lessThanOrEquals(100).holders().get(0)
				.predicate();
		assertThat(predicate.test(99)).isTrue();
		assertThat(predicate.test(100)).isTrue();
		assertThat(predicate.test(101)).isFalse();
	}
}