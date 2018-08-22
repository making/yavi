package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntArrayConstraintTest {
	private IntArrayConstraint<int[]> constraint = new IntArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<int[]> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<int[]> predicate = constraint.lessThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<int[]> predicate = constraint.lessThanOrEquals(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<int[]> predicate = constraint.greaterThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<int[]> predicate = constraint.greaterThanOrEquals(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isTrue();
	}

	@Test
	public void contains() {
		Predicate<int[]> predicate = constraint.contains(100).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 101, 102 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<int[]> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new int[] { 100 })).isFalse();
		assertThat(predicate.test(new int[] { 100, 101 })).isTrue();
		assertThat(predicate.test(new int[] { 100, 101, 102 })).isFalse();
	}
}