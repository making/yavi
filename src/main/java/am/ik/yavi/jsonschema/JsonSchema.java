/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.jsonschema;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import am.ik.yavi.core.ConstraintDescription;
import am.ik.yavi.core.ValueDescription;
import org.jspecify.annotations.Nullable;

/**
 * Translates a {@link ValueDescription} into a JSON Schema (draft 2020-12).
 *
 * <pre>{@code
 * Map<String, Object> schema = JsonSchema.of(validator.description());
 * String json = JsonSchema.toJson(validator.description());
 * }</pre>
 *
 * <p>
 * The schema is a tree of {@link Map}, {@link List}, {@link String}, {@link Number} and
 * {@link Boolean} values in insertion order, which any JSON library can serialize.
 *
 * <ul>
 * <li>An object is written with {@code properties}; a property is {@code required} unless
 * its value accepts {@code null} (see {@link ValueDescription#acceptsNull()}). A value
 * that accepts {@code null} also allows the type {@code null}.</li>
 * <li>A list is written with {@code items}, a map with {@code additionalProperties},
 * alternatives with {@code anyOf} and a default value with {@code default}.</li>
 * <li>The properties that an object does not define are written with
 * {@code additionalProperties}: their schema if they are validated, and {@code false} if
 * they are rejected.</li>
 * <li>The built-in constraints with an equivalent keyword are translated:
 * {@code notNull}, {@code notBlank}, {@code notEmpty} and the size constraints
 * ({@code minLength}, {@code maxLength}, {@code minItems}, {@code maxItems},
 * {@code minProperties}, {@code maxProperties}), the numeric ranges ({@code minimum},
 * {@code maximum}, {@code exclusiveMinimum}, {@code exclusiveMaximum}), {@code pattern},
 * {@code email}, {@code url}, {@code uuid}, {@code ipv4}, {@code ipv6} ({@code format}),
 * {@code unique} ({@code uniqueItems}), {@code oneOf} ({@code enum}), and
 * {@code equalTo}, {@code isTrue} and {@code isFalse} ({@code const}).</li>
 * <li>Any other constraint and an unknown value add no restriction, so the schema may
 * accept input that the validator rejects, but not the other way around. One exception is
 * {@code notBlank}, which is written as {@code minLength: 1}.</li>
 * </ul>
 *
 * @since 0.18.0
 */
public final class JsonSchema {

	/**
	 * The dialect of the generated schemas.
	 */
	public static final String DIALECT = "https://json-schema.org/draft/2020-12/schema";

	private JsonSchema() {
	}

	/**
	 * Returns the JSON Schema of the described input as a tree of maps and lists.
	 * @param description the description of the input of a validator
	 * @return a modifiable schema whose first entry is {@code $schema}
	 */
	public static Map<String, Object> of(ValueDescription description) {
		final Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("$schema", DIALECT);
		schema.putAll(schema(description, description.acceptsNull()));
		return schema;
	}

	/**
	 * Returns the JSON Schema of the described input as a compact JSON document.
	 * @param description the description of the input of a validator
	 * @return the schema as JSON
	 */
	public static String toJson(ValueDescription description) {
		final StringBuilder json = new StringBuilder();
		write(json, of(description));
		return json.toString();
	}

	private static Map<String, Object> schema(ValueDescription description, boolean nullable) {
		final Map<String, Object> schema = new LinkedHashMap<>();
		switch (description) {
			case ValueDescription.Value value -> value(schema, value, nullable);
			case ValueDescription.Properties properties -> {
				final Map<String, Object> schemas = new LinkedHashMap<>();
				final List<Object> required = new ArrayList<>();
				for (ValueDescription.Property property : properties.properties()) {
					final boolean optional = property.value().acceptsNull();
					schemas.put(property.name(), schema(property.value(), optional));
					if (!optional) {
						required.add(property.name());
					}
				}
				schema.put("type", type("object", nullable));
				schema.put("properties", schemas);
				if (!required.isEmpty()) {
					schema.put("required", required);
				}
				switch (properties.rest()) {
					case null -> {
					}
					case ValueDescription.Unknown unknown -> {
					}
					case ValueDescription.Never never -> schema.put("additionalProperties", false);
					case ValueDescription rest ->
						schema.put("additionalProperties", schema(rest, rest.acceptsNull()));
				}
			}
			case ValueDescription.Elements elements -> {
				schema.put("type", type("array", nullable));
				schema.put("items", schema(elements.element(), elements.element().acceptsNull()));
			}
			case ValueDescription.Entries entries -> {
				schema.put("type", type("object", nullable));
				schema.put("additionalProperties", schema(entries.value(), entries.value().acceptsNull()));
			}
			case ValueDescription.AnyOf anyOf -> {
				final List<Object> alternatives = new ArrayList<>();
				boolean nullAllowed = false;
				for (ValueDescription alternative : anyOf.alternatives()) {
					final boolean alternativeNullable = nullable && alternative.acceptsNull();
					nullAllowed |= alternativeNullable;
					alternatives.add(schema(alternative, alternativeNullable));
				}
				if (nullable && !nullAllowed) {
					alternatives.add(Map.of("type", "null"));
				}
				schema.put("anyOf", alternatives);
			}
			// the caller has decided whether null is accepted here
			case ValueDescription.NotNull notNull -> schema.putAll(schema(notNull.value(), nullable));
			case ValueDescription.Optional optional -> {
				schema.putAll(schema(optional.value(), true));
				final Object defaultValue = optional.defaultValue();
				if (defaultValue != null) {
					schema.put("default", Objects.requireNonNull(jsonValue(defaultValue)));
				}
			}
			case ValueDescription.Unknown unknown -> {
			}
			case ValueDescription.Never never -> schema.put("not", new LinkedHashMap<>());
		}
		return schema;
	}

