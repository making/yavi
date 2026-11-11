/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

/**
 * Constraint Context that contains the name and attributes.
 *
 * @since 0.11.0
 */
public interface ConstraintContext {

	/**
	 * Returns the attribute for the given key.
	 * @param key key
	 * @return the attribute
	 */
	Attribute attribute(String key);

	/**
	 * Returns the context name.<br>
	 * This method exists on this interface for backwards compatibility with YAVI 0.10 and
	 * earlier. <br>
	 * It is effectively used in the lower interface <code>ConstraintGroup</code>.
	 * @return context name
	 */
	String name();

	static ConstraintContext from(Map<String, ?> map) {
		return new ConstraintContext() {
			@Override
			public Attribute attribute(String key) {
				return () -> map.get(key);
			}

			@Override
			public String name() {
				return "ConstraintContextFromMap";
			}
		};
	}

	static ConstraintContext from(Function<String, ?> function) {
		return new ConstraintContext() {
			@Override
			public Attribute attribute(String key) {
				return () -> function.apply(key);
			}

			@Override
			public String name() {
				return "ConstraintContextFromFunction";
			}
		};
	}

	@FunctionalInterface
	interface Attribute {

		/**
		 * Returns the attribute value. <code>null</code> is returned if the attribute
		 * does not exist.
		 * @return value
		 */
		@Nullable
		Object value();

		/**
		 * Returns the typed attribute value
		 * @param clazz the type of the attribute value
		 * @return value
		 */
		@Nullable
		default <T> T value(Class<T> clazz) {
			return clazz.cast(this.value());
		}

		/**
		 * Returns whether the attribute value exists
		 * @return whether the attribute value exists
		 */
		default boolean exists() {
			return this.value() != null;
		}

		/**
		 * Return whether the attribute value is equal to the given value.
		 * @param value value
		 * @return whether the attribute value is equal to the given value.
		 */
		default boolean isEqualTo(Object value) {
			return Objects.equals(this.value(), value);
		}

	}

}
