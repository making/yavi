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
package am.ik.yavi.core;

import java.util.Arrays;

import am.ik.yavi.message.MessageFormatter;

public class ConstraintViolation {
	private final String name;
	private final String messageKey;
	private final String defaultMessageTemplate;
	private final Object[] args;
	private final Object value;
	private final MessageFormatter messageFormatter;

	public ConstraintViolation(String name, String messageKey,
			String defaultMessageTemplate, Object[] args, Object value,
			MessageFormatter messageFormatter) {
		this.name = name;
		this.messageKey = messageKey;
		this.defaultMessageTemplate = defaultMessageTemplate;
		this.args = args;
		this.value = value;
		this.messageFormatter = messageFormatter;
	}

	public String message() {
		return this.messageFormatter.format(this.name, this.messageKey,
				this.defaultMessageTemplate, this.args, this.value);
	}

	@Override
	public String toString() {
		return "ConstraintViolation{" + "name='" + name + '\'' + ", messageKey='"
				+ messageKey + '\'' + ", defaultMessageTemplate='"
				+ defaultMessageTemplate + '\'' + ", args=" + Arrays.toString(args)
				+ ", value=" + value + ", messageFormatter=" + messageFormatter + '}';
	}
}
