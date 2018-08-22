package am.ik.yavi.constraint.array;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharArrayConstraintTest {
	private CharArrayConstraint<char[]> constraint = new CharArrayConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<char[]> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] {})).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<char[]> predicate = constraint.lessThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<char[]> predicate = constraint.lessThanOrEquals(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<char[]> predicate = constraint.greaterThan(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isTrue();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<char[]> predicate = constraint.greaterThanOrEquals(2).predicates()
				.get(0).predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isTrue();
	}

	@Test
	public void contains() {
		Predicate<char[]> predicate = constraint.contains((char) 100).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 101, (char) 102 })).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<char[]> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test(new char[] { (char) 100 })).isFalse();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101 })).isTrue();
		assertThat(predicate.test(new char[] { (char) 100, (char) 101, (char) 102 }))
				.isFalse();
	}
}