	private static void value(Map<String, Object> schema, ValueDescription.Value value, boolean nullable) {
		final Class<?> javaType = value.type();
		final String type = type(javaType);
		if (type != null) {
			schema.put("type", type(type, nullable));
		}
		if (javaType == LocalDate.class) {
			schema.put("format", "date");
		}
		else if (javaType == Instant.class || javaType == OffsetDateTime.class) {
			schema.put("format", "date-time");
		}
		final Object[] constants = javaType.getEnumConstants();
		if (constants != null) {
			schema.put("enum", jsonArray(Arrays.asList(constants)));
		}
		for (ConstraintDescription constraint : value.constraints()) {
			constraint(schema, type, constraint);
		}
		if (nullable) {
			// enum and const restrict the value whatever the type is
			final Object constant = schema.remove("const");
			if (constant != null) {
				schema.put("enum", List.of(constant));
			}
			if (schema.get("enum") instanceof List<?>values && !values.contains(null)) {
				final List<@Nullable Object> withNull = new ArrayList<>(values);
				withNull.add(null);
				schema.put("enum", withNull);
			}
		}
	}

	private static void constraint(Map<String, Object> schema, @Nullable String type,
			ConstraintDescription constraint) {
		final List<@Nullable Object> args = constraint.args();
		final Object arg = args.isEmpty() ? null : args.get(0);
		final String size = switch (type) {
			case "string" -> "Length";
			case "array" -> "Items";
			case "object" -> "Properties";
			case null, default -> null;
		};
		final boolean numeric = "integer".equals(type) || "number".equals(type);
		switch (constraint.messageKey()) {
			case "object.oneOf" -> {
				if (arg instanceof Collection<?> values) {
					schema.put("enum", jsonArray(values));
				}
			}
			case "object.equalTo" -> {
				final Object constant = jsonValue(arg);
				if (constant != null) {
					schema.put("const", constant);
				}
			}
			case "boolean.isTrue" -> schema.put("const", true);
			case "boolean.isFalse" -> schema.put("const", false);
			case "container.notEmpty", "charSequence.notBlank" -> size(schema, size, 1, null);
			case "container.greaterThan" -> size(schema, size, arg instanceof Integer n ? n + 1 : null, null);
			case "container.greaterThanOrEqual" -> size(schema, size, arg instanceof Integer n ? n : null, null);
			case "container.lessThan" -> size(schema, size, null, arg instanceof Integer n ? n - 1 : null);
			case "container.lessThanOrEqual" -> size(schema, size, null, arg instanceof Integer n ? n : null);
			case "container.fixedSize" -> {
				if (arg instanceof Integer n) {
					size(schema, size, n, n);
				}
			}
			case "numeric.greaterThan" -> number(schema, numeric, "exclusiveMinimum", arg);
			case "numeric.greaterThanOrEqual" -> number(schema, numeric, "minimum", arg);
			case "numeric.lessThan" -> number(schema, numeric, "exclusiveMaximum", arg);
			case "numeric.lessThanOrEqual" -> number(schema, numeric, "maximum", arg);
			case "numeric.positive" -> number(schema, numeric, "exclusiveMinimum", 0);
			case "numeric.positiveOrZero" -> number(schema, numeric, "minimum", 0);
			case "numeric.negative" -> number(schema, numeric, "exclusiveMaximum", 0);
			case "numeric.negativeOrZero" -> number(schema, numeric, "maximum", 0);
			case "charSequence.pattern" -> {
				// YAVI matches the whole value, JSON Schema searches for a match
				if (arg instanceof String regex && !schema.containsKey("pattern")) {
					schema.put("pattern", "^(?:" + regex + ")$");
				}
			}
			case "charSequence.email" -> schema.put("format", "email");
			case "charSequence.url" -> schema.put("format", "uri");
			case "charSequence.uuid" -> schema.put("format", "uuid");
			case "charSequence.ipv4" -> schema.put("format", "ipv4");
			case "charSequence.ipv6" -> schema.put("format", "ipv6");
			case "collection.unique" -> schema.put("uniqueItems", true);
			default -> {
			}
		}
	}

