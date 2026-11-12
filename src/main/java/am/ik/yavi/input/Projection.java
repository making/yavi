/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.input;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import am.ik.yavi.core.InputReader;
import org.jspecify.annotations.Nullable;

/**
 * An object that shows only some of its properties to the validator that reads it, so
 * that the validator does not take the others for its own.
 *
 * @param object the object
 * @param names tells whether the property with the given name is shown
 * @param <N> the type of the nodes
 */
record Projection<N> (N object, Predicate<String> names) {

	/**
	 * Returns a copy of the map with the properties that are shown.
	 */
	static Map<String, Object> of(Map<String, Object> map, Predicate<String> names) {
		final Map<String, Object> shown = new LinkedHashMap<>();
		map.forEach((name, value) -> {
			if (names.test(name)) {
				shown.put(name, value);
			}
		});
		return shown;
	}

	/**
	 * Reads a {@link Projection} as the object it shows and any other node with the given
	 * reader.
	 */
	record Reader<N> (InputReader<N> reader) implements InputReader<Object> {

		@SuppressWarnings("unchecked")
		private N node(Object node) {
			return (N) (node instanceof Projection<?>projection ? projection.object() : node);
		}

		@Override
		public boolean isNull(Object node) {
			return this.reader.isNull(this.node(node));
		}

		@Override
		public Object value(Object node) {
			return this.reader.value(this.node(node));
		}

		@Override
		public Object value(Object node, Class<?> type) {
			return this.reader.value(this.node(node), type);
		}

		@Override
		public boolean isObject(Object node) {
			return this.reader.isObject(this.node(node));
		}

		@Override
		public @Nullable Object property(Object object, String name) {
			return hides(object, name) ? null : this.reader.property(this.node(object), name);
		}

		@Override
		public boolean hasProperty(Object object, String name) {
			return !hides(object, name) && this.reader.hasProperty(this.node(object), name);
		}

		@Override
		public Iterable<? extends Map.Entry<String, ? extends @Nullable Object>> properties(Object object) {
			final Iterable<? extends Map.Entry<String, ? extends @Nullable N>> properties = this.reader
				.properties(this.node(object));
			return object instanceof Projection<?>projection ? StreamSupport.stream(properties.spliterator(), false)
				.filter(property -> projection.names().test(property.getKey()))
				.toList() : properties;
		}

		@Override
		public boolean isArray(Object node) {
			return this.reader.isArray(this.node(node));
		}

		@Override
		public Iterable<? extends @Nullable Object> elements(Object array) {
			return this.reader.elements(this.node(array));
		}

		private static boolean hides(Object object, String name) {
			return object instanceof Projection<?>projection && !projection.names().test(name);
		}

	}

}
