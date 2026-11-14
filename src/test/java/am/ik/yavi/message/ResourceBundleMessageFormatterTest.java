/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import am.ik.yavi.core.ValueValidator;
import am.ik.yavi.validator.Yavi;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceBundleMessageFormatterTest {

	record Member(String name, Integer age, String email) {
	}

	@Test
	void formatsTheMessageOfTheBundleOfTheLocaleThenOfTheBaseBundleThenTheDefault() {
		final Locale platform = Locale.getDefault();
		try {
			// the bundle of the default locale of the platform is not a fallback
			Locale.setDefault(Locale.JAPAN);
			final ValueValidator<Map<String, Object>, Member> validator = Yavi.schema()
				._string("name", c -> c.notBlank())
				._integer("age", c -> c.lessThanOrEqual(150).message("too old"))
				._string("email", c -> c.notBlank().email())
				.fromMap(Member::new)
				.formattingWith(new ResourceBundleMessageFormatter("am.ik.yavi.message.messages"));
			final Map<String, Object> input = Map.of("name", "", "age", 300, "email", "x");
			// "age" keeps the text written in code, "email" has no message in a bundle
			assertThat(validator.validate(input, Locale.JAPAN).errors().messagesByName())
				.isEqualTo(Map.of("name", List.of("name is blank"), "age", List.of("too old"), "email",
						List.of("\"email\" must be a valid email address")));
			final ValueValidator<Integer, Integer> age = Yavi._integer("age", c -> c.lessThanOrEqual(150))
				.formattingWith(new ResourceBundleMessageFormatter("am.ik.yavi.message.messages"));
			assertThat(age.validate(300, Locale.JAPAN).errors().get(0).message())
				.isEqualTo("\"age\" は 150 以下で入力してください");
			assertThat(age.validate(300, Locale.ENGLISH).errors().get(0).message())
				.isEqualTo("age is too large (max 150)");
			assertThat(Yavi._integer("age", c -> c.lessThanOrEqual(150))
				.formattingWith(new ResourceBundleMessageFormatter("no.such.bundle"))
				.validate(300)
				.errors()
				.get(0)
				.message()).isEqualTo("\"age\" must be less than or equal to 150");
		}
		finally {
			Locale.setDefault(platform);
		}
	}

}
