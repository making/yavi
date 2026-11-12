/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.math.BigDecimal;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Reads the strings of another reader as text, as {@link InputReader#coercingStrings()}
 * states.
 */
record StringCoercingInputReader<N> (InputReader<N> reader) implements InputReader<N> {

	@Override
	public boolean isNull(N node) {
		return this.reader.isNull(node) || "".equals(this.reader.value(node));
	}

	@Override
	public Object value(N node) {
		return this.reader.value(node);
	}

	@Override
	public Object value(N node, Class<?> type) {
		final Object value = this.reader.value(node, type);
		if (!(value instanceof String string)) {
			return value;
		}
		if (type == Boolean.class) {
			return string.equals("true") ? Boolean.TRUE : string.equals("false") ? Boolean.FALSE : value;
		}
		if (Number.class.isAssignableFrom(type)) {
			try {
				return new BigDecimal(string);
			}
			catch (NumberFormatException e) {
				return value;
			}
		}
		return value;
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
	public InputReader<N> coercingStrings() {
		return this;
	}

}
