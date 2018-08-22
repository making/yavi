package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShortArrayConstraintTest {
	private ShortArrayConstraint<short[]> constraint = new ShortArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<short[]> predicate = constraint.notEmpty().holders().get(0).predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<short[]> predicate = constraint.lessThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<short[]> predicate = constraint.lessThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 }))
				.isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<short[]> predicate = constraint.greaterThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 }))
				.isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<short[]> predicate = constraint.greaterThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 }))
				.isTrue();
	}

	@Test
	public void contains() {
		Predicate<short[]> predicate = constraint.contains((short) 100).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 101, (short) 102 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<short[]> predicate = constraint.fixedSize(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new short[] { (short) 100 })).isFalse();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101 })).isTrue();
		assertThat(predicate.test(new short[] { (short) 100, (short) 101, (short) 102 }))
				.isFalse();
	}
}