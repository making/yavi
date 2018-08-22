package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayConstraintTest {
	private ByteArrayConstraint<byte[]> constraint = new ByteArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<byte[]> predicate = constraint.notEmpty().holders().get(0).predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<byte[]> predicate = constraint.lessThan(2).holders().get(0).predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<byte[]> predicate = constraint.lessThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 }))
				.isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<byte[]> predicate = constraint.greaterThan(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 }))
				.isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<byte[]> predicate = constraint.greaterThanOrEquals(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 }))
				.isTrue();
	}

	@Test
	public void contains() {
		Predicate<byte[]> predicate = constraint.contains((byte) 100).holders().get(0)
				.predicate();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 101, (byte) 102 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<byte[]> predicate = constraint.fixedSize(2).holders().get(0)
				.predicate();
		assertThat(predicate.test(new byte[] { (byte) 100 })).isFalse();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101 })).isTrue();
		assertThat(predicate.test(new byte[] { (byte) 100, (byte) 101, (byte) 102 }))
				.isFalse();
	}
}