/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
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
 * </code>
 * </pre>
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
	public String format(String messageKey, String defaultMessageFormat, Object[] args,
			Locale locale) {
		final String defaultMessage = new MessageFormat(defaultMessageFormat, locale)
				.format(args);
		final String message = this.messageSource.getMessage(messageKey, args,
				defaultMessage, locale);
		return Objects.requireNonNull(message, defaultMessage);
	}

	/**
	 * A compatible interface of Spring Framework's <code>MessageSource</code>.
	 */
	@FunctionalInterface
	public interface MessageSource {
		@Nullable
		String getMessage(String code, Object[] args, String defaultMessage,
				Locale locale);
	}
}
