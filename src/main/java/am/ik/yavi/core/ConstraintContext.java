/*
 * Copyright (C) 2018-2022 Toshiaki Maki <makingx@gmail.com>
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
import java.util.function.Function;

import am.ik.yavi.jsr305.Nullable;

/**
 * Constraint Context that contains the name and attributes.
 *
 * @since 0.11.0
 */
public interface ConstraintContext {
	/**
	 * Returns the attribute value for the given key. <code>null</code> is returned if the
	 * key does not exit.
	 * @param key key
	 * @return value
	 */
	@Nullable
	Object attribute(String key);

	/**
	 * Returns the typed attribute value for the given key
	 * @param key key
	 * @return value
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	default <T> T attribute(String key, Class<T> clazz) {
		final Object attribute = attribute(key);
		if (attribute == null && clazz == Boolean.class) {
			return (T) Boolean.valueOf(null);
		}
		return clazz.cast(attribute);
	}

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
			public Object attribute(String key) {
				return map.get(key);
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
			public Object attribute(String key) {
				return function.apply(key);
			}

			@Override
			public String name() {
				return "ConstraintContextFromFunction";
			}
		};
	}
}
