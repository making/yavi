package am.ik.yavi.constraint;

import java.util.function.Predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharSequenceConstraintTest {
	private CharSequenceConstraint<String> constraint = new CharSequenceConstraint<>();

	@Test
	public void notEmpty() {
		Predicate<CharSequence> predicate = constraint.notEmpty().holders().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
	}

	@Test
	public void notBlank() {
		Predicate<CharSequence> predicate = constraint.notBlank().holders().get(0)
				.predicate();
		assertThat(predicate.test("foo")).isTrue();
		assertThat(predicate.test("")).isFalse();
		assertThat(predicate.test("    ")).isFalse();
	}

	@Test
	public void lessThan() {
		Predicate<CharSequence> predicate = constraint.lessThan(3).holders().get(0)
				.predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}

	@Test
	public void lessThanOrEquals() {
		Predicate<CharSequence> predicate = constraint.lessThanOrEquals(3).holders()
				.get(0).predicate();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("abcd")).isFalse();
	}

	@Test
	public void greaterThan() {
		Predicate<CharSequence> predicate = constraint.greaterThan(3).holders().get(0)
				.predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}

	@Test
	public void greaterThanOrEquals() {
		Predicate<CharSequence> predicate = constraint.greaterThanOrEquals(3).holders()
				.get(0).predicate();
		assertThat(predicate.test("abcd")).isTrue();
		assertThat(predicate.test("abc")).isTrue();
		assertThat(predicate.test("ab")).isFalse();
	}

	@Test
	public void contains() {
		Predicate<CharSequence> predicate = constraint.contains("a").holders().get(0)
				.predicate();
		assertThat(predicate.test("yavi")).isTrue();
		assertThat(predicate.test("yvi")).isFalse();
	}

	@Test
	public void email() {
		Predicate<CharSequence> predicate = constraint.email().holders().get(0)
				.predicate();
		assertThat(predicate.test("abc@example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
	}

	@Test
	public void url() {
		Predicate<CharSequence> predicate = constraint.url().holders().get(0).predicate();
		assertThat(predicate.test("http://example.com")).isTrue();
		assertThat(predicate.test("example.com")).isFalse();
	}

	@Test
	public void pattern() {
		Predicate<CharSequence> predicate = constraint.pattern("[0-9]{4}").holders()
				.get(0).predicate();
		assertThat(predicate.test("1234")).isTrue();
		assertThat(predicate.test("134a")).isFalse();
	}

	@Test
	public void fixedSize() {
		Predicate<CharSequence> predicate = constraint.fixedSize(2).holders().get(0)
				.predicate();
		assertThat(predicate.test("a")).isFalse();
		assertThat(predicate.test("ab")).isTrue();
		assertThat(predicate.test("abc")).isFalse();
	}
}