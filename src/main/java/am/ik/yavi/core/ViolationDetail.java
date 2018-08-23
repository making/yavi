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

/**
 * This class is intended to be used for the JSON serialization (such as Jackson)
 */
public class ViolationDetail {
	private final String key;
	private final Object[] args;
	private final String defaultMessage;

	public ViolationDetail(String key, Object[] args, String defaultMessage) {
		this.key = key;
		this.args = args;
		this.defaultMessage = defaultMessage;
	}

	public String getKey() {
		return key;
	}

	public Object[] getArgs() {
		return args;
	}

	public String getDefaultMessage() {
		return defaultMessage;
	}

	@Override
	public String toString() {
		return "ViolationDetail{" + "key='" + key + '\'' + ", args="
				+ Arrays.toString(args) + ", defaultMessage='" + defaultMessage + '\''
				+ '}';
	}
}
