/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.Map;
import java.util.Objects;

import am.ik.json.JsonNode;
import am.ik.json.JsonObject;
import am.ik.yavi.core.CoercibleInputReader;
import org.jspecify.annotations.Nullable;

/**
 * Reads the tree model of tiny-json, whose scalars are the JDK types that a reader
 * returns.
 */
final class TinyJsonReader implements CoercibleInputReader<JsonNode> {

	static final TinyJsonReader INSTANCE = new TinyJsonReader();

	@Override
	public boolean isNull(JsonNode node) {
		return node.isNull();
	}

	@Override
	public Object value(JsonNode node) {
		return Objects.requireNonNull(node.value());
	}

	@Override
	public boolean isObject(JsonNode node) {
		return node.isObject();
	}

	@Override
	public @Nullable JsonNode property(JsonNode object, String name) {
		final JsonObject properties = Objects.requireNonNull(object.asObject());
		return properties.containsKey(name) ? properties.get(name) : null;
	}

	@Override
	public Iterable<Map.Entry<String, JsonNode>> properties(JsonNode object) {
		return Objects.requireNonNull(object.asObject()).asMap().entrySet();
	}

	@Override
	public boolean isArray(JsonNode node) {
		return node.isArray();
	}

	@Override
	public Iterable<JsonNode> elements(JsonNode array) {
		return Objects.requireNonNull(array.asArray()).values();
	}

}
