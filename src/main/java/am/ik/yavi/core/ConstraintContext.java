/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
 */
package am.ik.yavi.core;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

/**
 * Context values that are passed to validation and read by {@link ConstraintCondition}s.
 *
 * <p>
 * A context exposes named attributes. Use {@link #from(Map)} or {@link #from(Function)}
 * to create a context backed by a map or by a lookup function, or implement this
 * interface directly. {@link ConstraintGroup} is a context that only carries a name.
 *
 * <p>
 * Example: <pre>{@code
 * ConstraintContext context = ConstraintContext.from(Map.of("country", "IT"));
 * validator.validate(target, context);
 * }</pre>
 *
 * @since 0.11.0
 * @see ConstraintCondition
 * @see ConstraintGroup
 */
public interface ConstraintContext {

	/**
	 * Returns the attribute for the given key.
	 * @param key key
	 * @return the attribute
	 */
	Attribute attribute(String key);

	/**
	 * Returns the context name.
	 *
	 * <p>
	 * This method exists on this interface for backwards compatibility with YAVI 0.10 and
	 * earlier. <br>
	 * It is effectively used in the lower interface <code>ConstraintGroup</code>.
	 * @return context name
	 */
	String name();

	/**
	 * Returns a context whose attributes are read from the given map. Looking up a key
	 * that is not in the map yields an attribute with a {@code null} value.
	 * @param map the map that backs the attributes
	 * @return a context backed by the map
	 */
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

	/**
	 * Returns a context whose attributes are read by applying the given function to the
	 * attribute key. The function returns the attribute value, or {@code null} when the
	 * attribute does not exist.
	 * @param function the function that looks up attribute values
	 * @return a context backed by the function
	 */
	static ConstraintContext from(Function<String, ? extends @Nullable Object> function) {
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

	/**
	 * An attribute value of a {@link ConstraintContext}.
	 */
	@FunctionalInterface
	interface Attribute {

		/**
		 * Returns the attribute value. <code>null</code> is returned if the attribute
		 * does not exist.
		 * @return value
		 */
		@Nullable Object value();

		/**
		 * Returns the typed attribute value.
		 * @param clazz the type of the attribute value
		 * @param <T> the type of the attribute value
		 * @return value
		 * @throws ClassCastException if the attribute value is not an instance of the
		 * given class
		 */
		@Nullable default <T> T value(Class<T> clazz) {
			return clazz.cast(this.value());
		}

		/**
		 * Returns whether the attribute value exists.
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
