/*
 * Copyright (C) 2018-2024 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	 *
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
