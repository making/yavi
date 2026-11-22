/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import am.ik.yavi.core.OutputWriter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;

/**
 * Writes the tree model of Jackson 3, which {@link JsonNodeReader} reads. This is the
 * writer shown in the documentation.
 */
final class JsonNodeWriter implements OutputWriter<JsonNode> {

	static final JsonNodeWriter INSTANCE = new JsonNodeWriter();

	private static final JsonNodeFactory factory = JsonNodeFactory.instance;

	@Override
	public JsonNode nullNode() {
		return factory.nullNode();
	}

	@Override
	public JsonNode scalar(Object value) {
		return switch (value) {
			case String string -> factory.stringNode(string);
			case Boolean bool -> factory.booleanNode(bool);
			case Integer number -> factory.numberNode(number);
			case Long number -> factory.numberNode(number);
			case Short number -> factory.numberNode(number);
			case Byte number -> factory.numberNode(number);
			case BigInteger number -> factory.numberNode(number);
			case BigDecimal number -> factory.numberNode(number);
			case Double number -> factory.numberNode(number);
			case Float number -> factory.numberNode(number);
			default -> throw new IllegalArgumentException("Not a scalar: " + value.getClass().getName());
		};
	}

	@Override
	public JsonNode object(Map<String, JsonNode> properties) {
		return factory.objectNode().setAll(properties);
	}

	@Override
	public JsonNode array(List<JsonNode> elements) {
		return factory.arrayNode().addAll(elements);
	}

}
