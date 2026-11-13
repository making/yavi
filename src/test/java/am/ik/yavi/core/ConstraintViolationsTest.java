/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import am.ik.yavi.message.SimpleMessageFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
				"charSequence.notBlank", "\"items[1].name\" must not be blank", Arrays.asList("items[1].name", " ")));
		assertThat(details.get(3).args()).containsExactly("", null);
		// a detail is serialized as it is
		assertThat(new JsonMapper().writeValueAsString(details.subList(1, 2))).isEqualTo("""
				[{"name":"items[1].name","pointer":"/items/1/name","messageKey":"charSequence.notBlank",\
				"message":"\\"items[1].name\\" must not be blank","args":["items[1].name"," "]}]""");
		assertThat(new ConstraintViolations().messagesByName()).isEmpty();
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
