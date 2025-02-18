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

import java.util.function.BiPredicate;

@FunctionalInterface
public interface ConstraintCondition<T> extends BiPredicate<T, ConstraintContext> {

	/**
	 * @since 0.11.0
	 */
	static <T> ConstraintCondition<T> hasAttribute(String key) {
		return (t, context) -> context.attribute(key).exists();
	}

	/**
	 * @since 0.11.0
	 */
	static <T> ConstraintCondition<T> hasAttributeWithValue(String key, Object value) {
		return (t, context) -> context.attribute(key).isEqualTo(value);
	}

}
