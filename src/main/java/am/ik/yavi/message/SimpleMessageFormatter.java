/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * A simple implementation of {@link MessageFormatter} that formats messages using
 * {@link MessageFormat}. This class is a singleton to optimize resource usage.
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

	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		return new MessageFormat(defaultMessageFormat, locale).format(args);
	}

}
