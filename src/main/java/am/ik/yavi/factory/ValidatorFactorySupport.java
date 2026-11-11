/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.factory;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.jsr305.Nullable;
import am.ik.yavi.message.MessageFormatter;

/**
 * Shared base of the validator factories.
 *
 * <p>
 * Holds the message key separator and the {@link MessageFormatter} used by
 * {@link ValidatorFactory} and {@link BiConsumerFactory} and initializes a
 * {@link ValidatorBuilder} with them.
 */
abstract class ValidatorFactorySupport {

	/**
	 * Separator between nested property names in message keys; {@code null} for the
	 * default separator.
	 */
	protected final String messageKeySeparator;

	/**
	 * Formatter used to create violation messages; {@code null} for the default
	 * formatter.
	 */
	protected final MessageFormatter messageFormatter;

	/**
	 * Creates the base with the given message configuration.
	 * @param messageKeySeparator the separator between nested property names in message
	 * keys; {@code null} for the default
	 * @param messageFormatter the formatter used to create violation messages;
	 * {@code null} for the default
	 */
	protected ValidatorFactorySupport(@Nullable String messageKeySeparator,
			@Nullable MessageFormatter messageFormatter) {
		this.messageKeySeparator = messageKeySeparator;
		this.messageFormatter = messageFormatter;
	}

	/**
	 * Creates a builder initialized with this factory's message configuration.
	 * @param <T> the type of the object to validate
	 * @return a new builder
	 */
	protected <T> ValidatorBuilder<T> initBuilder() {
		final ValidatorBuilder<T> builder = this.messageKeySeparator == null ? new ValidatorBuilder<>()
				: new ValidatorBuilder<>(this.messageKeySeparator);
		if (this.messageFormatter != null) {
			builder.messageFormatter(messageFormatter);
		}
		return builder;
	}

}
