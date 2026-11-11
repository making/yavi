/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.factory;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

abstract class ValidatorFactorySupport {

	protected final String messageKeySeparator;

	protected final MessageFormatter messageFormatter;

	protected ValidatorFactorySupport(@Nullable String messageKeySeparator,
			@Nullable MessageFormatter messageFormatter) {
		this.messageKeySeparator = messageKeySeparator;
		this.messageFormatter = messageFormatter;
	}

	protected <T> ValidatorBuilder<T> initBuilder() {
		final ValidatorBuilder<T> builder = this.messageKeySeparator == null ? new ValidatorBuilder<>()
				: new ValidatorBuilder<>(this.messageKeySeparator);
		if (this.messageFormatter != null) {
			builder.messageFormatter(messageFormatter);
		}
		return builder;
	}

}
