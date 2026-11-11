/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.util.Locale;

/**
 * Formats the message of a constraint violation.
 *
 * <p>
 * A formatter is invoked whenever {@link am.ik.yavi.core.ConstraintViolation#message()}
 * or a related method is called. It receives the message id, the default message format
 * and the message arguments; implementations typically look up a localized message for
 * the id and fall back to the default format.
 *
 * <p>
 * Implementations must be thread-safe because a validator, which holds a formatter, may
 * be shared between threads.
 *
 * @since 0.0.1
 */
@FunctionalInterface
public interface MessageFormatter {

	/**
	 * Formats a constraint violation message.
	 * @param messageKey the message id used to look up a localized message
	 * @param defaultMessageFormat the message format to use when no localized message is
	 * found; it may contain {@link java.text.MessageFormat} placeholders
	 * @param args the arguments referenced by placeholders in the message format; the
	 * first argument is the property name
	 * @param locale the locale used for formatting
	 * @return the formatted message
	 */
	String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale);

}
