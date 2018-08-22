package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ObjectArrayConstraintTest {
	private ObjectArrayConstraint<String[], String> constraint = new ObjectArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<String[]> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<String[]> predicate = constraint.lessThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<String[]> predicate = constraint.lessThanOrEquals(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<String[]> predicate = constraint.greaterThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<String[]> predicate = constraint.greaterThanOrEquals(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(new String[] { "foo" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isTrue();
	}

	@Test
	public void contains() {
		Predicate<String[]> predicate = constraint.contains("foo").predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "bar", "baz" })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<String[]> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new String[] { "foo" })).isFalse();
		assertThat(predicate.test(new String[] { "foo", "bar" })).isTrue();
		assertThat(predicate.test(new String[] { "foo", "bar", "baz" })).isFalse();
	}
}