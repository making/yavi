/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.message.MessageFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
		assertThat(violation.localizable()).isTrue();
		assertThat(violation.message()).isEqualTo(defaultMessage);
	}

	@Test
	void testBuilderWithAllOptions() {
		// Test builder with all options
		String name = "username";
		String messageKey = "length";
		String defaultMessage = "{0} length must be between {1} and {2}";
		Object[] args = new Object[] { name, 4, 20 };
		MessageFormatter knowsEveryKey = (key, format, arguments, l) -> key;
		Locale locale = Locale.ENGLISH;

		ConstraintViolation violation = ConstraintViolation.builder()
			.name(name)
			.messageKey(messageKey)
			.defaultMessageFormat(defaultMessage)
			.args(args)
			.messageFormatter(knowsEveryKey)
			.locale(locale)
			.localizable(false)
			.build();

		assertThat(violation.name()).isEqualTo(name);
		assertThat(violation.messageKey()).isEqualTo(messageKey);
		assertThat(violation.defaultMessageFormat()).isEqualTo(defaultMessage);
		assertThat(violation.args()).isEqualTo(args);
		assertThat(violation.locale()).isEqualTo(locale);
		// a message that is not localizable is not looked up by its key
		assertThat(violation.localizable()).isFalse();
		assertThat(violation.message()).isEqualTo("username length must be between 4 and 20");
		assertThat(violation.withMessage(ViolationMessage.of("other", "other")).message()).isEqualTo("other");
		assertThat(violation.withMessage(ViolationMessage.of("other", "other").withText("{0} is too long")).message())
			.isEqualTo("username is too long");
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
			.paramNames(List.of("min", "max"))
			.locale(Locale.ENGLISH)
			.build();

		assertThat(violation.params()).containsExactly(Map.entry("min", 4), Map.entry("max", 20));
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
		// The structure of a renamed violation is unknown
		assertThat(original.path()).containsExactly(new PathSegment.Property("username"));
		assertThat(renamed.path()).containsExactly(new PathSegment.Property("user.username"));
	}

	@Test
	void testPrefixed() {
		ConstraintViolation original = ConstraintViolation.builder()
			.name("name")
			.messageKey("notEmpty")
			.defaultMessageFormat("{0} must not be empty")
			.argsWithPrependedName()
			.build();

		ConstraintViolation prefixed = original.prefixed(3).prefixed("items").prefixed(0).prefixed("");

		assertThat(prefixed.name()).isEqualTo("[0].items[3].name");
		assertThat(prefixed.args()[0]).isEqualTo("[0].items[3].name");
		assertThat(prefixed.message()).isEqualTo("[0].items[3].name must not be empty");
		assertThat(prefixed.path()).containsExactly(new PathSegment.Index(0), new PathSegment.Property("items"),
				new PathSegment.Index(3), new PathSegment.Property("name"));
		assertThat(prefixed.pointer()).isEqualTo("/0/items/3/name");
	}

	@Test
	void messageOfAValueWithoutAName() {
		ConstraintViolation leading = ConstraintViolation.builder()
			.name("")
			.messageKey("charSequence.notBlank")
			.defaultMessageFormat("\"{0}\" must not be blank")
			.argsWithPrependedName()
			.build();
		ConstraintViolation inner = ConstraintViolation.builder()
			.name("")
			.messageKey("container.lessThan")
			.defaultMessageFormat("The size of \"{0}\" must be less than {1}. The given size is {2}")
			.argsWithPrependedName(3, 5)
			.build();

		assertThat(leading.message()).isEqualTo("The value must not be blank");
		assertThat(inner.message()).isEqualTo("The size of the value must be less than 3. The given size is 5");
		assertThat(inner.detail().message()).isEqualTo(inner.message());
		// only the message differs from that of a named violation
		assertThat(leading.args()).containsExactly("");
		assertThat(leading.defaultMessageFormat()).isEqualTo("\"{0}\" must not be blank");
		// a formatter with its own formats still gets the empty name as an argument
		assertThat(leading.message((key, format, args, locale) -> key + ":" + format + ":" + args[0]))
			.isEqualTo("charSequence.notBlank:The value must not be blank:");
		// the name is back once the violation is located
		assertThat(leading.prefixed(0).prefixed("tags").message()).isEqualTo("\"tags[0]\" must not be blank");
	}

}
