/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * A {@link MessageFormatter} that looks up localized messages through a
 * {@link MessageSource}.
 *
 * <p>
 * The default message format is first formatted with {@link MessageFormat} and passed to
 * the message source as the fallback. This allows Spring Framework's
 * {@code MessageSource} to be used as follows:
 *
 * <pre>{@code
 * org.springframework.context.MessageSource messageSource = ...;
 * Validator<CartItem> validator = ValidatorBuilder.<CartItem>of()
 *         .messageFormatter(new MessageSourceMessageFormatter(messageSource::getMessage))
 *         .constraint(CartItem::getQuantity, "quantity", c -> c.greaterThan(0))
 *         .build();
 * }</pre>
 *
 * @author Toshiaki Maki
 * @since 0.5.0
 */
public class MessageSourceMessageFormatter implements MessageFormatter {

	private final MessageSource messageSource;

	/**
	 * Creates a formatter that delegates to the given message source.
	 * @param messageSource the message source used to look up localized messages
	 */
	public MessageSourceMessageFormatter(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * Looks up a localized message for the given message id and formats it.
	 *
	 * <p>
	 * The {@code defaultMessageFormat} is formatted with {@link MessageFormat} and passed
	 * to the message source as the default message.
	 * @param messageKey the message id used to look up a localized message
	 * @param defaultMessageFormat the message format used when no localized message is
	 * found
	 * @param args the arguments referenced by placeholders in the message format
	 * @param locale the locale used for the lookup and formatting
	 * @return the formatted message
	 * @throws NullPointerException if the message source returns {@code null}
	 */
	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		final String defaultMessage = new MessageFormat(defaultMessageFormat, locale).format(args);
		final String message = this.messageSource.getMessage(messageKey, args, defaultMessage, locale);
		return Objects.requireNonNull(message, defaultMessage);
	}

	/**
	 * A compatible interface of Spring Framework's {@code MessageSource}.
	 */
	@FunctionalInterface
	public interface MessageSource {

		/**
		 * Resolves a message for the given code.
		 * @param code the message id to look up
		 * @param args the arguments referenced by placeholders in the message
		 * @param defaultMessage the message to use when no message is found for the code
		 * @param locale the locale used for the lookup
		 * @return the resolved message
		 */
		@Nullable String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	}

}
