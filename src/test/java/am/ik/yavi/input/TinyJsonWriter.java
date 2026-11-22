/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.List;
import java.util.Map;

import am.ik.json.JsonArray;
import am.ik.json.JsonNode;
import am.ik.json.JsonObject;
import am.ik.yavi.core.OutputWriter;

/**
 * Writes the tree model of tiny-json, which {@link TinyJsonReader} reads. A node holds
 * the JDK value it is given.
 */
final class TinyJsonWriter implements OutputWriter<JsonNode> {

	static final TinyJsonWriter INSTANCE = new TinyJsonWriter();

	@Override
	public JsonNode nullNode() {
		return new JsonNode(null);
	}

	@Override
	public JsonNode scalar(Object value) {
		if ((value instanceof Double || value instanceof Float) && !Double.isFinite(((Number) value).doubleValue())) {
			throw new IllegalArgumentException("JSON has no number for " + value);
		}
		return new JsonNode(value);
	}

	@Override
	public JsonNode object(Map<String, JsonNode> properties) {
		final JsonObject object = new JsonObject();
		properties.forEach(object::put);
		return object.toNode();
	}

	@Override
	public JsonNode array(List<JsonNode> elements) {
		final JsonArray array = new JsonArray();
		elements.forEach(array::add);
		return array.toNode();
	}

}
