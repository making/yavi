package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BooleanConstraintTest {
	private BooleanConstraint<Boolean> constraint = new BooleanConstraint<>();

	@Test
	public void isTrue() {
		Predicate<Boolean> predicate = constraint.isTrue().predicates().get(0)
				.predicate();
		assertThat(predicate.test(true)).isTrue();
		assertThat(predicate.test(false)).isFalse();
	}

	@Test
	public void isFalse() {
		Predicate<Boolean> predicate = constraint.isFalse().predicates().get(0)
				.predicate();
		assertThat(predicate.test(true)).isFalse();
		assertThat(predicate.test(false)).isTrue();
	}
}