/*
 * Copyright (C) 2018-2019 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.yavi.core;

import java.util.Arrays;
import java.util.Locale;

import am.ik.yavi.message.MessageFormatter;

public class ConstraintViolation {
	private final Object[] args;

	private final String defaultMessageFormat;

	private final Locale locale;

	private final MessageFormatter messageFormatter;

	private final String messageKey;

	private final String name;

	public ConstraintViolation(String name, String messageKey,
			String defaultMessageFormat, Object[] args, MessageFormatter messageFormatter,
			Locale locale) {
		this.name = name;
		this.messageKey = messageKey;
		this.defaultMessageFormat = defaultMessageFormat;
		this.args = args;
		this.messageFormatter = messageFormatter;
		this.locale = locale;
	}

	public Object[] args() {
		return this.args;
	}

	public String defaultMessageFormat() {
		return this.defaultMessageFormat;
	}

	public ViolationDetail detail() {
		return new ViolationDetail(this.messageKey, this.args, this.message());
	}

	public Locale locale() {
		return this.locale;
	}

	public String message() {
		return this.messageFormatter.format(this.messageKey, this.defaultMessageFormat,
				this.args, this.locale);
	}

	public String messageKey() {
		return this.messageKey;
	}

	public String name() {
		return this.name;
	}

	@Override
	public String toString() {
		return "ConstraintViolation{" + "name='" + name + '\'' + ", messageKey='"
				+ messageKey + '\'' + ", defaultMessageFormat='" + defaultMessageFormat
				+ '\'' + ", args=" + Arrays.toString(args) + '}';
	}

	public Object violatedValue() {
		return this.args[this.args.length - 1];
	}
}
