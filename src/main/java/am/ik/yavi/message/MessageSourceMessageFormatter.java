/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

import am.ik.yavi.jsr305.Nullable;

/**
 * <code>MessageFormatter</code> implementation that delegates formatting to
 * <code>MessageSource</code>.<br>
 * This can adopt Spring Framework's <code>MessageSource</code> as follows:
 *
 * <pre>
 * <code>
 * org.springframework.context.MessageSource messageSource = ...;
 * Validator&lt;CartItem&gt; validator = ValidatorBuilder.&lt;CartItem&gt; of()
 *                        .messageFormatter(new MessageSourceMessageFormatter(messageSource::getMessage))
 *                        .constraint(CartItem::getQuantity, "quantity", c -&gt; c.greaterThan(0))
 *                        .constraint(...)
 *                        .build();
 * </code> </pre>
 *
 * @author Toshiaki Maki
 * @since 0.5.0
 */
public class MessageSourceMessageFormatter implements MessageFormatter {

	private final MessageSource messageSource;

	public MessageSourceMessageFormatter(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		final String defaultMessage = new MessageFormat(defaultMessageFormat, locale).format(args);
		final String message = this.messageSource.getMessage(messageKey, args, defaultMessage, locale);
		return Objects.requireNonNull(message, defaultMessage);
	}

	/**
	 * A compatible interface of Spring Framework's <code>MessageSource</code>.
	 */
	@FunctionalInterface
	public interface MessageSource {

		@Nullable
		String getMessage(String code, Object[] args, String defaultMessage, Locale locale);

	}

}
