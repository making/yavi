package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteConstraintTest {
	private ByteConstraint<Byte> constraint = new ByteConstraint<>();

	@Test
	public void greaterThan() {
		Predicate<Byte> predicate = constraint.greaterThan((byte) 100).predicates().get(0)
				.predicate();
		assertThat(predicate.test((byte) 101)).isTrue();
		assertThat(predicate.test((byte) 100)).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<Byte> predicate = constraint.greaterThanOrEquals((byte) 100)
				.predicates().get(0).predicate();
		assertThat(predicate.test((byte) 101)).isTrue();
		assertThat(predicate.test((byte) 100)).isTrue();
		assertThat(predicate.test((byte) 99)).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<Byte> predicate = constraint.lessThan((byte) 100).predicates().get(0)
				.predicate();
		assertThat(predicate.test((byte) 99)).isTrue();
		assertThat(predicate.test((byte) 100)).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<Byte> predicate = constraint.lessThanOrEquals((byte) 100).predicates()
				.get(0).predicate();
		assertThat(predicate.test((byte) 99)).isTrue();
		assertThat(predicate.test((byte) 100)).isTrue();
		assertThat(predicate.test((byte) 101)).isFalse();
	}
}