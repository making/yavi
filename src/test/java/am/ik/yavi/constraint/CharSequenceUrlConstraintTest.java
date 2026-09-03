/*
 * Copyright (C) 2018-2025 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.yavi.constraint;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.charsequence.UrlConstraintBuilder;
import am.ik.yavi.core.ConstraintPredicate;
import am.ik.yavi.core.ConstraintViolations;
import am.ik.yavi.core.Validator;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CharSequenceUrlConstraintTest {

	@Test
	void protocolViolationMessage() {
		ConstraintViolations violations = validator(url -> url.protocol("http", "https").build())
			.validate("ftp://example.com");
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.url.protocol");
		assertThat(violations.get(0).message())
			.isEqualTo("The protocol of \"url\" must be one of the following values: [http, https]");
	}

	@Test
	void hostViolationMessage() {
		ConstraintViolations violations = validator(url -> url.host("example.com").build())
			.validate("https://www.example.com");
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.url.host");
		assertThat(violations.get(0).message())
			.isEqualTo("The host of \"url\" must be one of the following values: [example.com]");
	}

	@Test
	void portViolationMessage() {
		ConstraintViolations violations = validator(url -> url.port(443, 8443).build())
			.validate("https://example.com:8080");
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.url.port");
		assertThat(violations.get(0).message())
			.isEqualTo("The port of \"url\" must be one of the following values: [443, 8443]");
	}

	@Test
	void unparsableValueReportsOnlyTheUrlViolation() {
		ConstraintViolations violations = validator(url -> url.protocol("https").host("example.com").port(443).build())
			.validate(" https://example.com");
		assertThat(violations).hasSize(1);
		assertThat(violations.get(0).messageKey()).isEqualTo("charSequence.url");
		assertThat(violations.get(0).message()).isEqualTo("\"url\" must be a valid URL");
	}

	@Test
	void allViolationsAreReported() {
		ConstraintViolations violations = validator(url -> url.protocol("https").host("example.com").port(443).build())
			.validate("http://www.example.com:8080");
		assertThat(violations).hasSize(3);
	}

	private static Validator<String> validator(
			Function<UrlConstraintBuilder<String>, List<ConstraintPredicate<String>>> builder) {
		return ValidatorBuilder.<String>of()._string(s -> s, "url", c -> c.url(builder)).build();
	}

}
