/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * The reader of a tree of JDK types, as {@link InputReader#ofJdkTypes()} states.
 */
enum JdkInputReader implements CoercibleInputReader<Object> {

	INSTANCE;

	@Override
	public boolean isNull(Object node) {
		return false;
	}

	@Override
	public Object value(Object node) {
		return node;
	}

	@Override
	public boolean isObject(Object node) {
		if (!(node instanceof Map<?, ?>map)) {
			return false;
		}
		for (Object key : map.keySet()) {
			if (!(key instanceof String)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public @Nullable Object property(Object object, String name) {
		return ((Map<?, ?>) object).get(name);
	}

	@Override
	public boolean hasProperty(Object object, String name) {
		return ((Map<?, ?>) object).containsKey(name);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterable<? extends Map.Entry<String, ? extends @Nullable Object>> properties(Object object) {
		return ((Map<String, @Nullable Object>) object).entrySet();
	}

	@Override
	public boolean isArray(Object node) {
		return node instanceof Iterable;
	}

	@Override
	public Iterable<? extends @Nullable Object> elements(Object array) {
		return (Iterable<?>) array;
	}

}
