package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectConstraintTest {
	private ObjectConstraint<String> constraint = new ObjectConstraint<>();

	@Test
	public void notNull() {
		Predicate<Object> predicate = constraint.notNull().holders().get(0).predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test(null)).isFalse();
	}

	@Test
	public void isNull() {
		Predicate<Object> predicate = constraint.isNull().holders().get(0).predicate();
		assertThat(predicate.test("foo")).isFalse();
		assertThat(predicate.test(null)).isTrue();
	}
}