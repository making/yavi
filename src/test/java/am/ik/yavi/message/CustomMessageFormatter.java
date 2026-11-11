/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.message;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum CustomMessageFormatter implements MessageFormatter {

	INSTANCE;

	private final Map<String, String> formats;

	CustomMessageFormatter() {
		this.formats = Collections.unmodifiableMap(new HashMap<String, String>() {
			{
				put("object.notNull", "Für \"{0}\" muss ein Wert vorhanden sein.");
				put("container.lessThanOrEqual",
						"Die Länge von \"{0}\" muss kleiner oder gleich {1} sein. Aktuelle Länge ist {2}.");
				put("container.greaterThanOrEqual",
						"Die Länge von \"{0}\" muss größer oder gleich {1} sein. Aktuelle Länge ist {2}.");
				put("numeric.greaterThanOrEqual", "\"{0}\" muss größer gleich {1} sein.");
				put("numeric.lessThanOrEqual", "\"{0}\" muss kleiner gleich {1} sein.");
				put("boolean.isTrue", "\"{0}\" muss wahr sein.");
				put("charSequence.email", "\"{0}\" muss eine gültige E-Mail Adresse sein.");
			}
		});
	}

	@Override
	public String format(String messageKey, String defaultMessageFormat, Object[] args, Locale locale) {
		final String format = this.formats.getOrDefault(messageKey, defaultMessageFormat);
		return new MessageFormat(format, locale).format(args);
	}

}
