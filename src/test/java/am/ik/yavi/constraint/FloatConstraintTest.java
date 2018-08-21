package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FloatConstraintTest {
	private FloatConstraint<Float> constraint = new FloatConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Float> predicate = constraint.greaterThan(100.0f).holders().get(0)
				.predicate();
		assertThat(predicate.test(101.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Float> predicate = constraint.greaterThanOrEquals(100.0f).holders()
				.get(0).predicate();
		assertThat(predicate.test(101.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isTrue();
		assertThat(predicate.test(99.0f)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Float> predicate = constraint.lessThan(100.0f).holders().get(0)
				.predicate();
		assertThat(predicate.test(99.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Float> predicate = constraint.lessThanOrEquals(100.0f).holders().get(0)
				.predicate();
		assertThat(predicate.test(99.0f)).isTrue();
		assertThat(predicate.test(100.0f)).isTrue();
		assertThat(predicate.test(101.0f)).isFalse();
	}
}