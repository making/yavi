package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh226Test {

	private static final Validator<String> recipientValidator = ValidatorBuilder.<String>of()
		.constraint(String::toString, "aaa", e -> e.notNull().notBlank().notEmpty().email())
		.build();

	private static final Validator<Email> baseEmailValidatorBuilder = ValidatorBuilder.<Email>of()
		.forEach(Email::getRecipients, "recipients", recipientValidator)
		.build();

	@Test
	void nullElement() {
		Email e = new Email();
		String nullVal = null;
		e.setRecipients(Arrays.asList(nullVal));
		ConstraintViolations violations = baseEmailValidatorBuilder.validate(e);
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message()).isEqualTo("\"recipients[0]\" must not be null");
	}

	public class Email {

		private List<String> recipients;

		public void setRecipients(List<String> recipients) {
			this.recipients = recipients;
		}

		public List<String> getRecipients() {
			return this.recipients;
		}

	}

}
