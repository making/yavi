/*
 * Copyright (C) 2018-2021 Toshiaki Maki <makingx@gmail.com>
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

public interface ViolationMessage {
	static ViolationMessage of(String messageKey, String defaultMessageFormat) {
		return new ViolationMessage() {
			@Override
			public String defaultMessageFormat() {
				return defaultMessageFormat;
			}

			@Override
			public String messageKey() {
				return messageKey;
			}
		};
	}

	String defaultMessageFormat();

	String messageKey();

	enum Default implements ViolationMessage {
		OBJECT_NOT_NULL("object.notNull", "\"{0}\" must not be null"), //
		OBJECT_IS_NULL("object.isNull", "\"{0}\" must be null"), //
		CONTAINER_NOT_EMPTY("container.notEmpty", "\"{0}\" must not be empty"), //
		CONTAINER_LESS_THAN("container.lessThan",
				"The size of \"{0}\" must be less than {1}. The given size is {2}"), //
		CONTAINER_LESS_THAN_OR_EQUAL("container.lessThanOrEqual",
				"The size of \"{0}\" must be less than or equal to {1}. The given size is {2}"), //
		CONTAINER_GREATER_THAN("container.greaterThan",
				"The size of \"{0}\" must be greater than {1}. The given size is {2}"), //
		CONTAINER_GREATER_THAN_OR_EQUAL("container.greaterThanOrEqual",
				"The size of \"{0}\" must be greater than or equal to {1}. The given size is {2}"), //
		CONTAINER_FIXED_SIZE("container.fixedSize",
				"The size of \"{0}\" must be {1}. The given size is {2}"), //
		NUMERIC_GREATER_THAN("numeric.greaterThan", "\"{0}\" must be greater than {1}"), //
		NUMERIC_GREATER_THAN_OR_EQUAL("numeric.greaterThanOrEqual",
				"\"{0}\" must be greater than or equal to {1}"), //
		NUMERIC_LESS_THAN("numeric.lessThan", "\"{0}\" must be less than {1}"), //
		NUMERIC_LESS_THAN_OR_EQUAL("numeric.lessThanOrEqual",
				"\"{0}\" must be less than or equal to {1}"), //
		BOOLEAN_IS_TRUE("boolean.isTrue", "\"{0}\" must be true"), //
		BOOLEAN_IS_FALSE("boolean.isFalse", "\"{0}\" must be false"), //
		CHAR_SEQUENCE_NOT_BLANK("charSequence.notBlank", "\"{0}\" must not be blank"), //
		CHAR_SEQUENCE_CONTAINS("charSequence.contains", "\"{0}\" must contain {1}"), //
		CHAR_SEQUENCE_EMAIL("charSequence.email",
				"\"{0}\" must be a valid email address"), //
		CHAR_SEQUENCE_IPV4("charSequence.ipv4", "\"{0}\" must be a valid IPv4"), //
		CHAR_SEQUENCE_IPV6("charSequence.ipv6", "\"{0}\" must be a valid IPv6"), //
		CHAR_SEQUENCE_URL("charSequence.url", "\"{0}\" must be a valid URL"), //
		CHAR_SEQUENCE_PATTERN("charSequence.pattern", "\"{0}\" must match {1}"), //
		CHAR_SEQUENCE_PASSWORD_REQUIRED("charSequence.passwordRequired",
				"\"{0}\" must contain {1} policy"), //
		CHAR_SEQUENCE_PASSWORD_OPTIONAL("charSequence.passwordOptional",
				"\"{0}\" must contain at least {1} policies from {2}"), //
		CHAR_SEQUENCE_LUHN("charSequence.luhn",
				"the check digit for \"{0}\" is invalid, Luhn checksum failed"), //
		CHAR_SEQUENCE_BOOLEAN("charSequence.boolean",
				"\"{0}\" must be a valid representation of a boolean."), //
		CHAR_SEQUENCE_BYTE("charSequence.byte",
				"\"{0}\" must be a valid representation of a byte."), //
		CHAR_SEQUENCE_SHORT("charSequence.integer",
				"\"{0}\" must be a valid representation of a short."), //
		CHAR_SEQUENCE_INTEGER("charSequence.integer",
				"\"{0}\" must be a valid representation of an integer."), //
		CHAR_SEQUENCE_LONG("charSequence.long",
				"\"{0}\" must be a valid representation of a long."), //
		CHAR_SEQUENCE_FLOAT("charSequence.float",
				"\"{0}\" must be a valid representation of a float."), //
		CHAR_SEQUENCE_DOUBLE("charSequence.double",
				"\"{0}\" must be a valid representation of a double."), //
		CHAR_SEQUENCE_BIGINTEGER("charSequence.biginteger",
				"\"{0}\" must be a valid representation of a big integer."), //
		CHAR_SEQUENCE_BIGDECIMAL("charSequence.bigdecimal",
				"\"{0}\" must be a valid representation of a big decimal."), //
		BYTE_SIZE_LESS_THAN("byteSize.lessThan",
				"The size of \"{0}\" must be less than {1}. The given size is {2}"), //
		BYTE_SIZE_LESS_THAN_OR_EQUAL("byteSize.lessThanOrEqual",
				"The byte size of \"{0}\" must be less than or equal to {1}. The given size is {2}"), //
		BYTE_SIZE_GREATER_THAN("byteSize.greaterThan",
				"The byte size of \"{0}\" must be greater than {1}. The given size is {2}"), //
		BYTE_SIZE_GREATER_THAN_OR_EQUAL("byteSize.greaterThanOrEqual",
				"The byte size of \"{0}\" must be greater than or equal to {1}. The given size is {2}"), //
		BYTE_SIZE_FIXED_SIZE("byteSize.fixedSize",
				"The byte size of \"{0}\" must be {1}. The given size is {2}"), //
		COLLECTION_CONTAINS("collection.contains", "\"{0}\" must contain {1}"), //
		MAP_CONTAINS_VALUE("map.containsValue", "\"{0}\" must contain value {1}"), //
		MAP_CONTAINS_KEY("map.containsKey", "\"{0}\" must contain key {1}"), //
		ARRAY_CONTAINS("array.contains", "\"{0}\" must contain {1}"), //
		CODE_POINTS_ALL_INCLUDED("codePoints.asWhiteList",
				"\"{1}\" is/are not allowed for \"{0}\""), //
		CODE_POINTS_NOT_INCLUDED("codePoints.asBlackList",
				"\"{1}\" is/are not allowed for \"{0}\"");

		private final String defaultMessageFormat;

		private final String messageKey;

		Default(String messageKey, String defaultMessageFormat) {
			this.messageKey = messageKey;
			this.defaultMessageFormat = defaultMessageFormat;
		}

		@Override
		public String defaultMessageFormat() {
			return this.defaultMessageFormat;
		}

		@Override
		public String messageKey() {
			return this.messageKey;
		}
	}
}
