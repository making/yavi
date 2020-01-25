package am.ik.yavi.core;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.message.SimpleMessageFormatter;

class ConstraintViolationsExceptionTest {

	@Test
	void customMessage() {
		final ConstraintViolations violations = new ConstraintViolations();
		final SimpleMessageFormatter messageFormatter = new SimpleMessageFormatter();
		violations.add(new ConstraintViolation("name1", "key", "{0} is invalid.",
				new Object[] { "a" }, messageFormatter, Locale.ENGLISH));
		final ConstraintViolationsException exception = new ConstraintViolationsException(
				"error!", violations);
		assertThat(exception.getMessage()).isEqualTo("error!");
	}

	@Test
	void defaultMessage() {
		final ConstraintViolations violations = new ConstraintViolations();
		final SimpleMessageFormatter messageFormatter = new SimpleMessageFormatter();
		violations.add(new ConstraintViolation("name1", "key", "{0} is invalid.",
				new Object[] { "a" }, messageFormatter, Locale.ENGLISH));
		final ConstraintViolationsException exception = new ConstraintViolationsException(
				violations);
		assertThat(exception.getMessage()).isEqualTo("Constraint violations found!"
				+ System.lineSeparator() + "* a is invalid.");
	}
}