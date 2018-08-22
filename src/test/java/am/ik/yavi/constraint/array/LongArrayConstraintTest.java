package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LongArrayConstraintTest {
	private LongArrayConstraint<long[]> constraint = new LongArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<long[]> predicate = constraint.notEmpty().holders().get(0).predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<long[]> predicate = constraint.lessThan(2).holders().get(0).predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<long[]> predicate = constraint.lessThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new long[] { 100L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<long[]> predicate = constraint.greaterThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new long[] { 100L, 101L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<long[]> predicate = constraint.greaterThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new long[] { 100L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isTrue();
	}

	@Test
	public void contains() {
		Predicate<long[]> predicate = constraint.contains(100L).holders().get(0)
				.predicate();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 101L, 102L })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<long[]> predicate = constraint.fixedSize(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new long[] { 100L })).isFalse();
		assertThat(predicate.test(new long[] { 100L, 101L })).isTrue();
		assertThat(predicate.test(new long[] { 100L, 101L, 102L })).isFalse();
	}
}