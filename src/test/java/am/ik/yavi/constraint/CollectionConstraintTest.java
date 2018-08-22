package am.ik.yavi.constraint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollectionConstraintTest {
	private CollectionConstraint<List<String>, List<String>, String> constraint = new CollectionConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<List<String>> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Collections.emptyList())).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<List<String>> predicate = constraint.lessThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<List<String>> predicate = constraint.lessThanOrEquals(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<List<String>> predicate = constraint.greaterThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<List<String>> predicate = constraint.greaterThanOrEquals(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isTrue();
	}

	@Test
	public void contains() {
		Predicate<List<String>> predicate = constraint.contains("foo").predicates().get(0)
				.predicate();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("bar", "baz"))).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<List<String>> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(Collections.singletonList("foo"))).isFalse();
		assertThat(predicate.test(Arrays.asList("foo", "bar"))).isTrue();
		assertThat(predicate.test(Arrays.asList("foo", "bar", "baz"))).isFalse();
	}
}