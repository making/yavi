package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import am.ik.yavi.message.SimpleMessageFormatter;

public class ConstraintViolationsTest {

	@Test
	public void apply() {
		SimpleMessageFormatter messageFormatter = new SimpleMessageFormatter();
		ConstraintViolations violations = new ConstraintViolations();
		violations.add(new ConstraintViolation("foo0", "abc0", "hello0",
				new Object[] { 1 }, "foobar0", messageFormatter, Locale.getDefault()));
		violations.add(new ConstraintViolation("foo1", "abc1", "hello1",
				new Object[] { 1 }, "foobar1", messageFormatter, Locale.getDefault()));

		BindingResult bindingResult = new BindingResult();
		violations.apply(bindingResult::rejectValue);
		assertThat(bindingResult.toString())
				.isEqualTo("[foo0_abc0_[1]_hello0][foo1_abc1_[1]_hello1]");
	}

	static class BindingResult {
		final StringBuilder builder = new StringBuilder();

		public void rejectValue(String field, String errorCode, Object[] errorArgs,
				String defaultMessage) {
			this.builder.append("[").append(field).append("_").append(errorCode)
					.append("_").append(Arrays.toString(errorArgs)).append("_")
					.append(defaultMessage).append("]");
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}
	}
}