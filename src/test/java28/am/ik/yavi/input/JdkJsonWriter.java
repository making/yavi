/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.List;
import java.util.Map;

import am.ik.yavi.core.OutputWriter;
import jdk.incubator.json.JsonArray;
import jdk.incubator.json.JsonBoolean;
import jdk.incubator.json.JsonNull;
import jdk.incubator.json.JsonNumber;
import jdk.incubator.json.JsonObject;
import jdk.incubator.json.JsonString;
import jdk.incubator.json.JsonValue;

/**
 * Writes the tree model of the JDK (JEP 540), which {@link JdkJsonReader} reads.
 */
final class JdkJsonWriter implements OutputWriter<JsonValue> {

	static final JdkJsonWriter INSTANCE = new JdkJsonWriter();

	@Override
	public JsonValue nullNode() {
		return JsonNull.of();
	}

	@Override
	public JsonValue scalar(Object value) {
		return switch (value) {
			case String string -> JsonString.of(string);
			case Boolean bool -> JsonBoolean.of(bool);
			// the text of the number, so that no digit is lost
			case Number number -> JsonNumber.of(number.toString());
			default -> throw new IllegalArgumentException("Not a scalar: " + value.getClass().getName());
		};
	}

	@Override
	public JsonValue object(Map<String, JsonValue> properties) {
		return JsonObject.of(properties);
	}

	@Override
	public JsonValue array(List<JsonValue> elements) {
		return JsonArray.of(elements);
	}

}
