/*
 * SPDX-FileCopyrightText: 2018-2026 Toshiaki Maki <makingx@gmail.com>
 * SPDX-License-Identifier: Apache-2.0
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
