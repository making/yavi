/*
 * Copyright (C) 2018 Toshiaki Maki <makingx@gmail.com>
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

import java.util.Locale;

@FunctionalInterface
public interface MessageFormatter {
	String format(String messageKey, String defaultMessageFormat, Object[] args,
			Locale locale);

	/**
	 * Use {@link #format(String, String, Object[], Locale)}. <code>name</code> can be
	 * replaced with <code>args[0]</code>
	 */
	@Deprecated
	default String format(String name, String messageKey, String defaultMessageFormat,
			Object[] args, Locale locale) {
		return this.format(messageKey, defaultMessageFormat, args, locale);
	}
}
