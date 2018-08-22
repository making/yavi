package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectConstraintTest {
	private ObjectConstraint<String, String> constraint = new ObjectConstraint<>();

	@Test
	public void notNull() {
		Predicate<String> predicate = constraint.notNull().predicates().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test(null)).isFalse();
	}

	@Test
	public void isNull() {
		Predicate<String> predicate = constraint.isNull().predicates().get(0).predicate();
		assertThat(predicate.test("foo")).isFalse();
		assertThat(predicate.test(null)).isTrue();
	}
}