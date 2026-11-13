/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.Map;

import am.ik.yavi.core.CoercibleInputReader;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

/**
 * Reads the tree model of Jackson 3. This is the reader shown in the README.
 */
final class JsonNodeReader implements CoercibleInputReader<JsonNode> {

	static final JsonNodeReader INSTANCE = new JsonNodeReader();

	@Override
	public boolean isNull(JsonNode node) {
		return node.isNull() || node.isMissingNode();
	}

	@Override
	public Object value(JsonNode node) {
		if (node.isString()) {
			return node.stringValue();
		}
		if (node.isNumber()) {
			return node.numberValue();
		}
		if (node.isBoolean()) {
			return node.booleanValue();
		}
		return node;
	}

	@Override
	public boolean isObject(JsonNode node) {
		return node.isObject();
	}

	@Override
	public @Nullable JsonNode property(JsonNode object, String name) {
		return object.get(name);
	}

	@Override
	public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
		return object.properties();
	}

	@Override
	public boolean isArray(JsonNode node) {
		return node.isArray();
	}

	@Override
	public Iterable<JsonNode> elements(JsonNode array) {
		return array;
	}

}