	/**
	 * Narrows the size keywords ({@code minLength}, {@code maxItems}, ...) of the schema
	 * to the given bounds.
	 */
	private static void size(Map<String, Object> schema, @Nullable String size, @Nullable Integer min,
			@Nullable Integer max) {
		if (size == null) {
			return;
		}
		if (min != null) {
			schema.merge("min" + size, min, (a, b) -> Math.max((Integer) a, (Integer) b));
		}
		if (max != null) {
			schema.merge("max" + size, max, (a, b) -> Math.min((Integer) a, (Integer) b));
		}
	}

	private static void number(Map<String, Object> schema, boolean numeric, String keyword, @Nullable Object bound) {
		if (numeric && bound instanceof Number && !schema.containsKey(keyword)) {
			schema.put(keyword, bound);
		}
	}

	private static Object type(String type, boolean nullable) {
		return nullable ? List.of(type, "null") : type;
	}

	private static @Nullable String type(Class<?> type) {
		if (CharSequence.class.isAssignableFrom(type) || type == Character.class || Enum.class.isAssignableFrom(type)) {
			return "string";
		}
		if (type == Boolean.class) {
			return "boolean";
		}
		if (type == Integer.class || type == Long.class || type == Short.class || type == Byte.class
				|| type == BigInteger.class || type == Year.class) {
			return "integer";
		}
		if (Number.class.isAssignableFrom(type)) {
			return "number";
		}
		if (Temporal.class.isAssignableFrom(type)) {
			return "string";
		}
		if (Iterable.class.isAssignableFrom(type) || type.isArray()) {
			return "array";
		}
		if (Map.class.isAssignableFrom(type)) {
			return "object";
		}
		return null;
	}

	/**
	 * Converts a default value or a constraint argument to a value that JSON can
	 * represent.
	 */
	private static @Nullable Object jsonValue(@Nullable Object value) {
		if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) {
			return value;
		}
		if (value instanceof Enum<?>constant) {
			return constant.name();
		}
		if (value instanceof java.util.Optional<?>optional) {
			return jsonValue(optional.orElse(null));
		}
		if (value instanceof Map<?, ?>map) {
			final Map<String, @Nullable Object> object = new LinkedHashMap<>();
			map.forEach((key, element) -> object.put(String.valueOf(key), jsonValue(element)));
			return object;
		}
		if (value instanceof Iterable<?>iterable) {
			return jsonArray(iterable);
		}
		if (value.getClass().isArray()) {
			final List<@Nullable Object> array = new ArrayList<>();
			for (int i = 0; i < Array.getLength(value); i++) {
				array.add(jsonValue(Array.get(value, i)));
			}
			return array;
		}
		return value.toString();
	}

	private static List<@Nullable Object> jsonArray(Iterable<?> values) {
		final List<@Nullable Object> array = new ArrayList<>();
		values.forEach(element -> array.add(jsonValue(element)));
		return array;
	}

	private static void write(StringBuilder json, @Nullable Object value) {
		if (value instanceof Map<?, ?>map) {
			json.append('{');
			final Iterator<? extends Map.Entry<?, ?>> entries = map.entrySet().iterator();
			while (entries.hasNext()) {
				final Map.Entry<?, ?> entry = entries.next();
				write(json, String.valueOf(entry.getKey()));
				json.append(':');
				write(json, entry.getValue());
				if (entries.hasNext()) {
					json.append(',');
				}
			}
			json.append('}');
		}
		else if (value instanceof Iterable<?>iterable) {
			json.append('[');
			final Iterator<?> elements = iterable.iterator();
			while (elements.hasNext()) {
				write(json, elements.next());
				if (elements.hasNext()) {
					json.append(',');
				}
			}
			json.append(']');
		}
		else if (value instanceof String string) {
			json.append('"');
			for (int i = 0; i < string.length(); i++) {
				final char c = string.charAt(i);
				switch (c) {
					case '"' -> json.append("\\\"");
					case '\\' -> json.append("\\\\");
					case '\n' -> json.append("\\n");
					case '\r' -> json.append("\\r");
					case '\t' -> json.append("\\t");
					default -> {
						if (c < 0x20) {
							json.append(String.format("\\u%04x", (int) c));
						}
						else {
							json.append(c);
						}
					}
				}
			}
			json.append('"');
		}
		else {
			// a number, a boolean or null
			json.append(value);
		}
	}

}
