/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.util.Arrays;
import java.util.Locale;

import am.ik.yavi.message.MessageSourceMessageFormatter.MessageSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceMessageFormatterTest {

	@Test
	void format() {
		final MessageSource messageSource = (code, args, defaultMessage, locale) -> "Message " + code + " "
				+ Arrays.toString(args);
		final MessageSourceMessageFormatter messageFormatter = new MessageSourceMessageFormatter(messageSource);
		final String message = messageFormatter.format("demo", "", new Object[] { 1, 2 }, Locale.ENGLISH);
		assertThat(message).isEqualTo("Message demo [1, 2]");
	}

}