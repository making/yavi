package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharSequenceConstraintTest {
	private CharSequenceConstraint<String, String> constraint = new CharSequenceConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<String> predicate = constraint.notEmpty().predicates().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
	}

	@Test
	public void notBlank() {
		Predicate<String> predicate = constraint.notBlank().predicates().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
		assertThat(predicate.test("    ")).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<String> predicate = constraint.lessThan(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<String> predicate = constraint.lessThanOrEquals(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("abcd")).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<String> predicate = constraint.greaterThan(3).predicates().get(0)
				.predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<String> predicate = constraint.greaterThanOrEquals(3).predicates()
				.get(0).predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("ab")).isFalse();
	}

	@Test
	public void contains() {
		Predicate<String> predicate = constraint.contains("a").predicates().get(0)
				.predicate();
		assertThat(predicate.test("yavi")).isTrue();
		assertThat(predicate.test("yvi")).isFalse();
	}

	@Test
	public void email() {
		Predicate<String> predicate = constraint.email().predicates().get(0).predicate();
		assertThat(predicate.test("abc@example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
	}

	@Test
	public void url() {
		Predicate<String> predicate = constraint.url().predicates().get(0).predicate();
		assertThat(predicate.test("http://example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
	}

	@Test
	public void pattern() {
		Predicate<String> predicate = constraint.pattern("[0-9]{4}").predicates().get(0)
				.predicate();
		assertThat(predicate.test("1234")).isTrue();
		assertThat(predicate.test("134a")).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<String> predicate = constraint.fixedSize(2).predicates().get(0)
				.predicate();
		assertThat(predicate.test("a")).isFalse();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}
}