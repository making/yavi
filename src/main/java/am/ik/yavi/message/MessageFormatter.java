/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.util.Locale;

@FunctionalInterface
public interface MessageFormatter {

	String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale);

}
