/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Reads the strings of another reader as text, as
 * {@link InputReader#coercingStrings(StringCoercion)} states.
 */
record StringCoercingInputReader<N> (InputReader<N> reader, StringCoercion coercion) implements InputReader<N> {

	@Override
	public boolean isNull(N node) {
		return this.reader.isNull(node)
				|| (this.reader.value(node) instanceof String string && this.coercion.isNull(string));
	}

	@Override
	public Object value(N node) {
		return this.reader.value(node);
	}

	@Override
	public Object value(N node, Class<?> type) {
		final Object value = this.reader.value(node, type);
		if (!(value instanceof String string) || type.isInstance(value)) {
			return value;
		}
		final String text = this.coercion.text(string);
		if (type == Boolean.class) {
			final Boolean bool = this.coercion.toBoolean(text);
			return bool != null ? bool : text;
		}
		if (Number.class.isAssignableFrom(type)) {
			try {
				return new BigDecimal(text);
			}
			catch (NumberFormatException e) {
				return text;
			}
		}
		return text;
	}

	@Override
	public boolean isObject(N node) {
		return this.reader.isObject(node);
	}

	@Override
	public @Nullable N property(N object, String name) {
		return this.reader.property(object, name);
	}

	@Override
	public boolean hasProperty(N object, String name) {
		return this.reader.hasProperty(object, name);
	}

	@Override
	public Iterable<? extends Map.Entry<String, ? extends @Nullable N>> properties(N object) {
		return this.reader.properties(object);
	}

	@Override
	public boolean isArray(N node) {
		return this.reader.isArray(node);
	}

	@Override
	public Iterable<? extends @Nullable N> elements(N array) {
		return this.reader.elements(array);
	}

	@Override
	public InputReader<N> coercingStrings(StringCoercion coercion) {
		return coercion == this.coercion ? this : new StringCoercingInputReader<>(this.reader, coercion);
	}

}
