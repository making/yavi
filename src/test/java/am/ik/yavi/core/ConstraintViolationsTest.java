/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConstraintViolationsTest {

	@Test
	public void apply() {
		SimpleMessageFormatter messageFormatter = SimpleMessageFormatter.getInstance();
		ConstraintViolations violations = new ConstraintViolations();
		violations.add(ConstraintViolation.builder()
			.name("foo0")
			.messageKey("abc0")
			.defaultMessageFormat("hello0")
			.args(1)
			.messageFormatter(messageFormatter)
			.build());
		violations.add(ConstraintViolation.builder()
			.name("foo1")
			.messageKey("abc1")
			.defaultMessageFormat("hello1")
			.args(1)
			.messageFormatter(messageFormatter)
			.build());
		BindingResult bindingResult = new BindingResult();
		violations.apply(bindingResult::rejectValue);
		assertThat(bindingResult.toString()).isEqualTo("[foo0_abc0_[1]_hello0][foo1_abc1_[1]_hello1]");
	}

	@Test
	void outputForms() {
		ViolationMessage notBlank = ViolationMessage.of("charSequence.notBlank", "\"{0}\" must not be blank");
		ConstraintViolations violations = new ConstraintViolations();
		violations.add(ConstraintViolation.of(notBlank, "", Locale.ENGLISH).prefixed("email"));
		violations
			.add(ConstraintViolation.of(notBlank, " ", Locale.ENGLISH).prefixed("name").prefixed(1).prefixed("items"));
		violations.add(ConstraintViolation
			.of(ViolationMessage.of("charSequence.email", "\"{0}\" must be a valid email address"), "", Locale.ENGLISH)
			.prefixed("email"));
		violations.add(ConstraintViolation.of(ViolationMessage.of("object.notNull", "\"{0}\" must not be null"), null,
				Locale.ENGLISH));
		// a property name that holds the characters a JSON Pointer escapes
		violations.add(ConstraintViolation.of(notBlank, "", Locale.ENGLISH).prefixed("a/b~c"));

		Map<String, List<String>> byName = violations.messagesByName();
		assertThat(byName).containsExactly(
				Map.entry("email", List.of("\"email\" must not be blank", "\"email\" must be a valid email address")),
				Map.entry("items[1].name", List.of("\"items[1].name\" must not be blank")),
				Map.entry("", List.of("The value must not be null")),
				Map.entry("a/b~c", List.of("\"a/b~c\" must not be blank")));
		assertThat(violations.messagesByPointer().keySet()).containsExactly("/email", "/items/1/name", "", "/a~1b~0c");
		assertThat(violations.messagesByPointer().values()).containsExactlyElementsOf(byName.values());
		assertThatThrownBy(() -> byName.values().iterator().next().add("x"))
			.isInstanceOf(UnsupportedOperationException.class);

		List<ViolationDetail> details = violations.details();
		assertThat(details).hasSize(5);
		assertThat(details.get(1)).isEqualTo(new ViolationDetail.Simple("items[1].name", "/items/1/name",
				"charSequence.notBlank", "\"items[1].name\" must not be blank", Map.of()));
		assertThat(new ConstraintViolations().messagesByName()).isEmpty();
	}

	@Test
	void detailsNameTheParametersAndLeaveOutTheViolatedValue() {
		Validator<String> validator = ValidatorBuilder.<String>of()
			._string(s -> s, "password", c -> c.greaterThanOrEqual(8)
				.pattern("[0-9]+")
				// a replaced message keeps the names of the arguments of the constraint
				.message(ViolationMessage.of("password.digits", "\"{0}\" must consist of digits"))
				.endsWith("9")
				.message("\"{0}\" must end with {1}"))
			.build();
		ConstraintViolations violations = new ConstraintViolations();
		violations.addAll(validator.validate("secret", Locale.ENGLISH));
		// the names of a hand-written violation are those of its message, if any
		violations
			.add(ConstraintViolation.of(ViolationMessage.of("password.reused", "\"{0}\" was used {1} times", "times"),
					"secret", Locale.ENGLISH, 2));
		violations.add(ConstraintViolation.of(ViolationMessage.of("password.weak", "\"{0}\" is weak ({1})"), "secret",
				Locale.ENGLISH, "dictionary"));

		assertThat(violations).extracting(ConstraintViolation::params)
			.containsExactly(Map.of("min", 8, "actualSize", 6), Map.of("pattern", "[0-9]+"), Map.of("suffix", "9"),
					Map.of("times", 2), Map.of());
		assertThat(violations.get(0).params().keySet()).containsExactly("min", "actualSize");
		assertThat(violations.get(1).args()).containsExactly("password", "[0-9]+", "secret");
		assertThat(violations.get(1).violatedValue()).isEqualTo("secret");
		assertThatThrownBy(() -> violations.get(0).params().clear()).isInstanceOf(UnsupportedOperationException.class);
		// a detail is serialized as it is, and what was sent is not sent back
		assertThat(new JsonMapper().writeValueAsString(violations.details())).isEqualTo("""
				[{"name":"password","pointer":"/password","messageKey":"container.greaterThanOrEqual",\
				"message":"The size of \\"password\\" must be greater than or equal to 8. The given size is 6",\
				"params":{"min":8,"actualSize":6}},\
				{"name":"password","pointer":"/password","messageKey":"password.digits",\
				"message":"\\"password\\" must consist of digits","params":{"pattern":"[0-9]+"}},\
				{"name":"password","pointer":"/password","messageKey":"charSequence.endsWith",\
				"message":"\\"password\\" must end with 9","params":{"suffix":"9"}},\
				{"name":"","pointer":"","messageKey":"password.reused",\
				"message":"The value was used 2 times","params":{"times":2}},\
				{"name":"","pointer":"","messageKey":"password.weak",\
				"message":"The value is weak (dictionary)","params":{}}]""");
	}

	@Test
	void builtInMessagesNameEveryArgumentButTheNameAndTheViolatedValue() {
		for (ViolationMessage.Default message : ViolationMessage.Default.values()) {
			// the highest placeholder of the format, which no other argument follows
			int placeholders = Pattern.compile("\\{(\\d)}")
				.matcher(message.defaultMessageFormat())
				.results()
				.mapToInt(result -> Integer.parseInt(result.group(1)))
				.max()
				.orElse(0);
			// the only built-in format that shows the violated value itself
			int unnamed = message == ViolationMessage.Default.CHAR_SEQUENCE_LOCAL_DATE ? 1 : 0;
			assertThat(message.paramNames()).as(message.messageKey())
				.doesNotHaveDuplicates()
				.hasSize(placeholders - unnamed);
		}
	}

	static class BindingResult {

		final StringBuilder builder = new StringBuilder();

		public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
			this.builder.append("[")
				.append(field)
				.append("_")
				.append(errorCode)
				.append("_")
				.append(Arrays.toString(errorArgs))
				.append("_")
				.append(defaultMessage)
				.append("]");
		}

		@Override
		public String toString() {
			return this.builder.toString();
		}

	}

}
