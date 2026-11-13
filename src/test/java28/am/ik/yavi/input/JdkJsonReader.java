/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.util.Map;

import am.ik.yavi.core.CoercibleInputReader;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonBoolean;
import jdk.incubator.json.JsonNull;
import jdk.incubator.json.JsonNumber;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonString;
import jdk.incubator.json.JsonValue;
import org.jspecify.annotations.Nullable;

/**
 * Reads the tree model of the JDK (JEP 540), which is an incubator module of JDK 28. This
 * is the reader shown in the README.
 */
final class JdkJsonReader implements CoercibleInputReader<JsonValue> {

	static final JdkJsonReader INSTANCE = new JdkJsonReader();

	@Override
	public boolean isNull(JsonValue node) {
		return node instanceof JsonNull;
	}

	@Override
	public Object value(JsonValue node) {
		return switch (node) {
			case JsonString string -> string.asString();
			// the text of the number, so that no digit is lost
			case JsonNumber number -> new BigDecimal(number.toString());
			case JsonBoolean bool -> bool.asBoolean();
			default -> node;
		};
	}

	@Override
	public boolean isObject(JsonValue node) {
		return node instanceof JsonObject;
	}

	@Override
	public @Nullable JsonValue property(JsonValue object, String name) {
		return object.asMap().get(name);
	}

	@Override
	public Iterable<Map.Entry<String, JsonValue>> properties(JsonValue object) {
		return object.asMap().entrySet();
	}

	@Override
	public boolean isArray(JsonValue node) {
		return node instanceof JsonArray;
	}

	@Override
	public Iterable<JsonValue> elements(JsonValue array) {
		return array.asList();
	}

}
