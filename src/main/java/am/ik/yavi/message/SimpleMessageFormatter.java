/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * A {@link MessageFormatter} that formats the default message format with
 * {@link MessageFormat}.
 *
 * <p>
 * The message id is ignored; no localized message lookup is performed. This is the
 * formatter used by default. Since a new {@link MessageFormat} instance is created for
 * every call, the shared {@link #getInstance() instance} is thread-safe.
 *
 * @since 0.0.1
 */
public class SimpleMessageFormatter implements MessageFormatter {

	private static final SimpleMessageFormatter INSTANCE = new SimpleMessageFormatter();

	/**
	 * Returns the singleton instance of {@link SimpleMessageFormatter}.
	 * @return the singleton instance
	 */
	public static SimpleMessageFormatter getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor.
	 * @deprecated Use {@link #getInstance()} instead to get the singleton instance. This
	 * constructor will be removed in a future version.
	 */
	@Deprecated
	public SimpleMessageFormatter() {
		// Public constructor kept for backward compatibility
	}

	/**
	 * Formats the given default message format with {@link MessageFormat}, ignoring the
	 * message id.
	 * @param messageKey the message id, which is ignored
	 * @param defaultMessageFormat the message format containing {@link MessageFormat}
	 * placeholders
	 * @param args the arguments referenced by placeholders in the message format
	 * @param locale the locale used for formatting
	 * @return the formatted message
	 */
	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		return new MessageFormat(defaultMessageFormat, locale).format(args);
	}

}
