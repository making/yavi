package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DoubleArrayConstraintTest {
	private DoubleArrayConstraint<double[]> constraint = new DoubleArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<double[]> predicate = constraint.notEmpty().holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<double[]> predicate = constraint.lessThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<double[]> predicate = constraint.lessThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<double[]> predicate = constraint.greaterThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<double[]> predicate = constraint.greaterThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isTrue();
	}

	@Test
	public void contains() {
		Predicate<double[]> predicate = constraint.contains(100.0).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 101.0, 102.0 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<double[]> predicate = constraint.fixedSize(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new double[] { 100.0 })).isFalse();
		assertThat(predicate.test(new double[] { 100.0, 101.0 })).isTrue();
		assertThat(predicate.test(new double[] { 100.0, 101.0, 102.0 })).isFalse();
	}
}