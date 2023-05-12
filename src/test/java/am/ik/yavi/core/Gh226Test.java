/*
 * Copyright (C) 2018-2023 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.List;

import am.ik.yavi.builder.ValidatorBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Gh226Test {
	private static final Validator<String> recipientValidator = ValidatorBuilder
			.<String> of().constraint(String::toString, "aaa",
					e -> e.notNull().notBlank().notEmpty().email())
			.build();

	private static final Validator<Email> baseEmailValidatorBuilder = ValidatorBuilder
			.<Email> of().forEach(Email::getRecipients, "recipients", recipientValidator)
			.build();

	@Test
	void nullElement() {
		Email e = new Email();
		String nullVal = null;
		e.setRecipients(Arrays.asList(nullVal));
		ConstraintViolations violations = baseEmailValidatorBuilder.validate(e);
		assertThat(violations.size()).isEqualTo(1);
		assertThat(violations.get(0).message())
				.isEqualTo("\"recipients[0]\" must not be null");
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
