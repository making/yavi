package am.ik.yavi.core;

import am.ik.yavi.message.MessageFormatter;
import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ConstraintViolation} class and its builder pattern.
 */
class ConstraintViolationTest {

	@Test
	void testBasicBuilder() {
		// Test basic builder with only required properties
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}";

		ConstraintViolation violation = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.build();

		assertThat(violation.name()).isEqualTo(name);
		assertThat(violation.messageKey()).isEqualTo(messageKey);
		assertThat(violation.defaultMessageFormat()).isEqualTo(defaultMessage);
		assertThat(violation.args()).isNotNull();
		assertThat(violation.args()).isEmpty();
		assertThat(violation.locale()).isEqualTo(Locale.getDefault());
		assertThat(violation.message()).isEqualTo(defaultMessage);
	}

	@Test
	void testBuilderWithAllOptions() {
		// Test builder with all options
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}";
		Object[] args = new Object[] { name, 4, 20 };
		MessageFormatter formatter = SimpleMessageFormatter.getInstance();
		Locale locale = Locale.ENGLISH;

		ConstraintViolation violation = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.args(args)
			.messageFormatter(formatter)
			.locale(locale)
			.build();

		assertThat(violation.name()).isEqualTo(name);
		assertThat(violation.messageKey()).isEqualTo(messageKey);
		assertThat(violation.defaultMessageFormat()).isEqualTo(defaultMessage);
		assertThat(violation.args()).isEqualTo(args);
		assertThat(violation.locale()).isEqualTo(locale);
		assertThat(violation.message()).isEqualTo("username length must be between 4 and 20");
	}

	@Test
	void testArgsWithPrependedName() {
		// Test argsWithPrependedName method
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}";

		ConstraintViolation violation = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.argsWithPrependedName(4, 20) // Varargs
			.locale(Locale.ENGLISH)
			.build();

		assertThat(violation.args()).hasSize(3);
		assertThat(violation.args()[0]).isEqualTo(name);
		assertThat(violation.args()[1]).isEqualTo(4);
		assertThat(violation.args()[2]).isEqualTo(20);
		assertThat(violation.message()).isEqualTo("username length must be between 4 and 20");
	}

	@Test
	void testArgsWithPrependedNameAndAppendedViolatedValue() {
		// Test argsWithPrependedNameAndAppendedViolatedValue method
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}, but was {3}";
		String invalidValue = "abc"; // Too short value
		ViolatedValue violatedValue = new ViolatedValue(invalidValue);
		Object[] middleArgs = new Object[] { 4, 20 };

		ConstraintViolation violation = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.argsWithPrependedNameAndAppendedViolatedValue(middleArgs, violatedValue)
			.locale(Locale.ENGLISH)
			.build();

		assertThat(violation.args()).hasSize(4);
		assertThat(violation.args()[0]).isEqualTo(name);
		assertThat(violation.args()[1]).isEqualTo(4);
		assertThat(violation.args()[2]).isEqualTo(20);
		assertThat(violation.args()[3]).isEqualTo(invalidValue);
		assertThat(violation.message()).isEqualTo("username length must be between 4 and 20, but was abc");
	}

	@Test
	void testRename() {
		// Test rename method
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}";

		ConstraintViolation original = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.argsWithPrependedName(4, 20)
			.build();

		ConstraintViolation renamed = original.rename(n -> "user." + n);

		assertThat(renamed.name()).isEqualTo("user.username");
		assertThat(renamed.args()[0]).isEqualTo("user.username");
		assertThat(renamed.message()).contains("user.username");
	}

	@Test
	void testIndexed() {
		// Test indexed method
		String name = "items";
		String messageKey = "notEmpty";
		String defaultMessage = "{0} must not be empty";

		ConstraintViolation original = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.argsWithPrependedName()
			.build();

		ConstraintViolation indexed = original.indexed(3);

		assertThat(indexed.name()).isEqualTo("items[3]");
		assertThat(indexed.args()[0]).isEqualTo("items[3]");
		assertThat(indexed.message()).isEqualTo("items[3] must not be empty");
	}

}
